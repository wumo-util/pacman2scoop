package com.github.wumo.pacman2scoop

import com.github.wumo.console.ArgParser
import com.github.wumo.http.HttpClient.closeClient
import com.github.wumo.http.HttpClient.initClient
import java.io.File

fun main(args: Array<String>) {
  try {
    val options = object: ArgParser() {
      val dir: String? by option("d", "download", description = "download packages to directory")
      val packages: List<String> by option("p", "package", description = "msys2 packages to search")
    }
    options.parse(args)
    
    initClient()
    
    val requiredPackages = Online.dependenciesOf(options.packages)
    
    val content = buildString {
      appendln("\"url\": [")
      appendln(requiredPackages.joinToString(",\n") { "\"${it.url}\"" })
      appendln("],")
      appendln("\"hash\": [")
      appendln(requiredPackages.joinToString(",\n") { "\"${it.sha256}\"" })
      appendln("]")
    }
    File("packages.txt").writeText(content)
    println("packages' dependencies info has been written to packages.txt.")
    
    options.dir?.also {
      Download.download(requiredPackages, it)
    }
  } catch(e: Exception) {
    println(e.message)
  } finally {
    closeClient()
  }
}