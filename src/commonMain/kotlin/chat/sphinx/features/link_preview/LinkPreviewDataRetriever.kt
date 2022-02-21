package chat.sphinx.features.link_preview


import chat.sphinx.concepts.coroutines.CoroutineDispatchers
import chat.sphinx.concepts.link_preview.model.*
import chat.sphinx.concepts.network.query.chat.NetworkQueryChat
import chat.sphinx.concepts.socket_io.SphinxSocketIOMessage
import chat.sphinx.features.link_preview.util.getDescription
import chat.sphinx.features.link_preview.util.getFavIconUrl
import chat.sphinx.features.link_preview.util.getImageUrl
import chat.sphinx.features.link_preview.util.getTitle
import chat.sphinx.response.LoadResponse
import chat.sphinx.response.Response
import chat.sphinx.wrapper.chat.ChatHost
import chat.sphinx.wrapper.chat.ChatUUID
import chat.sphinx.wrapper.tribe.TribeJoinLink
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.jvm.Volatile

internal sealed interface LinkPreviewDataRetriever

internal data class HtmlPreviewDataRetriever(val url: Url): LinkPreviewDataRetriever {
    private val lock = Mutex()

    @Volatile
    private var previewData: HtmlPreviewData? = null

    suspend fun getHtmlPreview(
        dispatchers: CoroutineDispatchers,
        okHttpClient: HttpClient
    ): HtmlPreviewData? =
        previewData ?: lock.withLock {
            previewData ?: retrievePreview(
                dispatchers = dispatchers,
                okHttpClient = okHttpClient,
            ).also {
                if (it != null) {
                    previewData = it
                }
            }
        }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun retrievePreview(
        dispatchers: CoroutineDispatchers,
        okHttpClient: HttpClient
    ): HtmlPreviewData? {
        val request = SphinxSocketIOMessage.Type.Group.Member.Request.Builder().url(url).build()
        var response: Response?

        withContext(dispatchers.io) {
            response =
                try {
                    okHttpClient.newCall(request).execute()
                } catch (e: Exception) {
                    null
                }

            if (response?.isSuccessful == false) {
                response?.body?.closeQuietly()
            }
        }

        if (response == null || response?.isSuccessful == false) {
            return null
        }

        return response?.body?.source()?.inputStream()?.let { stream ->

            try {
                withContext(dispatchers.default) {

                    val document: Document = Jsoup.parse(
                        /* in */            stream,
                        /* charsetName */   null,
                        /* baseUri */       url.toString(),
                    )

                    HtmlPreviewData(
                        document.getTitle()?.toHtmlPreviewTitleOrNull(),
                        HtmlPreviewDomainHost(url.host),
                        document.getDescription()?.toPreviewDescriptionOrNull(),
                        document.getImageUrl()?.toPreviewImageUrlOrNull(),
                        document.getFavIconUrl()?.toHtmlPreviewFavIconUrlOrNull(),
                    )
                }
            } catch (e: Exception) {
                null
            } finally {
                stream.closeQuietly()
            }
        }
    }
}

internal class TribePreviewDataRetriever(val tribeJoinLink: TribeJoinLink): LinkPreviewDataRetriever {
    private val lock = Mutex()

    @Volatile
    private var previewData: TribePreviewData? = null

    suspend fun getTribePreview(networkQueryChat: NetworkQueryChat): TribePreviewData? =
        previewData ?: lock.withLock {
            previewData ?: retrievePreview(networkQueryChat)
                .also {
                    if (it != null) {
                        previewData = it
                    }
                }

        }

    private suspend fun retrievePreview(networkQueryChat: NetworkQueryChat): TribePreviewData? {

        var data: TribePreviewData? = null

        networkQueryChat.getTribeInfo(
            ChatHost(tribeJoinLink.tribeHost),
            ChatUUID(tribeJoinLink.tribeUUID)
        ).collect { response ->
            Exhaustive@
            when (response) {
                is LoadResponse.Loading -> {}
                is Response.Error -> {}
                is Response.Success -> {
                    data = TribePreviewData(
                        TribePreviewName(response.value.name),
                        response.value.description.toPreviewDescriptionOrNull(),
                        response.value.img?.toPreviewImageUrlOrNull(),
                    )
                }
            }
        }

        return data
    }
}
