package chat.sphinx.features.network.query.message.model

import chat.sphinx.concepts.network.relay_call.RelayResponse
import kotlinx.serialization.Serializable

@Serializable
internal data class ReadMessagesRelayResponse(
    override val success: Boolean,
    override val response: Any?,
    override val error: String?
) : RelayResponse<Any>()