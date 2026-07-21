package com.aiaggregator.app.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0006\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\t2\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u001c\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\r\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\u000fJ\u0016\u0010\u0010\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\u000f\u00a8\u0006\u0011"}, d2 = {"Lcom/aiaggregator/app/data/MessageDao;", "", "deleteLastAssistantMessage", "", "conversationId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteMessagesByConversation", "getMessagesByConversation", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/aiaggregator/app/data/MessageEntity;", "getMessagesByConversationSync", "insertMessage", "message", "(Lcom/aiaggregator/app/data/MessageEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateMessage", "app_debug"})
@androidx.room.Dao
public abstract interface MessageDao {
    
    @androidx.room.Query(value = "SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.aiaggregator.app.data.MessageEntity>> getMessagesByConversation(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId);
    
    @androidx.room.Query(value = "SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getMessagesByConversationSync(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.aiaggregator.app.data.MessageEntity>> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertMessage(@org.jetbrains.annotations.NotNull
    com.aiaggregator.app.data.MessageEntity message, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Update
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object updateMessage(@org.jetbrains.annotations.NotNull
    com.aiaggregator.app.data.MessageEntity message, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM messages WHERE conversation_id = :conversationId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteMessagesByConversation(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM messages WHERE conversation_id = :conversationId AND role = \'assistant\' AND id = (SELECT MAX(id) FROM messages WHERE conversation_id = :conversationId AND role = \'assistant\')")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteLastAssistantMessage(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}