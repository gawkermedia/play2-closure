package com.kinja.play.plugins

import play.api._
import play.api.templates._
import play.api.Configuration._
import play.api.Play.current

import com.google.template.soy.SoyFileSet
import com.google.template.soy.data.SoyListData
import com.google.template.soy.data.SoyMapData
import com.google.template.soy.tofu.SoyTofu
import com.google.template.soy.msgs.SoyMsgBundle
import com.google.template.soy.msgs.SoyMsgBundleHandler
import com.google.template.soy.xliffmsgplugin.XliffMsgPlugin

import java.io.File

/**
 * Play plugin for Closure.
 */
class ClosurePlugin(app: Application) extends Plugin {

	lazy val engine = newEngine.build

	def newEngine: ClosureEngine = ClosureEngine(app.mode)

	/**
	 * If the app is running in production mode then always returns a same engine instance, otherwise returns a brand new instance.
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
		!app.configuration.getString("closureplugin").filter(_ == "disabled").isDefined
	}
}

/**
 * Closure Template API
 *
 * @param sourceDirectories List of directories where you store your templates
 */
class ClosureEngine(val sourceDirectories: Traversable[File]) {

	protected var DEFAULT_LOCALE = "en_US"

	protected var locale = DEFAULT_LOCALE

	/**
	 * The current compiled templates.
	 */
	protected lazy val tofu: SoyTofu = builder

	/**
	 * List of template files.
	 */
	protected lazy val files = fileList

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
			case l: Long => sl.add(l)
			case i: Int => sl.add(i)
			case a: AnyRef if a != null => sl.add(mapToSoyData(getCCParams(a)))
			case None =>
			case null =>
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
			case l: Long => sm.put(k, l)
			case i: Int => sm.put(k, i)
			case a: AnyRef if a != null => sm.put(k, mapToSoyData(getCCParams(a)))
			case None =>
			case null =>
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

	protected def recursiveListFiles(f: File, extension: String = ""): Array[File] = {
		val these = f.listFiles
		these.filter(_.getName.endsWith(extension)) ++ these.filter(_.isDirectory).flatMap(recursiveListFiles(_, extension))
	}

	/**
	 * Returns all soy files from source directories.
	 */
	protected def fileList = sourceDirectories.flatMap(recursiveListFiles(_, ".soy"))

	/**
	 * Helper.
	 *
	 * Creates a tofu instance and returns an engine instance.
	 */
	def build: ClosureEngine = {
		tofu
		this
	}

	def getLocale = locale

	def setLocale(name: String): Unit = {
		locale = name
	}

	/**
	 * Add all input files to the builder.
	 *
	 * @param input List of template files
	 */
	def fileSet(input: Traversable[File]): SoyFileSet.Builder = {
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

	def msgBundle: Option[SoyMsgBundle] = {
		if (locale != DEFAULT_LOCALE) {
			val bundleHandler = new SoyMsgBundleHandler(new XliffMsgPlugin());
			val bundle = bundleHandler.createFromFile(Play.getFile("app/locales/" + locale + ".xlf"));
			Some(bundle)
		} else {
			None
		}
	}

	/**
	 * Creates a new Renderer for a template without any msgbundle.
	 *
	 * @param template  The name of the template to render.
	 * 					You can use names like "closuretest.index.soy", the .soy extension will be removed.
	 */
	def newRenderer(template: String): SoyTofu.Renderer =
		tofu.newRenderer(template.replace(".soy", ""))

	/**
	 * Creates a new Renderer for a template with an opitional msgbundle.
	 *
	 * @param template  The name of the template to render.
	 * 					You can use names like "closuretest.index.soy", the .soy extension will be removed.
	 */
	def renderer(template: String): SoyTofu.Renderer = {
		msgBundle match {
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
	def render(template: String, data: Map[String, Any]): String =
		renderer(template).setData(mapToSoyData(data)).render()

	/**
	 *  Renders a template.
	 *
	 * @param template The name of the template to render.
	 * @param data The data to call the template with.
	 */
	def render(template: String, data: SoyMapData): String =
		renderer(template).setData(data).render()
}

object ClosureEngine {

	/**
	 * Creates a new engine by mode.
	 */
	def apply(mode: Mode.Mode): ClosureEngine = if (mode == Mode.Test) {
		apply("test/views")
	} else {
		apply("public/views")
	}

	/**
	 * Creates a new engine.
	 *
	 * @param rootDir Root directory of template files.
	 */
	def apply(rootDir: String): ClosureEngine = new ClosureEngine(List(Play.getFile(rootDir)))

}

/**
 * Helper object
 */
object Closure {

	private def plugin = play.api.Play.maybeApplication.map { app =>
		app.plugin[ClosurePlugin].getOrElse(throw new RuntimeException("you should enable ClosurePlugin in play.plugins"))
	}.getOrElse(throw new RuntimeException("you should have a running app in scope a this point"))

	def getLocale: String = plugin.api.getLocale

	def setLocale(locale: String): Unit = plugin.api.setLocale(locale)

	// PUBLIC INTERFACE
	def render(template: String, data: Map[String, Any] = Map()): String = plugin.api.render(template, data)

	def render(template: String, data: SoyMapData): String = plugin.api.render(template, data)

	def html(template: String, data: Map[String, Any] = Map()): Html = Html(render(template, data))

	def html(template: String, data: SoyMapData): Html = Html(render(template, data))

}
