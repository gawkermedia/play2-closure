// vim: sw=2 ts=2 softtabstop=2 expandtab :
package com.kinja.play

import com.google.common.base.Predicates
import com.google.inject.{ Guice, Injector }
import com.google.template.soy.data.SanitizedContent
import com.google.template.soy.msgs.{ SoyMsgBundle, SoyMsgBundleHandler }
import com.google.template.soy.SoyFileSet
import com.google.template.soy.jbcsrc.api.SoySauce
import com.google.template.soy.xliffmsgplugin.XliffMsgPlugin

import com.kinja.soy._

import java.io.File
import java.net.URL
import java.nio.file.Paths
import java.util.jar.JarFile
import javax.inject.Inject

import play.api.{ Configuration, Environment }
import play.api.inject.Binding
import play.api.{ Logger, Mode }

import scala.annotation.unused
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._
import scala.util.Try
import scala.util.control.NonFatal

class InvalidClosureValueException(obj: Any, path: Option[String] = None) extends Exception {

  private val maxMessageLength: Int = 100

  private val clazz: String = obj.getClass.getName

  private val objAsString: String = obj.toString match {
    case s if s.length > maxMessageLength => s.take(maxMessageLength) + "..."
    case s => s
  }

  override val getMessage: String = "Unsupported value [" + clazz + "]" +
    path.map(" at " + _).getOrElse("") + ": " + objAsString
}

trait ClosureComponent {

  def environment: Environment

  def getVersion: String

  def setVersion(value: String): Boolean

  def render(template: String): String

  def render(template: String, data: java.util.Map[String, Any]): String

  def render(template: String, data: java.util.Map[String, Any], inject: java.util.Map[String, Any]): String

  def render(template: String, data: SoyMap): String

  def render(template: String, data: SoyMap, inject: SoyMap): String
}

/**
 * Play plugin for Closure.
 */
class ClosureComponentImpl @Inject() (
    configuration: Configuration,
    override val environment: Environment) extends ClosureComponent {

  private lazy val assetPath: Option[String] = Try(configuration.underlying.getString("closureplugin.assetPath")).toOption
  private lazy val soyPaths: Seq[String] =
    Try(configuration.underlying.getStringList("closureplugin.soyPaths")).toOption.map(_.asScala.toSeq)
      .getOrElse(Seq("app/views/closure"))

  private lazy val modules: Seq[com.google.inject.Module] =
    Try(configuration.underlying.getStringList("closureplugin.plugins")).toOption.map(_.asScala.toSeq.flatMap(s =>
      (try {
        environment.classLoader.loadClass(s).newInstance()
      } catch {
        case _: ClassNotFoundException =>
          throw new ClosureModuleException("Module class: " + s + " not found.")
        case _: InstantiationException =>
          throw new ClosureModuleException("Module class: " + s + " has no default constructor.")
        case _: IllegalAccessException =>
          throw new ClosureModuleException("Module class: " + s + " has no accessible constructor.")
      }) match {
        case e: com.google.inject.Module => Seq(e)
        case _ => Seq.empty
      })).getOrElse(Seq.empty)

  private var engine: ClosureEngine = null
  private var version: String = ""

  private def log = Logger("closureplugin")

  private def newEngine: ClosureEngine = assetPath match {
    // read templates from filesystem
    case Some(rootDir) => ClosureEngine(environment.mode, soyPaths, rootDir, modules)
    // read templates from jar
    case _ => ClosureEngine.apply(soyPaths, modules)
  }

  private def reloadEngine(): Unit = {
    log.info("Reloading engine")
    engine = newEngine.build
  }

  override def getVersion: String = version

  override def setVersion(value: String): Boolean = {
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
  private def api: ClosureEngine = {
    if (engine == null) {
      reloadEngine()
    }
    engine
  }

  override def render(template: String): String =
    api.render(template, new java.util.HashMap[String, Any](), new java.util.HashMap[String, Any]())

  override def render(template: String, data: java.util.Map[String, Any]): String =
    api.render(template, data, new java.util.HashMap[String, Any]())

  override def render(template: String, data: java.util.Map[String, Any], inject: java.util.Map[String, Any]): String =
    api.render(template, data, inject)

  override def render(template: String, data: SoyMap): String =
    api.render(template, data.build, new java.util.HashMap[String, Any]())

  override def render(template: String, data: SoyMap, inject: SoyMap): String =
    api.render(template, data.build, inject.build)

  log.info("start on mode: " + environment.mode)

  version = Try(configuration.underlying.getString("buildNumber")).toOption
    .getOrElse(throw new Exception("buildNumber config is missing"))

  // start the ClosureEngine
  api
}

/**
 * Closure Template API
 *
 * @param files List of your templates
 */
class ClosureEngine(
    val files: Iterable[URL],
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
            case s if s.length > 100 => s.take(100) + "... (" + s.length + " bytes)"
            case s => s
          }
      }
    }

  @inline private def ignore[A](@unused a: A): Unit = ()

  /**
   * Compile the current file set - which stored in the files lazy val - into Java object.
   */
  def newSoySauce: SoySauce = {
    val fileSetBuilder = injector.getInstance(classOf[SoyFileSet.Builder])
    //val fileSetBuilder = new SoyFileSet.Builder()
    files.foreach { file =>
      log.debug("Add " + file)
      fileSetBuilder.add(file, file.toString)
    }
    fileSetBuilder.build.compileTemplates
  }

  /**
   * The current compiled templates.
   */
  protected lazy val soySauce: SoySauce = newSoySauce

  private def addSoyValue(list: java.util.LinkedList[Any], a: Any, path: => String): Unit = {
    log.debug(message(path, a))
    a match {
      case s: SoyMap => ignore(list.add(s.build))
      case s: SoyList => ignore(list.add(s.build))
      case s: SoyString => Option(s.build).foreach(v => list.add(v)) // prevent NullPointerException
      case s: SoyBoolean => ignore(list.add(s.build))
      case s: SoyInt => ignore(list.add(s.build))
      case s: SoyFloat => ignore(list.add(s.build: Double))
      case s: SoyHtml => ignore(list.add(s.build))
      case s: SoyUri => ignore(list.add(s.build))
      case s: SoyCss => ignore(list.add(s.build))
      case s: SoyJs => ignore(list.add(s.build))
      case SoyNull => // do nothing
      case mm: Map[String, Any] => ignore(list.add(mapToSoyData(mm, path)))
      case l: Seq[Any] => ignore(list.add(seqToSoyData(l, path)))
      case s: String => ignore(list.add(s))
      case d: Double => ignore(list.add(d))
      case f: Float => ignore(list.add(f.toDouble))
      case l: Long => ignore(list.add(l.toString))
      case i: Int => ignore(list.add(i))
      case b: Boolean => ignore(list.add(b))
      case s: Set[_] => ignore(list.add(seqToSoyData(s.toSeq, path)))
      case m: java.util.Map[String, Any] => ignore(list.add(m))
      case l: java.util.List[Any] => ignore(list.add(l))
      case s: SanitizedContent => ignore(list.add(s))
      case None => // do nothing
      case null => // do nothing
      case _ => throw new InvalidClosureValueException(a, Some(path.tail))
    }
  }

  private def seqToSoyData(l: Seq[Any], path: => String): java.util.List[Any] = {
    val sl = new java.util.LinkedList[Any]()
    l.foreach {
      case Some(a: Any) => addSoyValue(sl, a, path + "[]")
      case v => addSoyValue(sl, v, path + "[]")
    }
    sl
  }

  private def putSoyValue(map: java.util.HashMap[String, Any], k: String, a: Any, path: => String): Unit = {
    log.debug(message(path, a))
    a match {
      case s: SoyMap => ignore(map.put(k, s.build))
      case s: SoyList => ignore(map.put(k, s.build))
      case s: SoyString => Option(s.build) foreach (v => map.put(k, v)) // prevent NullPointerException
      case s: SoyBoolean => ignore(map.put(k, s.build))
      case s: SoyInt => ignore(map.put(k, s.build))
      case s: SoyFloat => ignore(map.put(k, s.build: Double))
      case s: SoyHtml => ignore(map.put(k, s.build))
      case s: SoyUri => ignore(map.put(k, s.build))
      case s: SoyCss => ignore(map.put(k, s.build))
      case s: SoyJs => ignore(map.put(k, s.build))
      case SoyNull => // do nothing
      case mm: Map[String, Any] => ignore(map.put(k, mapToSoyData(mm, path)))
      case l: Seq[Any] => ignore(map.put(k, seqToSoyData(l, path)))
      case s: String => ignore(map.put(k, s))
      case d: Double => ignore(map.put(k, d))
      case f: Float => ignore(map.put(k, f.toDouble))
      case l: Long => ignore(map.put(k, l.toString))
      case i: Int => ignore(map.put(k, i))
      case b: Boolean => ignore(map.put(k, b))
      case s: Set[_] => ignore(map.put(k, seqToSoyData(s.toSeq, path)))
      case m: java.util.Map[String, Any] => ignore(map.put(k, m))
      case l: java.util.List[Any] => ignore(map.put(k, l))
      case s: SanitizedContent => ignore(map.put(k, s))
      case None => // do nothing
      case null => // do nothing
      case _ => throw new InvalidClosureValueException(a, Some(path.tail))
    }
  }

  private def mapToSoyData(m: Map[String, Any], path: => String): java.util.HashMap[String, Any] = {
    val sm = new java.util.HashMap[String, Any]()
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
   * Creates a sauce instance and returns an engine instance.
   */
  def build: ClosureEngine = {
    soySauce
    this
  }

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
        case Some(dir) =>
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
  def newRenderer(template: String): SoySauce.Renderer =
    soySauce.renderTemplate(template.replace(".soy", ""))

  /**
   * Creates a new Renderer for a template with an opitional msgbundle.
   *
   * @param template	The name of the template to render.
   * 									You can use names like "closuretest.index.soy", the .soy extension will be removed.
   */
  def renderer(template: String, locale: String = DEFAULT_LOCALE): SoySauce.Renderer = {
    msgBundle(locale)
      .map(bundle => newRenderer(template).setMsgBundle(bundle))
      .getOrElse(newRenderer(template))
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
          .setActiveDelegatePackageSelector(Predicates.in(sd.asJava))
          .setData(mapToSoyData(data, path = ""))
          .setIj(mapToSoyData(inject, path = ""))
          .renderText()
          .get()
      case _ =>
        renderer(template, locale)
          .setData(mapToSoyData(data, path = ""))
          .setIj(mapToSoyData(inject, path = ""))
          .renderText()
          .get()
    }
  }

  /**
   * Renders a template.
   *
   * @param template The name of the template to render.
   * @param data The data to call the template with.
   */
  def render(template: String, data: java.util.Map[String, Any], inject: java.util.Map[String, Any]): String = {
    val locale: String =
      try {
        val d: Any = data.get(KEY_LOCALE)
        if (d.isInstanceOf[String]) {
          d.asInstanceOf[String]
        } else {
          DEFAULT_LOCALE
        }
      } catch {
        case _: IllegalArgumentException => DEFAULT_LOCALE
      }

    val delegate: Option[String] =
      try {
        Option(data.get(KEY_DELEGATE_NS).asInstanceOf[java.util.List[Any]]) flatMap { ld =>
          if (ld.size > 0) Some(ld.get(0).toString) else None
        }
      } catch {
        case _: Throwable =>
          try {
            Option(data.get(KEY_DELEGATE_NS).asInstanceOf[String])
          } catch {
            case _: Throwable => None
          }
      }

    delegate match {
      case Some(sd: String) =>
        renderer(template, locale)
          .setActiveDelegatePackageSelector(Predicates.in(Set(sd.toString).asJava))
          .setData(data)
          .setIj(inject)
          .renderText()
          .get()
      case _ =>
        renderer(template, locale)
          .setData(data)
          .setIj(inject)
          .renderText()
          .get()
    }

  }

}

object ClosureEngine {

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
    mode: Mode,
    soyPaths: Seq[String],
    rootDir: String,
    modules: Seq[com.google.inject.Module]): ClosureEngine = mode match {
    case Mode.Dev => soyPaths match {
      case sp if sp.nonEmpty => apply(sp, "app/locales", modules)
      case _ => apply(Seq("app/views/closure"), "app/locales", modules)
    }
    case Mode.Test => apply(Seq("test/views/closure"), "test/locales", modules)
    case _ =>
      val templateDirs = soyPaths match {
        case sp if sp.nonEmpty =>
          sp.map(p => new File(rootDir + "/" + p + "/closure"))
        case _ => Seq(new File(rootDir + "/closure"))
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
        apply(soyPaths, modules)
      }
  }

  /**
   * Templates are in the jar as resource
   *
   */
  def apply(soyPaths: Seq[String], modules: Seq[com.google.inject.Module]): ClosureEngine = {
    val buffer = new ListBuffer[URL]()

    // get soy files from current working directory
    val dirs = if (soyPaths.nonEmpty) soyPaths else Seq(".")
    buffer ++= dirs.flatMap(dir => recursiveListFiles(Paths.get(dir).toFile, ".soy")).map(_.toURI.toURL)

    // get soy files from the classpath
    val classPath = System.getProperty("java.class.path", ".")
    val classPathElements = classPath.split(System.getProperty("path.separator"))
    classPathElements.foreach { element =>
      try {
        val file = new File(element)
        if (file.isDirectory) {
          buffer ++= recursiveListFiles(file, ".soy").map(_.toURI.toURL)
        } else {
          val jar = new JarFile(file)
          val entries = jar.entries
          while (entries.hasMoreElements) {
            val name = entries.nextElement.getName
            if (name.endsWith(".soy")) {
              buffer += new URL("jar:file://" + file.getAbsolutePath + "!/" + name)
            }
          }
        }
      } catch {
        case NonFatal(_) =>
      }
    }

    if (buffer.isEmpty) {
      throw new Exception("No template files found on either the classpath or \n" + dirs.map(new File(_).getAbsolutePath).mkString(" or "))
    }

    new ClosureEngine(buffer.toList, modules = modules)
  }

  /**
   * Creates a new engine.
   *
   * @param templateDirs Directory of template files.
   */
  def apply(templateDirs: Seq[File], localeDir: Option[File], modules: Seq[com.google.inject.Module]): ClosureEngine =
    new ClosureEngine(templateDirs.flatMap(recursiveListFiles(_, ".soy")).map(_.toURI.toURL), localeDir, modules = modules)

  /**
   * Creates a new engine.
   *
   * @param templateDirs Directory of template files.
   */
  def apply(templateDirs: Seq[String], localeDir: String, modules: Seq[com.google.inject.Module]): ClosureEngine =
    apply(
      templateDirs.map(td => new File(td)),
      Some(new File(localeDir)),
      modules = modules)

  def recursiveListFiles(f: File, extension: String = ""): Array[File] = {
    Logger("closureplugin").info(s"Reading template files from ${f.getPath}")
    val these = Option(f.listFiles).getOrElse(Array.empty)
    these.filter(_.getName.endsWith(extension)) ++ these.filter(_.isDirectory).flatMap(recursiveListFiles(_, extension))
  }

  /**
   * Returns all soy files from source directories.
   */
  //def fileList: Traversable[File] = sourceDirectories.flatMap(recursiveListFiles(_, ".soy"))

}

/**
 * Play module.
 */
class ClosureModule extends play.api.inject.Module {
  def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[ClosureComponent].to[ClosureComponentImpl])
  }
}

class ClosureModuleException(msg: String) extends Exception(msg)
