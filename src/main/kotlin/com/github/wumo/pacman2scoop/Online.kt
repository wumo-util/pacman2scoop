package com.github.wumo.pacman2scoop

import com.github.wumo.http.HttpClient.client
import com.github.wumo.http.HttpClient.closeClient
import com.github.wumo.http.HttpClient.initClient
import com.github.wumo.http.OkHttpUtils.url
import com.github.wumo.http.OkHttpUtils.get
import kotlinx.coroutines.runBlocking

object Online {
  val baseUrl = url("https", "packages.msys2.org", "package")
  
  val fileUrlPattern = Regex("""<dt[^>]+>\s*File:\s*</dt>[\S\s]*?<a\s+href="([^"]+)"""")
  val sha256Pattern = Regex("""<dt[^>]+>\s*SHA256:\s*</dt>[\S\s]*?<code>([^<]+)</code>""")
  val dependenciesPattern = Regex("""<dt[^>]+>\s*Dependencies:\s*</dt>[\S\s]*?<dd[^>]+>([\S\s]+?)</dd>""")
  val dependencyPattern = Regex("""<a\s+href="([^"]+)"""")
  
  @OptIn(ExperimentalStdlibApi::class)
  fun dependenciesOf(requiredPackages: List<String>): List<Package> {
    val queue = requiredPackages.mapTo(ArrayList()) {
      baseUrl.url(it).toString()
    }
    
    return runBlocking {
      val packages = mutableMapOf<String, Package>()
      while(queue.isNotEmpty()) {
        val p = queue.removeLast()
        println("fetching $p")
        val result = client.get(p)
        var start: Int
        val fileUrlMatch = fileUrlPattern.find(result)!!
        start = fileUrlMatch.range.last
        val fileUrl = fileUrlMatch.groupValues[1]
        val sha256Match = sha256Pattern.find(result, start)!!
        val sha256 = sha256Match.groupValues[1]
        packages[p] = Package(fileUrl, fileUrl, sha256)
        start = sha256Match.range.last
        val dependenciesMatch = dependenciesPattern.find(result, start)!!
        val dependenciesStr = dependenciesMatch.groupValues[1]
        dependencyPattern.findAll(dependenciesStr).forEach {
          val d = it.groupValues[1]
          if(d !in packages)
            queue.add(d)
        }
      }
      val uniquePackagesByFileUrl = mutableMapOf<String, Package>()
      packages.values.forEach { uniquePackagesByFileUrl[it.url] = it }
      uniquePackagesByFileUrl.values.sortedBy { it.url }
    }
  }
}