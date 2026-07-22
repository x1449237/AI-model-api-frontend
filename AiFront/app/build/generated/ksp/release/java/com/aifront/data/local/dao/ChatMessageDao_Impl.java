package com.aifront.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.aifront.data.model.ChatMessage;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ChatMessageDao_Impl implements ChatMessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ChatMessage> __insertionAdapterOfChatMessage;

  private final EntityDeletionOrUpdateAdapter<ChatMessage> __deletionAdapterOfChatMessage;

  private final EntityDeletionOrUpdateAdapter<ChatMessage> __updateAdapterOfChatMessage;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMessagesByConversation;

  public ChatMessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfChatMessage = new EntityInsertionAdapter<ChatMessage>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `chat_messages` (`id`,`conversationId`,`role`,`content`,`modelId`,`modelName`,`vendorName`,`timestamp`,`tokensUsed`,`responseTimeMs`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatMessage entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getConversationId());
        statement.bindString(3, entity.getRole());
        statement.bindString(4, entity.getContent());
        if (entity.getModelId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getModelId());
        }
        if (entity.getModelName() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getModelName());
        }
        if (entity.getVendorName() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getVendorName());
        }
        statement.bindLong(8, entity.getTimestamp());
        statement.bindLong(9, entity.getTokensUsed());
        statement.bindLong(10, entity.getResponseTimeMs());
      }
    };
    this.__deletionAdapterOfChatMessage = new EntityDeletionOrUpdateAdapter<ChatMessage>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `chat_messages` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatMessage entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfChatMessage = new EntityDeletionOrUpdateAdapter<ChatMessage>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `chat_messages` SET `id` = ?,`conversationId` = ?,`role` = ?,`content` = ?,`modelId` = ?,`modelName` = ?,`vendorName` = ?,`timestamp` = ?,`tokensUsed` = ?,`responseTimeMs` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChatMessage entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getConversationId());
        statement.bindString(3, entity.getRole());
        statement.bindString(4, entity.getContent());
        if (entity.getModelId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getModelId());
        }
        if (entity.getModelName() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getModelName());
        }
        if (entity.getVendorName() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getVendorName());
        }
        statement.bindLong(8, entity.getTimestamp());
        statement.bindLong(9, entity.getTokensUsed());
        statement.bindLong(10, entity.getResponseTimeMs());
        statement.bindLong(11, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteMessagesByConversation = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM chat_messages WHERE conversationId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMessage(final ChatMessage message,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfChatMessage.insertAndReturnId(message);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMessages(final List<ChatMessage> messages,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfChatMessage.insert(messages);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessage(final ChatMessage message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfChatMessage.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessage(final ChatMessage message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfChatMessage.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessagesByConversation(final long conversationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMessagesByConversation.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, conversationId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteMessagesByConversation.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ChatMessage>> getMessagesByConversation(final long conversationId) {
    final String _sql = "SELECT * FROM chat_messages WHERE conversationId = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, conversationId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chat_messages"}, new Callable<List<ChatMessage>>() {
      @Override
      @NonNull
      public List<ChatMessage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "modelId");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorName");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfTokensUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "tokensUsed");
          final int _cursorIndexOfResponseTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "responseTimeMs");
          final List<ChatMessage> _result = new ArrayList<ChatMessage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatMessage _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpModelId;
            if (_cursor.isNull(_cursorIndexOfModelId)) {
              _tmpModelId = null;
            } else {
              _tmpModelId = _cursor.getString(_cursorIndexOfModelId);
            }
            final String _tmpModelName;
            if (_cursor.isNull(_cursorIndexOfModelName)) {
              _tmpModelName = null;
            } else {
              _tmpModelName = _cursor.getString(_cursorIndexOfModelName);
            }
            final String _tmpVendorName;
            if (_cursor.isNull(_cursorIndexOfVendorName)) {
              _tmpVendorName = null;
            } else {
              _tmpVendorName = _cursor.getString(_cursorIndexOfVendorName);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final int _tmpTokensUsed;
            _tmpTokensUsed = _cursor.getInt(_cursorIndexOfTokensUsed);
            final long _tmpResponseTimeMs;
            _tmpResponseTimeMs = _cursor.getLong(_cursorIndexOfResponseTimeMs);
            _item = new ChatMessage(_tmpId,_tmpConversationId,_tmpRole,_tmpContent,_tmpModelId,_tmpModelName,_tmpVendorName,_tmpTimestamp,_tmpTokensUsed,_tmpResponseTimeMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getMessagesListByConversation(final long conversationId,
      final Continuation<? super List<ChatMessage>> $completion) {
    final String _sql = "SELECT * FROM chat_messages WHERE conversationId = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, conversationId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ChatMessage>>() {
      @Override
      @NonNull
      public List<ChatMessage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "modelId");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorName");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfTokensUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "tokensUsed");
          final int _cursorIndexOfResponseTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "responseTimeMs");
          final List<ChatMessage> _result = new ArrayList<ChatMessage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChatMessage _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpModelId;
            if (_cursor.isNull(_cursorIndexOfModelId)) {
              _tmpModelId = null;
            } else {
              _tmpModelId = _cursor.getString(_cursorIndexOfModelId);
            }
            final String _tmpModelName;
            if (_cursor.isNull(_cursorIndexOfModelName)) {
              _tmpModelName = null;
            } else {
              _tmpModelName = _cursor.getString(_cursorIndexOfModelName);
            }
            final String _tmpVendorName;
            if (_cursor.isNull(_cursorIndexOfVendorName)) {
              _tmpVendorName = null;
            } else {
              _tmpVendorName = _cursor.getString(_cursorIndexOfVendorName);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final int _tmpTokensUsed;
            _tmpTokensUsed = _cursor.getInt(_cursorIndexOfTokensUsed);
            final long _tmpResponseTimeMs;
            _tmpResponseTimeMs = _cursor.getLong(_cursorIndexOfResponseTimeMs);
            _item = new ChatMessage(_tmpId,_tmpConversationId,_tmpRole,_tmpContent,_tmpModelId,_tmpModelName,_tmpVendorName,_tmpTimestamp,_tmpTokensUsed,_tmpResponseTimeMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getMessageCount(final long conversationId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM chat_messages WHERE conversationId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, conversationId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLastMessage(final long conversationId,
      final Continuation<? super ChatMessage> $completion) {
    final String _sql = "SELECT * FROM chat_messages WHERE conversationId = ? ORDER BY timestamp DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, conversationId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ChatMessage>() {
      @Override
      @Nullable
      public ChatMessage call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "modelId");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorName");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfTokensUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "tokensUsed");
          final int _cursorIndexOfResponseTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "responseTimeMs");
          final ChatMessage _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpModelId;
            if (_cursor.isNull(_cursorIndexOfModelId)) {
              _tmpModelId = null;
            } else {
              _tmpModelId = _cursor.getString(_cursorIndexOfModelId);
            }
            final String _tmpModelName;
            if (_cursor.isNull(_cursorIndexOfModelName)) {
              _tmpModelName = null;
            } else {
              _tmpModelName = _cursor.getString(_cursorIndexOfModelName);
            }
            final String _tmpVendorName;
            if (_cursor.isNull(_cursorIndexOfVendorName)) {
              _tmpVendorName = null;
            } else {
              _tmpVendorName = _cursor.getString(_cursorIndexOfVendorName);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final int _tmpTokensUsed;
            _tmpTokensUsed = _cursor.getInt(_cursorIndexOfTokensUsed);
            final long _tmpResponseTimeMs;
            _tmpResponseTimeMs = _cursor.getLong(_cursorIndexOfResponseTimeMs);
            _result = new ChatMessage(_tmpId,_tmpConversationId,_tmpRole,_tmpContent,_tmpModelId,_tmpModelName,_tmpVendorName,_tmpTimestamp,_tmpTokensUsed,_tmpResponseTimeMs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
