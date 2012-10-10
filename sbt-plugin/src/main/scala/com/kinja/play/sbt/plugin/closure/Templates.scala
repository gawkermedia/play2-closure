package com.kinja.play.sbt.plugin.closure

import sbt._
import sbt.Keys._

import com.google.template.soy.SoyFileSet
import com.google.template.soy.msgs.SoyMsgBundle
import com.google.template.soy.msgs.SoyMsgBundleHandler
import com.google.template.soy.xliffmsgplugin.XliffMsgPlugin
import com.google.template.soy.jssrc.SoyJsSrcOptions

object SoyTemplates {

	import SbtSoy.SoyKeys._

	protected def doCompile(source: File, locale: String, localeDir: File): String = {
		val fileSet = new SoyFileSet.Builder()
		val bundleHandler = new SoyMsgBundleHandler(new XliffMsgPlugin());
		val bundle = bundleHandler.createFromFile(new java.io.File(localeDir, locale + ".xlf"))
		val option = new SoyJsSrcOptions()
		fileSet
			.add(source)
			.build()
			.compileToJsSrc(option, bundle).get(0)
	}

	def compile(source: File, locale: String, localeDir: File): (String, Option[String], Seq[File]) = {
		val out = doCompile(source, locale, localeDir)
		(out, Some(out), Seq())
	}

	val Compiler = (ext: String, src: File, resources: File, jsTargetPattern: String, cache: File, files: PathFinder, locales: Seq[String], localeDir: File) => {
		import java.io._
		val cacheFile = cache / ext
		val currentInfos = (src ** ("*." + ext)).get.map(f => f -> FileInfo.lastModified(f)).toMap
		val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)

		if (previousInfo != currentInfos) {

			// Delete previous generated files
			previousRelation._2s.foreach(IO.delete)

			val generated = (files x relativeTo(Seq(src / "closure"))).flatMap {
				case (sourceFile, name) => {
					for (locale <- locales) yield {
						val (debug, min, dependencies) = compile(sourceFile, locale, localeDir)
						val out = new File(resources, jsTargetPattern.replace("{LOCALE}", locale) + "/" + name.replace("." + ext, ".js"))
						//val outMin = new File(resources, "public/javascripts/generated/closure/" + naming(name, true))
						IO.write(out, debug)
						val resFile = new File(resources, sourceFile.getAbsolutePath.substring(src.getAbsolutePath.length).substring(1))
						IO.copyFile(sourceFile, resFile)
						(resFile, out)
					}
				}
			}

			val listFile = new File(resources, "closure_templates.txt")
			IO.write(listFile, generated.map(_._1).distinct.toList.map(
				_.getAbsolutePath.substring(resources.getAbsolutePath.length)).mkString(IO.Newline))

			Sync.writeInfo(cacheFile,
				Relation.empty[File, File] ++ generated ++ List((listFile, listFile)),
				currentInfos)(FileInfo.lastModified.format)

			// Return new files
			generated.map(_._1).distinct.toList ++ generated.map(_._2).distinct.toList ++ List(listFile)
		} else {
			// Return previously generated files
			previousRelation._1s.toSeq ++ previousRelation._2s.toSeq
		}
	}
}

object SbtSoy extends Plugin {

	import SoyKeys._

	def soySettings: Seq[Setting[_]] = {
		Seq(
			soyDirectory in Compile <<= (baseDirectory)(_ / "app" / "views"),
			soyDirectory in Test <<= (baseDirectory)(_ / "test" / "views"),

			soyLocaleDirectory in Compile <<= (baseDirectory)(_ / "app" / "locales"),
			soyLocaleDirectory in Test <<= (baseDirectory)(_ / "test" / "locales"),

			soyJsDirectory in Compile <<= resourceManaged in Compile,
			soyJsDirectory in Test <<= resourceManaged in Test,

			soyJsDirectorySuffix := "public/javascripts/templates/closure/{LOCALE}",

			unmanagedResourceDirectories in Compile <++= Seq(soyLocaleDirectory in Compile).join,
			unmanagedResourceDirectories in Test <++= Seq(soyLocaleDirectory in Test).join,

			soyExtension := "soy",
			soyLocales := Seq("en_US", "hu_HU", "es_ES"),

			soyEntryPoints in Compile <<= (soyDirectory in Compile, soyExtension)((base, ext) => base / "closure" ** ("*." + ext)),
			soyEntryPoints in Test <<= (soyDirectory in Test, soyExtension)((base, ext) => base / "closure" ** ("*." + ext)),

			soyCompiler in Compile <<= (soyExtension, soyDirectory in Compile, soyJsDirectory in Compile, soyJsDirectorySuffix, cacheDirectory, soyEntryPoints in
				Compile, soyLocales, soyLocaleDirectory in Compile) map SoyTemplates.Compiler,

			soyCompiler in Test <<= (soyExtension, soyDirectory in Test, soyJsDirectory in Test, soyJsDirectorySuffix, cacheDirectory, soyEntryPoints in
				Test, soyLocales, soyLocaleDirectory in Test) map SoyTemplates.Compiler)
	}

	object SoyKeys {
		val soyDirectory = SettingKey[File]("soy-directory", "Default directory containing .soy templates.")
		val soyLocaleDirectory = SettingKey[File]("soy-locale-directory", "Default directory containing .xlf message files.")
		val soyJsDirectory = SettingKey[File]("soy-js-directory", "Base directory for generated javascripts.")
		val soyJsDirectorySuffix = SettingKey[String](
			"soy-js-directory-suffix", "The final directory for generated javascripts: soyJsDirectory / soyJsDirectorySuffix")
		val soyEntryPoints = SettingKey[PathFinder]("soy-entry-points")
		val soyLocales = SettingKey[Seq[String]]("soy-locales")
		val soyOptions = SettingKey[Seq[String]]("soy-options")
		val soyCompiler = TaskKey[Seq[java.io.File]]("soy-compiler")
		val soyExtension = SettingKey[String]("soy-extension")
	}

}
