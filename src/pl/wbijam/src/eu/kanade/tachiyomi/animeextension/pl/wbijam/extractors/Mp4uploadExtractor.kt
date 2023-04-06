package eu.kanade.tachiyomi.animeextension.pl.wbijam.extractors

import dev.datlag.jsunpacker.JsUnpacker
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.network.GET
import okhttp3.Headers
import okhttp3.OkHttpClient

class Mp4uploadExtractor(private val client: OkHttpClient) {
    fun getVideoFromUrl(url: String, headers: Headers, suffix: String = ""): List<Video> {
        val body = client.newCall(GET(url, headers = headers)).execute().body.string()

        val packed = body.substringAfter("<script type='text/javascript'>eval(function(p,a,c,k,e,d)")
            .substringBefore("</script>")
        val unpacked = JsUnpacker.unpackAndCombine("eval(function(p,a,c,k,e,d)" + packed) ?: return emptyList()
        val qualityRegex = """\WHEIGHT=(\d+)""".toRegex()
        val videoQuality = qualityRegex.find(unpacked)?.groupValues?.let { "${it[1]}p" } ?: "Unknown resolution"

        val videoUrl = unpacked.substringAfter("player.src(\"").substringBefore("\");")
        return listOf(
            Video(videoUrl, "Mp4upload - $videoQuality$suffix", videoUrl, headers = Headers.headersOf("Referer", "https://www.mp4upload.com/")),
        )
    }
}