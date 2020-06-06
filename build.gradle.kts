plugins {
  kotlin("jvm") version "1.3.72"
  application
  kotlin("plugin.serialization") version "1.3.72"
  id("org.beryx.runtime") version "1.8.5"
  id("com.google.osdetector") version "1.6.2"
  id("com.github.wumo.graalvm") version "0.0.5"
}

group = "com.github.wumo"
version = "0.0.2"

repositories {
  mavenCentral()
  maven(url = "https://jitpack.io")
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation("com.github.wumo:common-utils:1.0.5")
  implementation("com.github.wumo:http-stack:1.0.2")
  implementation("org.apache.commons:commons-compress:1.20")
  implementation("com.github.luben:zstd-jni:1.4.4-7")
  implementation("org.tukaani:xz:1.8")
  implementation("commons-codec:commons-codec:1.14")
  
  testImplementation("junit:junit:4.13")
  testImplementation(kotlin("test-junit"))
}

application {
  mainClassName = "com.github.wumo.pacman2scoop.MainKt"
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
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

graalvm {
  graalvmHome = System.getenv("GRAALVM_HOME")
  mainClassName = "com.github.wumo.pacman2scoop.MainKt"
  val res = when(osdetector.os) {
    "osx"   -> "darwin[.]x86_64"
    "linux"   -> {
      when(osdetector.arch) {
        "x86_32" -> "linux/i386"
        "x86_64" -> "linux/amd64"
        else     -> error("Not supported ${osdetector.arch}")
      }
    }
    "windows" -> {
      when(osdetector.arch) {
        "x86_32" -> "win/x86"
        "x86_64" -> "win/amd64"
        else     -> error("Not supported ${osdetector.arch}")
      }
    }
    else      -> error("Not supported ${osdetector.os}")
  }
  arguments = listOf(
    "--no-fallback",
    "--enable-all-security-services",
    "--report-unsupported-elements-at-runtime",
    "--allow-incomplete-classpath",
    "-H:IncludeResources=$res/.*",
    "-H:JNIConfigurationResources=graalvm-jni.json"
  )
}