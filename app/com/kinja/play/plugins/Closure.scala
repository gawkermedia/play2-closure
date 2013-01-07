// vim: sw=2 ts=2 softtabstop=2 expandtab :
package com.kinja.play.plugins

import play.api._
import play.api.templates._
import play.api.Configuration._
import play.api.Play.current

import collection.JavaConversions._

import com.google.template.soy.SoyFileSet
import com.google.template.soy.data.SoyListData
import com.google.template.soy.data.SoyMapData
import com.google.template.soy.tofu.SoyTofu
import com.google.template.soy.msgs.SoyMsgBundle
import com.google.template.soy.msgs.SoyMsgBundleHandler
import com.google.template.soy.xliffmsgplugin.XliffMsgPlugin

import java.io.File
import java.net.URL

/**
 * Play plugin for Closure.
 */
class ClosurePlugin(app: Application) extends Plugin {

  lazy val engine = newEngine.build

  def newEngine: ClosureEngine = ClosureEngine(app.mode)

  /**
   * If the app is running in production mode then always returns a same engine instance,
   * otherwise returns a brand new instance.
   */
  def api = if (app.mode == Mode.Prod) {
    engine
  } else {
    newEngine
  }

  override def onStart() = {
    Logger("closureplugin").info("start on mode: " + app.mode)
    // This reprevent the new engine creatation on startup in dev mode.
    if (app.mode == Mode.Prod) {
      api
    }
  }

  override lazy val enabled = {
    !app.configuration.getString("closureplugin").filter(
      _ == "disabled"
    ).isDefined
  }
}

/**
 * Closure Template API
 *
 * @param sourceDirectories List of directories where you store your templates
 */
class ClosureEngine(val files: Traversable[URL], val DEFAULT_LOCALE: String = "en_US") {

  val KEY_DELEGATE_NS = "delegate"

  val KEY_LOCALE = "locale"

  /**
   * The current compiled templates.
   */
  protected lazy val tofu: SoyTofu = builder

  /**
   * Converts a case class into a map.
   *
   * @param cc A case class instance.
   */
  protected def getCCParams(cc: AnyRef): Map[String, Any] = {
    (Map[String, Any]() /: cc.getClass.getDeclaredFields) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc))
    }
  }

  protected def addSoyValue(sl: SoyListData, a: Any): Unit = {
    a match {
      case mm: Map[String, Any] => sl.add(mapToSoyData(mm))
      case l: List[Any] => sl.add(listToSoyData(l))
      case s: String => sl.add(s)
      case d: Double => sl.add(d)
      case f: Float => sl.add(f)
      case l: Long => sl.add(l.toString)
      case i: Int => sl.add(i)
      case b: Boolean => sl.add(b)
      case s: Set[String] =>
      case None => null
      case null => null
      case a: AnyRef if a != null => sl.add(mapToSoyData(getCCParams(a)))
      case _ => throw new Exception("Invalid Soy object: " + a)
    }
  }

  protected def listToSoyData(l: List[Any]): SoyListData = {
    val sl = new SoyListData()
    l.foreach { v =>
      v match {
        case Some(a: Any) => addSoyValue(sl, a)
        case _ => addSoyValue(sl, v)
      }
    }
    sl
  }

  protected def putSoyValue(sm: SoyMapData, k: String, a: Any): Unit = {
    a match {
      case mm: Map[String, Any] => sm.put(k, mapToSoyData(mm))
      case l: List[Any] => sm.put(k, listToSoyData(l))
      case s: String => sm.put(k, s)
      case d: Double => sm.put(k, d)
      case f: Float => sm.put(k, f)
      case l: Long => sm.put(k, l.toString)
      case i: Int => sm.put(k, i)
      case b: Boolean => sm.put(k, b)
      case None => null
      case null => null
      case a: AnyRef if a != null => sm.put(k, mapToSoyData(getCCParams(a)))
      case _ => throw new Exception("Invalid Soy object: " + a)
    }
  }

  protected def mapToSoyData(m: Map[String, Any]): SoyMapData = {
    val sm = new SoyMapData()
    m.keys.foreach { k =>
      m(k) match {
        case Some(a: Any) => putSoyValue(sm, k, a)
        case _ => putSoyValue(sm, k, m(k))
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
    val soyBuilder = new SoyFileSet.Builder()
    input.foreach(file => {
      Logger("closureplugin").debug("Add " + file)
      soyBuilder.add(file)
    })
    soyBuilder
  }

  /**
   * Compile the current file set - which stored in the files lazy val -  into Java object.
   */
  def builder: SoyTofu = fileSet(files).build.compileToTofu

  def msgBundle(locale: String): Option[SoyMsgBundle] = {
    if (locale != DEFAULT_LOCALE) {
      getClass.getResource("/" + locale + ".xlf") match {
        case url: java.net.URL => {
          val bundleHandler = new SoyMsgBundleHandler(new XliffMsgPlugin());
          val bundle = bundleHandler.createFromResource(url)
          Some(bundle)
        }
        case _ => None
      }
    } else {
      None
    }
  }

  /**
   * Creates a new Renderer for a template without any msgbundle.
   *
   * @param template  The name of the template to render.
   *                  You can use names like "closuretest.index.soy", the .soy extension will be removed.
   */
  def newRenderer(template: String): SoyTofu.Renderer =
    tofu.newRenderer(template.replace(".soy", ""))

  /**
   * Creates a new Renderer for a template with an opitional msgbundle.
   *
   * @param template  The name of the template to render.
   *                  You can use names like "closuretest.index.soy", the .soy extension will be removed.
   */
  def renderer(template: String, locale: String = DEFAULT_LOCALE): SoyTofu.Renderer = {
    msgBundle(locale) match {
      case Some(bundle: SoyMsgBundle) => newRenderer(template).setMsgBundle(bundle)
      case _ => newRenderer(template)
    }
  }

  /**
   *  Renders a template.
   *
   * @param template The name of the template to render.
   * @param data The data to call the template with.
   */
  def render(template: String, data: Map[String, Any], inject: Map[String, Any]): String = {
    val locale: String = data.get(KEY_LOCALE) match {
      case Some(s: String) => s
      case _ => DEFAULT_LOCALE
    }
    data.get(KEY_DELEGATE_NS) match {
      case Some(sd: Set[String]) =>
        renderer(template, locale)
          .setActiveDelegatePackageNames(sd)
          .setData(mapToSoyData(data))
          .setIjData(mapToSoyData(inject))
          .render()
      case _ =>
        renderer(template, locale)
          .setData(mapToSoyData(data))
          .setIjData(mapToSoyData(inject))
          .render()
    }
  }

  /**
   *  Renders a template.
   *
   * @param template The name of the template to render.
   * @param data The data to call the template with.
   */
  def render(template: String, data: SoyMapData, inject: SoyMapData): String = {
    val locale: String = data.getString(KEY_LOCALE) match {
      case s: String => s
      case _ => DEFAULT_LOCALE
    }
    renderer(template, locale)
      .setData(data)
      .setIjData(inject)
      .render()
  }

}

object ClosureEngine {

  /**
   * Creates a new engine by mode.
   */
  def apply(mode: Mode.Mode): ClosureEngine = apply /*mode match {
		case Mode.Dev => apply("app/views/closure")
		case Mode.Test => apply("test/views")
		case _ => apply
	}*/

  def apply: ClosureEngine = new ClosureEngine(
    scala.io.Source.fromInputStream(getClass.getResourceAsStream("/closure_templates.txt"), "UTF-8").getLines().map(line => {
      getClass.getResource(line)
    }).toList)

  /**
   * Creates a new engine.
   *
   * @param rootDir Root directory of template files.
   */
  def apply(rootDir: String): ClosureEngine = new ClosureEngine(
    List(Play.getFile(rootDir)).flatMap(recursiveListFiles(_, ".soy")).map(_.toURI.toURL))

  def recursiveListFiles(f: File, extension: String = ""): Array[File] = {
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
  def render(template: String, data: Map[String, Any] = Map()): String =
    plugin.api.render(template, data, Map[String, Any]())

  def render(template: String, data: Map[String, Any], inject: Map[String, Any]): String =
    plugin.api.render(template, data, inject)

  def render(template: String, data: SoyMapData): String =
    plugin.api.render(template, data, new SoyMapData())

  def render(template: String, data: SoyMapData, inject: SoyMapData): String =
    plugin.api.render(template, data, inject)

  def html(template: String, data: Map[String, Any] = Map()): Html =
    Html(render(template, data, Map[String, Any]()))

  def html(template: String, data: Map[String, Any], inject: Map[String, Any]): Html =
    Html(render(template, data, inject))

  def html(template: String, data: SoyMapData): Html =
    Html(render(template, data, new SoyMapData))

  def html(template: String, data: SoyMapData, inject: SoyMapData): Html =
    Html(render(template, data, inject))
}
