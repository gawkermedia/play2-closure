# Play! 2.1 plugin for Google Closure Templates

This plugin is designed for using Google Closure Templates with Play! 2.1

## Prerequisites

The latest Google Closure Templates jar from google: http://code.google.com/p/closure-templates/

Install the latest sbt-closure-templates plugin: https://github.com/gawkermedia/sbt-closure-templates

## Install

1. Clone repo
2. Cd into play2-closure directory
3. `play publish-local`
4. Add this plugin to your application as a dependency: `"com.kinja.play" %% "play2-closure" % "0.9-SNAPSHOT"`

## Usage

Import the plugin's helper:

```scala
import com.kinja.play.plugins.Closure
```

Render a template:

```scala
Closure.html("com.example.index.soy", Map("hello" -> "world"))
```

Set the current locale:

```scala
Closure.html("com.example.index", Map(
  Closure.KEY_LOCALE -> "hu_HU", // ISO language code: four letter with underscore
  "hello" -> "world"))
```

The dictionary file pattern is `app/locale/{$locale}.xlf`
