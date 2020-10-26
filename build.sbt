name := "zio-demo"

version := "0.1"

scalaVersion := "2.13.3"

val zioConfigVersion = "1.0.0-RC29"
libraryDependencies += "dev.zio" %% "zio-config" % zioConfigVersion
libraryDependencies += "dev.zio" %% "zio-config-magnolia" % zioConfigVersion
libraryDependencies += "dev.zio" %% "zio-config-refined" % zioConfigVersion
libraryDependencies += "dev.zio" %% "zio-config-typesafe" % zioConfigVersion

