package com.github.wumo

import com.github.wumo.console.ArgParser
import java.io.File

fun main(args: Array<String>) {
  try {
    
    val requiredPackages = Online.dependenciesOf(args.toList())
    
    val content = buildString {
      requiredPackages.forEach {
        appendln("\"${it.url}\"")
      }
      appendln()
      requiredPackages.forEach {
        appendln("\"${it.sha256}\"")
      }
    }
    File("packages.txt").writeText(content)
    println("results have been written to packages.txt.")
  } catch(e: Exception) {
    println(e.message)
  }
}