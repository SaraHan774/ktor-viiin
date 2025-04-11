package chic.august.scraper

import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@Serializable
data class KocwLectureInfo(
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val cid: String,
    val lectures: List<KocwLecture>,
)

@Serializable
data class KocwLecture(
    val title: String,
    val vodUrl: String
)

object KocwScraper {
    fun scrapeLecture(cid: String): KocwLectureInfo {
        val url = "https://www.kocw.net/home/cview.do?cid=$cid"
        return try {
            val doc = Jsoup.connect(url).get()

            val title = doc.select("meta[property=og:title]").attr("content").ifBlank {
                doc.title()
            }

            val description = doc.select("meta[name=Description]").attr("content")
            val thumbnail = doc.select("meta[name=image]").attr("content")

            KocwLectureInfo(
                title = title,
                description = description,
                thumbnailUrl = thumbnail,
                cid = cid,
                lectures = extractLecturesFromVodList(doc)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            KocwLectureInfo(
                title = "알수없음",
                description = "알수없음",
                thumbnailUrl = "",
                cid = "",
                lectures = emptyList(),
            )
        }
    }

    private fun extractLecturesFromVodList(doc: Document): List<KocwLecture> {
        val lectures = mutableListOf<KocwLecture>()

        val aTags = doc.select("#vod_list a[id^=aTitle]")
        for (a in aTags) {
            val onclick = a.attr("onclick")
            val match = Regex("f_play\\('(.*?)','(.*?)','(.*?)'.*?'(.*?)'").find(onclick)

            if (match != null) {
                val (location, courseId, lectureId, title) = match.destructured
                val vodUrl =
                    "https://www.kocw.net/home/common/player/popVod.do?cour_cd=$courseId&lec_cd=$lectureId&vod_type=V&type=A&vod_url=$location"
                lectures.add(KocwLecture(title = title, vodUrl = vodUrl))
            }
        }
        return lectures
    }
}