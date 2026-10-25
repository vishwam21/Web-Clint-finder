package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Clients ---
    @Query("SELECT * FROM clients ORDER BY createdAt DESC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClientById(id: Long): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>): List<Long>

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun deleteClientById(id: Long)

    @Query("DELETE FROM clients")
    suspend fun deleteAllClients()

    @Query("UPDATE clients SET isOptedOut = :optedOut WHERE id = :id")
    suspend fun setClientOptOut(id: Long, optedOut: Boolean)

    @Query("UPDATE clients SET status = :status, lastContactDate = :timestamp WHERE id = :id")
    suspend fun updateClientStatus(id: Long, status: String, timestamp: Long?)

    // --- Messages ---
    @Query("SELECT * FROM messages ORDER BY updatedAt DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE clientId = :clientId ORDER BY updatedAt DESC")
    fun getMessagesForClient(clientId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Long): MessageEntity?

    @Query("SELECT * FROM messages WHERE status = 'PENDING_REVIEW' ORDER BY updatedAt DESC")
    fun getPendingReviewMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE status = 'APPROVED' ORDER BY updatedAt DESC")
    fun getApprovedMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Query("UPDATE messages SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateMessageStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE messages SET status = 'SENT', sentAt = :sentAt, updatedAt = :sentAt WHERE id = :id")
    suspend fun markMessageSent(id: Long, sentAt: Long = System.currentTimeMillis())

    @Query("UPDATE messages SET body = :body, subject = :subject, status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun editMessageContent(id: Long, body: String, subject: String, status: String, updatedAt: Long = System.currentTimeMillis())
}
