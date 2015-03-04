// vim: sw=2 ts=2 softtabstop=2 expandtab :
package com.kinja.play.plugins

import play.api._
import play.twirl.api._
import play.api.Play.current

import collection.JavaConversions._

import com.google.inject.Guice
import com.google.inject.Injector

import com.google.inject.Module
import com.google.template.soy.SoyFileSet
import com.google.template.soy.data.SoyData
import com.google.template.soy.data.SoyListData
import com.google.template.soy.data.SoyMapData
import com.google.template.soy.tofu.SoyTofu
import com.google.template.soy.msgs.SoyMsgBundle
import com.google.template.soy.msgs.SoyMsgBundleHandler
import com.google.template.soy.xliffmsgplugin.XliffMsgPlugin

import java.io.File
import java.net.URL
import java.lang.Class

import com.kinja.soy.{ SoyNull, SoyString, SoyBoolean, SoyInt, SoyFloat, SoyDouble, SoyList, SoyMap }

class InvalidClosureValueException(obj: Any, path: Option[String] = None) extends Exception {

  private val maxMessageLength: Int = 100

  private val clazz: String = obj.getClass.getName

  private val objAsString: String = obj.toString match {
    case s if s.size > maxMessageLength => s.take(maxMessageLength) + "..."
    case s => s
  }

  override val getMessage: String = "Unsupported value [" + clazz + "]" +
    path.map(" at " + _).getOrElse("") + ": " + objAsString
}

/**
 * Play plugin for Closure.
 */
class ClosurePlugin(app: Application) extends Plugin {

  private lazy val assetPath: Option[String] = app.configuration.getString("closureplugin.assetPath")
  private lazy val soyPaths: List[String] =
    app.configuration.getStringList("closureplugin.soyPaths").getOrElse {
      val defaults = new java.util.LinkedList[java.lang.String]()
      defaults.add("app/views/closure")
      defaults
    }.toList

  private lazy val modules: Seq[com.google.inject.Module] =
    app.configuration.getStringList("closureplugin.plugins").map(_.flatMap(s =>
      (try {
        app.classloader.loadClass(s).newInstance()
      } catch {
        case e: ClassNotFoundException =>
          throw new ClosurePluginException("Plugin class: " + s + " not found.")
        case e: InstantiationException =>
          throw new ClosurePluginException("Plugin class: " + s + " has no default constructor.")
        case e: IllegalAccessException =>
          throw new ClosurePluginException("Plugin class: " + s + " has no accessible constructor.")
      }) match {
        case e: com.google.inject.Module => List(e)
        case _ => List.empty
      })).getOrElse(Seq.empty)

  private var engine: ClosureEngine = null
  private var version: String = ""

  def log = Logger("closureplugin")

  def newEngine: ClosureEngine = assetPath match {
    // read templates from filesystem
    case Some(rootDir) => ClosureEngine(app.mode, soyPaths, rootDir, modules)
    // read templates from jar
    case _ => ClosureEngine.apply(modules)
  }

  def reloadEngine(): Unit = {
    log.info("Reloading engine")
    engine = newEngine.build
  }

  def getVersion: String = version

  def setVersion(value: String): Boolean = {
    if (version != value) {
      version = value
      reloadEngine()
      true
    } else {
      false
    }
  }

  /**
   * If the app is running in production mode then always returns a same engine instance,
   * otherwise returns a brand new instance.
   */
  def api: ClosureEngine = {
    if (engine == null) {
      reloadEngine()
    }
    engine
  }

  override def onStart() = {
    if (enabled) {
      log.info("start on mode: " + app.mode)

      version = Play.application.configuration.getString("buildNumber").getOrElse(
        throw new Exception("buildNumber config is missing"))
      // This prevent the new engine creatation on startup in dev mode.
      //if (app.mode == Mode.Prod) {
      api
      //}
    } else {
      log.info("plugin is disabled")
    }
  }

  override lazy val enabled = {
    !app.configuration.getString("closureplugin.status").filter(
      _ == "disabled").isDefined
  }
}

/**
 * Closure Template API
 *
 * @param files List of your templates
 */
class ClosureEngine(
    val files: Traversable[URL],
    localeDir: Option[File] = None,
    val DEFAULT_LOCALE: String = "en-US",
    val modules: Seq[com.google.inject.Module]) {

  var injector: Injector = Guice.createInjector(modules: _*)

  val KEY_DELEGATE_NS = "delegate"

  val KEY_LOCALE = "locale"

  private val log = Logger("closureplugin")

  @inline private def message(path: String, a: Any): String =
    path.tail + " <- " + {
      a match {
        case null => "null"
        case s: String if s.isEmpty => "(empty string)"
        case _ =>
          a.toString match {
            case s if s.size > 100 => s.take(100) + "... (" + s.size + " bytes)"
            case s => s
          }
      }
    }

  /**
   * The current compiled templates.
   */
  protected lazy val tofu: SoyTofu = builder

  private def addSoyValue(sl: SoyListData, a: Any, path: => String): Unit = {
    log.debug(message(path, a))
    a match {
      case s: SoyMap => sl.add(s.build)
      case s: SoyList => sl.add(s.build)
      case s: SoyString => Option(s.build) map (v => sl.add(v)) // prevent NullPointerException
      case s: SoyBoolean => sl.add(s.build)
      case s: SoyInt => sl.add(s.build)
      case s: SoyFloat => sl.add(s.build)
      case s: SoyDouble => sl.add(s.build)
      case SoyNull => // do nothing
      case mm: Map[String, Any] => sl.add(mapToSoyData(mm, path))
      case l: Seq[Any] => sl.add(seqToSoyData(l, path))
      case s: String => sl.add(s)
      case d: Double => sl.add(d)
      case f: Float => sl.add(f)
      case l: Long => sl.add(l.toString)
      case i: Int => sl.add(i)
      case b: Boolean => sl.add(b)
      case s: Set[_] => sl.add(seqToSoyData(s.toSeq, path))
      case m: SoyMapData => sl.add(m)
      case l: SoyListData => sl.add(l)
      case None => // do nothing
      case null => // do nothing
      case _ => throw new InvalidClosureValueException(a, Some(path.tail))
    }
  }

  private def seqToSoyData(l: Seq[Any], path: => String): SoyListData = {
    val sl = new SoyListData()
    l.foreach { v =>
      v match {
        case Some(a: Any) => addSoyValue(sl, a, path + "[]")
        case _ => addSoyValue(sl, v, path + "[]")
      }
    }
    sl
  }

  private def putSoyValue(sm: SoyMapData, k: String, a: Any, path: => String): Unit = {
    log.debug(message(path, a))
    a match {
      case s: SoyMap => sm.put(k, s.build)
      case s: SoyList => sm.put(k, s.build)
      case s: SoyString => Option(s.build) map (v => sm.put(k, v)) // prevent NullPointerException
      case s: SoyBoolean => sm.put(k, s.build)
      case s: SoyInt => sm.put(k, s.build)
      case s: SoyFloat => sm.put(k, s.build)
      case s: SoyDouble => sm.put(k, s.build)
      case SoyNull => // do nothing
      case mm: Map[String, Any] => sm.put(k, mapToSoyData(mm, path))
      case l: Seq[Any] => sm.put(k, seqToSoyData(l, path))
      case s: String => sm.put(k, s)
      case d: Double => sm.put(k, d)
      case f: Float => sm.put(k, f)
      case l: Long => sm.put(k, l.toString)
      case i: Int => sm.put(k, i)
      case b: Boolean => sm.put(k, b)
      case s: Set[_] => sm.put(k, seqToSoyData(s.toSeq, path))
      case m: SoyMapData => sm.put(k, m)
      case l: SoyListData => sm.put(k, l)
      case None => // do nothing
      case null => // do nothing
      case _ => throw new InvalidClosureValueException(a, Some(path.tail))
    }
  }

  private def mapToSoyData(m: Map[String, Any], path: => String): SoyMapData = {
    val sm = new SoyMapData()
    m.keys.foreach { k =>
      m(k) match {
        case Some(a: Any) => putSoyValue(sm, k, a, path + "." + k)
        case a => putSoyValue(sm, k, a, path + "." + k)
      }
    }
    sm
  }

  /**
   * Helper.
   *
   * Creates a tofu instance and returns an engine instance.
   */
  def build: ClosureEngine = {
    tofu
    this
  }

  /**
   * Add all input files to the builder.
   *
   * @param input List of template files
   */
  def fileSet(input: Traversable[URL]): SoyFileSet.Builder = {
    val soyBuilder = injector.getInstance(classOf[SoyFileSet.Builder])
    //val soyBuilder = new SoyFileSet.Builder()
    input.foreach(file => {
      Logger("closureplugin").debug("Add " + file)
      soyBuilder.add(file)
    })
    soyBuilder
  }

  /**
   * Compile the current file set - which stored in the files lazy val - into Java object.
   */
  def builder: SoyTofu = fileSet(files).build.compileToTofu

  private def msgBundleAsResource(locale: String): Option[SoyMsgBundle] = {
    getClass.getResource("/" + locale + ".xlf") match {
      case url: java.net.URL =>
        val bundleHandler = new SoyMsgBundleHandler(new XliffMsgPlugin())
        val bundle = bundleHandler.createFromResource(url)
        Some(bundle)
      case _ => None
    }
  }

  def msgBundle(locale: String): Option[SoyMsgBundle] = {
    if (locale != DEFAULT_LOCALE) {
      localeDir match {
        case Some(dir: File) =>
          val xlf = new File(dir.getCanonicalPath + "/" + locale + ".xlf")
          if (xlf.exists) {
            val bundleHandler = new SoyMsgBundleHandler(new XliffMsgPlugin())
            val bundle = bundleHandler.createFromFile(xlf)
            Some(bundle)
          } else {
            msgBundleAsResource(locale)
          }
        case _ => msgBundleAsResource(locale)
      }
    } else {
      None
    }
  }

  /**
   * Creates a new Renderer for a template without any msgbundle.
   *
   * @param template The name of the template to render.
   *                 You can use names like "closuretest.index.soy", the .soy extension will be removed.
   */
  def newRenderer(template: String): SoyTofu.Renderer =
    tofu.newRenderer(template.replace(".soy", ""))

  /**
   * Creates a new Renderer for a template with an opitional msgbundle.
   *
   * @param template	The name of the template to render.
   * 									You can use names like "closuretest.index.soy", the .soy extension will be removed.
   */
  def renderer(template: String, locale: String = DEFAULT_LOCALE): SoyTofu.Renderer = {
    msgBundle(locale) match {
      case Some(bundle: SoyMsgBundle) => newRenderer(template).setMsgBundle(bundle)
      case _ => newRenderer(template)
    }
  }

  /**
   * 	Renders a template.
   *
   * @param template The name of the template to render.
   * @param data The data to call the template with.
   */
  def render(template: String, data: Map[String, Any], inject: Map[String, Any]): String = {
    log.debug("Rendering " + template)
    val locale: String = data.get(KEY_LOCALE) match {
      case Some(s: String) => s
      case _ => DEFAULT_LOCALE
    }
    data.get(KEY_DELEGATE_NS) match {
      case Some(sd: Set[String]) =>
        renderer(template, locale)
          .setActiveDelegatePackageNames(sd)
          .setData(mapToSoyData(data, path = ""))
          .setIjData(mapToSoyData(inject, path = ""))
          .render()
      case _ =>
        renderer(template, locale)
          .setData(mapToSoyData(data, path = ""))
          .setIjData(mapToSoyData(inject, path = ""))
          .render()
    }
  }

  /**
   * Renders a template.
   *
   * @param template The name of the template to render.
   * @param data The data to call the template with.
   */
  def render(template: String, data: SoyMapData, inject: SoyMapData): String = {
    val locale: String =
      try {
        data.getString(KEY_LOCALE)
      } catch {
        case _: IllegalArgumentException => DEFAULT_LOCALE
      }

    val delegate: Option[String] =
      try {
        Option(data.getListData(KEY_DELEGATE_NS)) flatMap { ld =>
          if (ld.length > 0) Some(ld.get(0).toString) else None
        }
      } catch {
        case _: Throwable =>
          try {
            Option(data.getString(KEY_DELEGATE_NS))
          } catch {
            case _: Throwable => None
          }
      }

    delegate match {
      case Some(sd: String) =>
        renderer(template, locale)
          .setActiveDelegatePackageNames(Set(sd.toString))
          .setData(data)
          .setIjData(inject)
          .render()
      case _ =>
        renderer(template, locale)
          .setData(data)
          .setIjData(inject)
          .render()
    }

  }

}

object ClosureEngine {

  val resourceFile = "/closure_templates.txt"

  /**
   * Creates a new engine by mode.
   *
   * @param mode Play's mode (development, production, test, etc)
   * @param version Version of the templates files a.k.a. build number
   * @param rootDir Templates's root directory. The templates must be
   *        in rootDir + "/" + version + "/closure" directory
   *
   * @return A new ClosureEngine instance
   */
  def apply(
    mode: Mode.Mode,
    soyPaths: List[String],
    rootDir: String,
    modules: Seq[com.google.inject.Module]): ClosureEngine = mode match {
    case Mode.Dev => soyPaths match {
      case sp: List[String] if sp.nonEmpty => apply(sp, "app/locales", modules)
      case _ => apply(List("app/views/closure"), "app/locales", modules)
    }
    case Mode.Test => apply(List("test/views/closure"), "test/locales", modules)
    case _ =>
      val templateDirs = soyPaths match {
        case sp: List[String] if sp.nonEmpty =>
          sp.map(p => new File(rootDir + "/" + p + "/closure"))
        case _ => List(new File(rootDir + "/closure"))
      }
      val existingTemplateDirs = templateDirs.filter(_.exists)
      Logger("closureplugin").info("Checking template directory: " + templateDirs)
      if (existingTemplateDirs.nonEmpty) {
        Logger("closureplugin").info("Using '" + existingTemplateDirs + "' template directory")
        val localeDir = new File(rootDir + "/locales")
        if (localeDir.exists) {
          Logger("closureplugin").info("Using '" + localeDir + "' locale directory")
          apply(existingTemplateDirs, Some(localeDir), modules = modules)
        } else {
          apply(existingTemplateDirs, None, modules = modules)
        }
      } else {
        Logger("closureplugin").error("Template directory '" + templateDirs + "' does not exists. Falling back to jar.")
        apply(modules)
      }
  }

  /**
   * Templates are in the jar as resource
   *
   */
  def apply(modules: Seq[com.google.inject.Module]): ClosureEngine = {
    val res = getClass.getResourceAsStream(resourceFile)
    if (res == null) {
      throw new Exception("Resource file not foud: " + resourceFile)
    } else {
      new ClosureEngine(
        scala.io.Source.fromInputStream(res, "UTF-8").getLines().map(line => {
          getClass.getResource(line)
        }).toList, modules = modules)
    }
  }

  /**
   * Creates a new engine.
   *
   * @param templateDirs Directory of template files.
   */
  def apply(templateDirs: List[File], localeDir: Option[File], modules: Seq[com.google.inject.Module]): ClosureEngine = new ClosureEngine(
    templateDirs.flatMap(recursiveListFiles(_, ".soy")).map(_.toURI.toURL), localeDir, modules = modules)

  /**
   * Creates a new engine.
   *
   * @param templateDirs Directory of template files.
   */
  def apply(templateDirs: List[String], localeDir: String, modules: Seq[com.google.inject.Module]): ClosureEngine =
    apply(
      templateDirs.map(td => new File(td)),
      Some(new File(localeDir)),
      modules = modules)

  def recursiveListFiles(f: File, extension: String = ""): Array[File] = {
    Logger("closureplugin").info(s"Reading template files from ${f.getPath}")
    val these = f.listFiles
    these.filter(_.getName.endsWith(extension)) ++ these.filter(_.isDirectory).flatMap(recursiveListFiles(_, extension))
  }

  /**
   * Returns all soy files from source directories.
   */
  //def fileList: Traversable[File] = sourceDirectories.flatMap(recursiveListFiles(_, ".soy"))

}

/**
 * Helper object
 */
object Closure {

  private def plugin = play.api.Play.maybeApplication.map { app =>
    app.plugin[ClosurePlugin].getOrElse(throw new RuntimeException("you should enable ClosurePlugin in play.plugins"))
  }.getOrElse(throw new RuntimeException("you should have a running app in scope a this point"))

  // PUBLIC INTERFACE
  def reloadEngineCache(): Unit = plugin.reloadEngine()

  def setVersion(value: String): Boolean = plugin.setVersion(value)

  def render(template: String, data: Map[String, Any] = Map()): String =
    plugin.api.render(template, data, Map[String, Any]())

  def render(template: String, data: Map[String, Any], inject: Map[String, Any]): String =
    plugin.api.render(template, data, inject)

  def render(template: String, data: SoyMapData): String =
    plugin.api.render(template, data, new SoyMapData())

  def render(template: String, data: SoyMapData, inject: SoyMapData): String =
    plugin.api.render(template, data, inject)

  def render(template: String, data: SoyMap): String =
    plugin.api.render(template, data.build, new SoyMapData())

  def render(template: String, data: SoyMap, inject: SoyMap): String =
    plugin.api.render(template, data.build, inject.build)

  def html(template: String, data: Map[String, Any] = Map()): Html =
    Html(render(template, data, Map[String, Any]()))

  def html(template: String, data: Map[String, Any], inject: Map[String, Any]): Html =
    Html(render(template, data, inject))

  def html(template: String, data: SoyMapData): Html =
    Html(render(template, data, new SoyMapData))

  def html(template: String, data: SoyMapData, inject: SoyMapData): Html =
    Html(render(template, data, inject))
}

class ClosurePluginException(msg: String) extends Exception(msg)
