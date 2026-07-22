package com.aiaggregator.app.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
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
public final class MessageDao_Impl implements MessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MessageEntity> __insertionAdapterOfMessageEntity;

  private final EntityDeletionOrUpdateAdapter<MessageEntity> __updateAdapterOfMessageEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMessagesByConversation;

  private final SharedSQLiteStatement __preparedStmtOfDeleteLastAssistantMessage;

  public MessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMessageEntity = new EntityInsertionAdapter<MessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `messages` (`id`,`conversation_id`,`role`,`content`,`model_id`,`model_name`,`vendor_name`,`timestamp`,`response_time_ms`,`token_count`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MessageEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getConversationId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getConversationId());
        }
        if (entity.getRole() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getRole());
        }
        if (entity.getContent() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getContent());
        }
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
        statement.bindLong(9, entity.getResponseTimeMs());
        statement.bindLong(10, entity.getTokenCount());
      }
    };
    this.__updateAdapterOfMessageEntity = new EntityDeletionOrUpdateAdapter<MessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `messages` SET `id` = ?,`conversation_id` = ?,`role` = ?,`content` = ?,`model_id` = ?,`model_name` = ?,`vendor_name` = ?,`timestamp` = ?,`response_time_ms` = ?,`token_count` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MessageEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getConversationId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getConversationId());
        }
        if (entity.getRole() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getRole());
        }
        if (entity.getContent() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getContent());
        }
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
        statement.bindLong(9, entity.getResponseTimeMs());
        statement.bindLong(10, entity.getTokenCount());
        statement.bindLong(11, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteMessagesByConversation = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages WHERE conversation_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteLastAssistantMessage = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages WHERE conversation_id = ? AND role = 'assistant' AND id = (SELECT MAX(id) FROM messages WHERE conversation_id = ? AND role = 'assistant')";
        return _query;
      }
    };
  }

  @Override
  public Object insertMessage(final MessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMessageEntity.insert(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessage(final MessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMessageEntity.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessagesByConversation(final String conversationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMessagesByConversation.acquire();
        int _argIndex = 1;
        if (conversationId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, conversationId);
        }
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
  public Object deleteLastAssistantMessage(final String conversationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteLastAssistantMessage.acquire();
        int _argIndex = 1;
        if (conversationId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, conversationId);
        }
        _argIndex = 2;
        if (conversationId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, conversationId);
        }
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
          __preparedStmtOfDeleteLastAssistantMessage.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MessageEntity>> getMessagesByConversation(final String conversationId) {
    final String _sql = "SELECT * FROM messages WHERE conversation_id = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (conversationId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, conversationId);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversation_id");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "model_id");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "model_name");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendor_name");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfResponseTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "response_time_ms");
          final int _cursorIndexOfTokenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "token_count");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpConversationId;
            if (_cursor.isNull(_cursorIndexOfConversationId)) {
              _tmpConversationId = null;
            } else {
              _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            }
            final String _tmpRole;
            if (_cursor.isNull(_cursorIndexOfRole)) {
              _tmpRole = null;
            } else {
              _tmpRole = _cursor.getString(_cursorIndexOfRole);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
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
            final long _tmpResponseTimeMs;
            _tmpResponseTimeMs = _cursor.getLong(_cursorIndexOfResponseTimeMs);
            final int _tmpTokenCount;
            _tmpTokenCount = _cursor.getInt(_cursorIndexOfTokenCount);
            _item = new MessageEntity(_tmpId,_tmpConversationId,_tmpRole,_tmpContent,_tmpModelId,_tmpModelName,_tmpVendorName,_tmpTimestamp,_tmpResponseTimeMs,_tmpTokenCount);
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
  public Object getMessagesByConversationSync(final String conversationId,
      final Continuation<? super List<MessageEntity>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE conversation_id = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (conversationId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, conversationId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversation_id");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "model_id");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "model_name");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendor_name");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfResponseTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "response_time_ms");
          final int _cursorIndexOfTokenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "token_count");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpConversationId;
            if (_cursor.isNull(_cursorIndexOfConversationId)) {
              _tmpConversationId = null;
            } else {
              _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            }
            final String _tmpRole;
            if (_cursor.isNull(_cursorIndexOfRole)) {
              _tmpRole = null;
            } else {
              _tmpRole = _cursor.getString(_cursorIndexOfRole);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
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
            final long _tmpResponseTimeMs;
            _tmpResponseTimeMs = _cursor.getLong(_cursorIndexOfResponseTimeMs);
            final int _tmpTokenCount;
            _tmpTokenCount = _cursor.getInt(_cursorIndexOfTokenCount);
            _item = new MessageEntity(_tmpId,_tmpConversationId,_tmpRole,_tmpContent,_tmpModelId,_tmpModelName,_tmpVendorName,_tmpTimestamp,_tmpResponseTimeMs,_tmpTokenCount);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
