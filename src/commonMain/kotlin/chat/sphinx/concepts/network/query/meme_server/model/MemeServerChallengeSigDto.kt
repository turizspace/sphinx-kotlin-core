package chat.sphinx.concepts.network.query.meme_server.model

import kotlinx.serialization.Serializable

@Serializable
data class MemeServerChallengeSigDto(
    val sig: String,
)
