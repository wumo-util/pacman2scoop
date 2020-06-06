package com.github.wumo.pacman2scoop

import com.github.wumo.http.HttpClient
import kotlin.test.Test

class TestDownload {
  
  @Test
  fun test() {
    Download.decompress("mingw-w64-x86_64-gcc-10.1.0-3-any.pkg.tar.zst", "out")
  }
  
  @Test
  fun testDownload() {
    HttpClient.initClient()
    Download.download(
      listOf(
        Package(
          "mingw-w64-x86_64-gcc",
          "http://repo.msys2.org/mingw/x86_64/mingw-w64-x86_64-gcc-10.1.0-3-any.pkg.tar.zst?repo=mingw64",
          "e48cd52bc88f67f153b5dff5050cb9aba10c4eb3ba519316e9f40101a1616cbe"
        )
      ),
      "out"
    )
    HttpClient.closeClient()
  }
}