package com.github.wumo.pacman2scoop

import com.github.wumo.http.HttpClient.closeClient
import com.github.wumo.http.HttpClient.initClient
import java.io.File

fun main(args: Array<String>) {
  try {
    val download = args.size > 0 && args[0] == "-d"
    val downloadDir = if(download) args[1] else ""
    val pkgs = if(download) args.drop(2) else args.toList()
    
    initClient()
    
    val requiredPackages = Online.dependenciesOf(pkgs)
    
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
    
    if(download)
      Download.download(requiredPackages, downloadDir)
  } catch(e: Exception) {
    println(e.message)
  } finally {
    closeClient()
  }
}