package com.github.wumo

import java.io.File

fun main(args: Array<String>) {
  try {
    
    val requiredPackages = Online.dependenciesOf(args.toList())
    
    val content = buildString {
      appendln("\"url\": [")
      appendln(requiredPackages.joinToString(",\n") { "\"${it.url}\"" })
      appendln("],")
      appendln("\"hash\": [")
      appendln(requiredPackages.joinToString(",\n") { "\"${it.sha256}\"" })
      appendln("]")
    }
    File("packages.txt").writeText(content)
    println("results have been written to packages.txt.")
  } catch(e: Exception) {
    println(e.message)
  }
}