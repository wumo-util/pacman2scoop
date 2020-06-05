plugins {
  kotlin("jvm") version "1.3.72"
  application
  kotlin("plugin.serialization") version "1.3.72"
  id("com.github.johnrengelman.shadow") version "5.2.0"
  id("org.beryx.runtime") version "1.8.5"
  id("com.google.osdetector") version "1.6.2"
  id("org.mikeneck.graalvm-native-image") version "0.5.0"
}

group = "com.github.wumo"
version = "0.0.1"

repositories {
  mavenCentral()
  maven(url = "https://jitpack.io")
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation("com.github.wumo:common-utils:1.0.5")
  implementation("com.github.wumo:http-stack:1.0.2")
}

application {
  mainClassName = "com.github.wumo.MainKt"
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  
  jpackage {
  
  }
  
  register<Zip>("packageDist") {
    dependsOn(jpackage)
    val fileName = "${project.name}-${project.version}-${osdetector.classifier}.${archiveExtension.get()}"
    archiveFileName.set(fileName)
    destinationDirectory.set(file("$buildDir/dist"))
    from("$buildDir/jpackage")
  }
}

runtime {
  addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
  addModules("java.logging")
  
  jpackage {
    imageName = "pacman2scoop"
    appVersion = project.version.toString()
    installerOptions = listOf("--app-version", version.toString())
    
    skipInstaller = true
    
    if(osdetector.os == "windows") {
//      installerType = "msi"
      imageOptions = listOf("--win-console")
      installerOptions =
        installerOptions + listOf("--win-per-user-install", "--win-dir-chooser", "--win-menu", "--win-shortcut")
    }
  }
}

nativeImage {
  println(System.getenv("JAVA_HOME"))
  setGraalVmHome(System.getenv("JAVA_HOME"))
  setMainClass("com.github.wumo.MainKt")
  setExecutableName("pacman2scoop")
  arguments(
    "--no-fallback",
    "--enable-all-security-services",
    "--initialize-at-run-time=com.example.runtime",
    "--report-unsupported-elements-at-runtime",
    "--allow-incomplete-classpath"
  )
}