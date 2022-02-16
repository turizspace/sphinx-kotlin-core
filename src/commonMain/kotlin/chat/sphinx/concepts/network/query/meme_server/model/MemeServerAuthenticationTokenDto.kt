package chat.sphinx.concepts.network.query.meme_server.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MemeServerAuthenticationTokenDto(
    val token: String,
)
