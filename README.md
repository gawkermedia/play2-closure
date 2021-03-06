# Play! 2.4.x plugin for Google Closure Templates

This plugin is designed for using Google Closure Templates with Play! 2.4.x

## Installation

1. Clone the repository

2. Go into play2-closure directory and execute `sbt publishLocal`

3. Add the plugin to your application as a dependency:
    ```scala
    libraryDependencies += "com.kinja.play" %% "play2-closure" % "1.0-2.4.11-SNAPSHOT"
    ```

4. Enable the plugin in `conf/play.plugins`:
    ```
    1600:com.kinja.play.plugins.ClosurePlugin
    ```

5. Configure the build number in `conf/application.conf`:
    ```
    buildNumber="1"
    ```

6. Create at least one soy template file in `app/views/closure`.


## Usage

### Rendering templates

The plugins supports [Scala data structures for Google Closure Templates](https://github.com/gawkermedia/soy) which is
the preferred way of passing data to the plugin for rendering.

Rendering a template is as simple as calling `Closure.render` with the template name and the data to be passed.

```scala
import com.kinja.play.plugins.Closure
import com.kinja.soy._

val rendered: String = Closure.render("com.example.index.soy", Soy.map("hello" -> "world"))
```

The template files should be placed in `app/views/closure` in dev mode and `test/views/closure` for tests.

### Packaging the template files with the application

Template files are not automatically packaged into the output JAR, you need to map them manually.
You can achieve this with the `mappings` SBT setting:

```scala
mappings in (Compile, packageBin) ++= {
  val app = baseDirectory.value / "app"
  ((app / "views" / "closure") ** "*.soy").get pair rebase(app, "app")
}
```

For alternative ways to provide the plugin with the template files (for example when deploying the
templates separately from app), see the [Templates in production](#templates-in-production) section below.

### Setting the locale

```scala
Closure.html("com.example.index", Soy.map(
  "locale" -> "hu_HU", // ISO language code: four letter with underscore
  "hello" -> "world"))
```

The dictionary file pattern is `app/locales/{$locale}.xlf`

### Templates in production

Normally, templates are loaded from the packaged application. However, the plugin allows hot swapping the templates
without restarting the Play application. This is useful to do quick template-only deploys. For this, in production
mode templates are attempted to be loaded from the following directory: `{assetPath}/{buildNumber}/closure`. If this
directory does not exists, templates are loaded from the application package. `assetPath` and `buildNumber` can be set in
Play configuration as keys `closureplugin.assetPath` and `buildNumber` respectively.

The recommended setup for hot-swapping is as follows:

Add this to conf/application-live.conf:

```
include "build-number.conf"
closureplugin.assetPath = "/path/to/hotswappable/assets/on/the/production/server"
```

The file `conf/build-number.conf` file should be added by the continuous integration tool (e.g. Jenkins) you use before
packaging the application to include the current build number of the application.

```
buildNumber = "8874"
```

Create an API call which can be used to bump the build number. Be careful not to make this publicly available, the best
would be to check the client IP and only allow localhost. The controller method should call `Closure.setVersion` to tell
the plugin where to read templates from. You should probably want to bump it elsewhere (like in a global var) if you
use it elsewhere in your application.

```scala
import play.api.mvc._
import com.kinja.play.plugins.Closure

object HotSwapController extends Controller {
	
	def hotswap(buildNumber: String) = Action {
		// implement security here!
		Closure.setVersion(buildNumber)
	}
}
```

The hot-swapping can now be implemented as a separate build job, which copies the templates to your productions servers
in the appropriate directory under a new build number and then calls the hotswap API call to set the new build number
on all production servers.

### Plugins

This plugin supports Closure Template plugins. Play2-closure takes a list of classpaths in `closureplugin.plugins`. For
example, to add the XliffMsgPlugin, you should add this to your application.conf:

```
closureplugin.plugins = ["com.google.template.soy.xliffmsgplugin.XliffMsgPlugin"]
```
