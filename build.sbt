enablePlugins(ScalaJSPlugin)

name := "tickTockToeJS"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.0"

skip in packageJSDependencies := false
jsDependencies +=
  "org.webjars" % "jquery" % "2.1.4" / "2.1.4/jquery.js"

jsDependencies += RuntimeDOM


libraryDependencies += "com.lihaoyi" %%% "utest" % "0.3.0" % "test"
testFrameworks += new TestFramework("utest.runner.Framework")




// core = essentials only. No bells or whistles.
libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "0.11.1"

// React JS itself (Note the filenames, adjust as needed, eg. to remove addons.)
jsDependencies ++= Seq(

  "org.webjars.bower" % "react" % "15.1.0" / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",

  "org.webjars.bower" % "react" % "15.1.0" /         "react-dom.js" minified  "react-dom.min.js" dependsOn "react-with-addons.js" commonJSName "ReactDOM",

  "org.webjars.bower" % "react" % "15.1.0" /         "react-dom-server.js" minified  "react-dom-server.min.js" dependsOn "react-dom.js" commonJSName "ReactDOMServer")