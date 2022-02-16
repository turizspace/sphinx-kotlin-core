package chat.sphinx.concepts.network.query.message.model

import chat.sphinx.wrapper.dashboard.ChatId
import chat.sphinx.wrapper.dashboard.ContactId
import chat.sphinx.wrapper.dashboard.toChatId
import chat.sphinx.wrapper.dashboard.toContactId
import chat.sphinx.wrapper.message.SenderAlias
import chat.sphinx.wrapper.message.toSenderAlias
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class TransactionDto(
    val id: Long,
    val chat_id: Long?,
    val type: Int,
    val sender: Long,
    val sender_alias: String?,
    val receiver: Long?,
    val amount: Long,
    val payment_hash: String?,
    val payment_request: String?,
    val date: String,
    val reply_uuid: String?,
) {

    fun isIncomingWithSender(ownerId: ContactId): Boolean {
        return sender != ownerId.value
    }

    fun isOutgoingWithReceiver(ownerId: ContactId): Boolean {
        return receiver != null && sender == ownerId.value
    }

    fun isOutgoingMessageBoost(ownerId: ContactId): Boolean {
        return reply_uuid != null && sender == ownerId.value
    }

    fun isPaymentInChat(): Boolean {
        return chat_id != null
    }

    fun getSenderId(): ContactId? {
        return sender.toContactId()
    }

    fun getSenderAlias(): SenderAlias? {
        return sender_alias?.toSenderAlias()
    }

    fun getReceiverId(): ContactId? {
        return receiver?.toContactId()
    }

    fun getChatId(): ChatId? {
        return chat_id?.toChatId()
    }
}