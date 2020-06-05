package com.github.wumo

import com.github.wumo.process.ProcessHelper
import java.nio.file.Paths

object Local {
  
  val defaultInstallation = "C:\\msys64"
  val shellCMD = "msys2_shell.cmd"
  
  fun searchMsy2(): String {
    val file = Paths.get(defaultInstallation, shellCMD).toFile()
    if(file.exists()) {
      return file.absolutePath
    }
    //search in Path
    val path = System.getenv("Path")
    for(p in path.split(';')) {
      val f = Paths.get(p, shellCMD).toFile()
      if(f.exists())
        return f.absolutePath
    }
    error("msys2 not found!")
  }
  
  fun String.exec(cmd: String): String {
    val (result, exitCode) = ProcessHelper.eval("\"$this\" -defterm -no-start -full-path -here -c \"$cmd\"")
    check(exitCode == 0) { result }
    return result
  }
}