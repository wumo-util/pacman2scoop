package com.github.wumo.pacman2scoop

import com.github.wumo.http.HttpClient.client
import com.github.wumo.http.OkHttpUtils.download
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths

object Download {
  fun decompress(inFile: String, outDir: String) {
    BufferedInputStream(FileInputStream(inFile)).use { fileInputStream->
      val input = when {
        inFile.endsWith("tar.xz")  ->
          TarArchiveInputStream(XZCompressorInputStream(fileInputStream))
        inFile.endsWith("tar.zst") ->
          TarArchiveInputStream(ZstdCompressorInputStream(fileInputStream))
        else                       -> error("$inFile is not supported")
      }
      input.use {
        while(true) {
          val entry = input.nextEntry ?: break
          if(!input.canReadEntryData(entry)) continue
          val outFile = Paths.get(outDir, entry.name).toFile()
          if(entry.isDirectory)
            check(outFile.isDirectory || outFile.mkdirs()) { "failed to create directory $outFile" }
          else {
            val parent = outFile.parentFile
            check(parent.isDirectory || parent.mkdirs()) { "failed to create direcoty $parent" }
            BufferedOutputStream(outFile.outputStream()).use { output->
              input.copyTo(output)
            }
          }
        }
      }
    }
  }
  
  fun download(requiredPackages: List<Package>, downloadDir: String) {
    File(downloadDir).mkdirs()
    runBlocking {
      requiredPackages.forEach { pkg->
        val u = pkg.url.toHttpUrl()
        val fileName = u.pathSegments.last()
        val file = Paths.get(downloadDir, fileName).toFile()
        if(!file.exists() || file.sha256() != pkg.sha256) {
          println("downloading ${pkg.url}")
          client.download(file, pkg.url)
          check(file.sha256() == pkg.sha256) { "$file checksum failed" }
        }
        println("decompress $fileName")
        decompress(file.toString(), downloadDir)
      }
    }
  }
  
  private fun File.sha256() = inputStream().use { DigestUtils.sha256Hex(it) }
}