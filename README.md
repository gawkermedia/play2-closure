# Play! 2.0 plugin for Google Closure Templates

This plugin is designed for using Google Closure Templates with Play! 2.0.3.

## Prerequisites

The latest Google Closure Templates jar from google: http://code.google.com/p/closure-templates/

## Install

1. Clone repo
2. Cd into play2-closure directory
3. `play publish-local`
4. Add this plugin to your application as a dependency: `"com.kinja" %% "play2-closure" % "0.1-SNAPSHOT"`

## Usage

Import the plugin's helper:

`import com.kinja.play.plugins.Closure`

Render a template:

`Closure.html("com.example.index.soy", Map("hello" -> "world"))`
