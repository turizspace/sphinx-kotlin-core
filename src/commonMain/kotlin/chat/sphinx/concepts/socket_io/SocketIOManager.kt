package chat.sphinx.concepts.socket_io

import chat.sphinx.response.Response
import chat.sphinx.response.ResponseError
import chat.sphinx.wrapper.relay.AuthorizationToken
import chat.sphinx.wrapper.relay.RelayUrl
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

abstract class SocketIOManager {
    abstract suspend fun connect(
        relayData: Pair<AuthorizationToken, RelayUrl>? = null
    ): Response<Any, ResponseError>

    abstract val isConnected: Boolean

    abstract fun disconnect()

    abstract val socketIOStateFlow: StateFlow<SocketIOState>
    abstract val socketIOErrorFlow: SharedFlow<SocketIOError>

    abstract fun addListener(listener: SphinxSocketIOMessageListener): Boolean
    abstract fun removeListener(listener: SphinxSocketIOMessageListener): Boolean
}
