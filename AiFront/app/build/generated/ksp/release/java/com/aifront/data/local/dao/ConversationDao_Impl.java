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
import com.aifront.data.model.Conversation;
import com.aifront.data.model.ConversationType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
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
public final class ConversationDao_Impl implements ConversationDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Conversation> __insertionAdapterOfConversation;

  private final EntityDeletionOrUpdateAdapter<Conversation> __deletionAdapterOfConversation;

  private final EntityDeletionOrUpdateAdapter<Conversation> __updateAdapterOfConversation;

  private final SharedSQLiteStatement __preparedStmtOfDeleteConversationById;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMessageCount;

  private final SharedSQLiteStatement __preparedStmtOfUpdateConversationMeta;

  public ConversationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfConversation = new EntityInsertionAdapter<Conversation>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `conversations` (`id`,`title`,`modelId`,`modelName`,`vendorName`,`conversationType`,`isClusterMode`,`clusterModelIds`,`createdAt`,`updatedAt`,`messageCount`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Conversation entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        if (entity.getModelId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getModelId());
        }
        if (entity.getModelName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getModelName());
        }
        if (entity.getVendorName() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getVendorName());
        }
        statement.bindString(6, __ConversationType_enumToString(entity.getConversationType()));
        final int _tmp = entity.isClusterMode() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getClusterModelIds() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getClusterModelIds());
        }
        statement.bindLong(9, entity.getCreatedAt());
        statement.bindLong(10, entity.getUpdatedAt());
        statement.bindLong(11, entity.getMessageCount());
      }
    };
    this.__deletionAdapterOfConversation = new EntityDeletionOrUpdateAdapter<Conversation>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `conversations` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Conversation entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfConversation = new EntityDeletionOrUpdateAdapter<Conversation>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `conversations` SET `id` = ?,`title` = ?,`modelId` = ?,`modelName` = ?,`vendorName` = ?,`conversationType` = ?,`isClusterMode` = ?,`clusterModelIds` = ?,`createdAt` = ?,`updatedAt` = ?,`messageCount` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Conversation entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        if (entity.getModelId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getModelId());
        }
        if (entity.getModelName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getModelName());
        }
        if (entity.getVendorName() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getVendorName());
        }
        statement.bindString(6, __ConversationType_enumToString(entity.getConversationType()));
        final int _tmp = entity.isClusterMode() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getClusterModelIds() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getClusterModelIds());
        }
        statement.bindLong(9, entity.getCreatedAt());
        statement.bindLong(10, entity.getUpdatedAt());
        statement.bindLong(11, entity.getMessageCount());
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteConversationById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM conversations WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateMessageCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE conversations SET messageCount = (SELECT COUNT(*) FROM chat_messages WHERE conversationId = ?) WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateConversationMeta = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE conversations SET updatedAt = ?, messageCount = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertConversation(final Conversation conversation,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfConversation.insertAndReturnId(conversation);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteConversation(final Conversation conversation,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfConversation.handle(conversation);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateConversation(final Conversation conversation,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfConversation.handle(conversation);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteConversationById(final long id,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteConversationById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfDeleteConversationById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessageCount(final long conversationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMessageCount.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, conversationId);
        _argIndex = 2;
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
          __preparedStmtOfUpdateMessageCount.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateConversationMeta(final long id, final long updatedAt, final int messageCount,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateConversationMeta.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, messageCount);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfUpdateConversationMeta.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Conversation>> getAllConversations() {
    final String _sql = "SELECT * FROM conversations ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"conversations"}, new Callable<List<Conversation>>() {
      @Override
      @NonNull
      public List<Conversation> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "modelId");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorName");
          final int _cursorIndexOfConversationType = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationType");
          final int _cursorIndexOfIsClusterMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isClusterMode");
          final int _cursorIndexOfClusterModelIds = CursorUtil.getColumnIndexOrThrow(_cursor, "clusterModelIds");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfMessageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "messageCount");
          final List<Conversation> _result = new ArrayList<Conversation>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Conversation _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
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
            final ConversationType _tmpConversationType;
            _tmpConversationType = __ConversationType_stringToEnum(_cursor.getString(_cursorIndexOfConversationType));
            final boolean _tmpIsClusterMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsClusterMode);
            _tmpIsClusterMode = _tmp != 0;
            final String _tmpClusterModelIds;
            if (_cursor.isNull(_cursorIndexOfClusterModelIds)) {
              _tmpClusterModelIds = null;
            } else {
              _tmpClusterModelIds = _cursor.getString(_cursorIndexOfClusterModelIds);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpMessageCount;
            _tmpMessageCount = _cursor.getInt(_cursorIndexOfMessageCount);
            _item = new Conversation(_tmpId,_tmpTitle,_tmpModelId,_tmpModelName,_tmpVendorName,_tmpConversationType,_tmpIsClusterMode,_tmpClusterModelIds,_tmpCreatedAt,_tmpUpdatedAt,_tmpMessageCount);
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
  public Flow<List<Conversation>> getConversationsByType(final String type) {
    final String _sql = "SELECT * FROM conversations WHERE conversationType = ? ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, type);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"conversations"}, new Callable<List<Conversation>>() {
      @Override
      @NonNull
      public List<Conversation> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "modelId");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorName");
          final int _cursorIndexOfConversationType = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationType");
          final int _cursorIndexOfIsClusterMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isClusterMode");
          final int _cursorIndexOfClusterModelIds = CursorUtil.getColumnIndexOrThrow(_cursor, "clusterModelIds");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfMessageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "messageCount");
          final List<Conversation> _result = new ArrayList<Conversation>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Conversation _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
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
            final ConversationType _tmpConversationType;
            _tmpConversationType = __ConversationType_stringToEnum(_cursor.getString(_cursorIndexOfConversationType));
            final boolean _tmpIsClusterMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsClusterMode);
            _tmpIsClusterMode = _tmp != 0;
            final String _tmpClusterModelIds;
            if (_cursor.isNull(_cursorIndexOfClusterModelIds)) {
              _tmpClusterModelIds = null;
            } else {
              _tmpClusterModelIds = _cursor.getString(_cursorIndexOfClusterModelIds);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpMessageCount;
            _tmpMessageCount = _cursor.getInt(_cursorIndexOfMessageCount);
            _item = new Conversation(_tmpId,_tmpTitle,_tmpModelId,_tmpModelName,_tmpVendorName,_tmpConversationType,_tmpIsClusterMode,_tmpClusterModelIds,_tmpCreatedAt,_tmpUpdatedAt,_tmpMessageCount);
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
  public Object getConversationById(final long id,
      final Continuation<? super Conversation> $completion) {
    final String _sql = "SELECT * FROM conversations WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Conversation>() {
      @Override
      @Nullable
      public Conversation call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "modelId");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfVendorName = CursorUtil.getColumnIndexOrThrow(_cursor, "vendorName");
          final int _cursorIndexOfConversationType = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationType");
          final int _cursorIndexOfIsClusterMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isClusterMode");
          final int _cursorIndexOfClusterModelIds = CursorUtil.getColumnIndexOrThrow(_cursor, "clusterModelIds");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfMessageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "messageCount");
          final Conversation _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
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
            final ConversationType _tmpConversationType;
            _tmpConversationType = __ConversationType_stringToEnum(_cursor.getString(_cursorIndexOfConversationType));
            final boolean _tmpIsClusterMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsClusterMode);
            _tmpIsClusterMode = _tmp != 0;
            final String _tmpClusterModelIds;
            if (_cursor.isNull(_cursorIndexOfClusterModelIds)) {
              _tmpClusterModelIds = null;
            } else {
              _tmpClusterModelIds = _cursor.getString(_cursorIndexOfClusterModelIds);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final int _tmpMessageCount;
            _tmpMessageCount = _cursor.getInt(_cursorIndexOfMessageCount);
            _result = new Conversation(_tmpId,_tmpTitle,_tmpModelId,_tmpModelName,_tmpVendorName,_tmpConversationType,_tmpIsClusterMode,_tmpClusterModelIds,_tmpCreatedAt,_tmpUpdatedAt,_tmpMessageCount);
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

  private String __ConversationType_enumToString(@NonNull final ConversationType _value) {
    switch (_value) {
      case TEXT: return "TEXT";
      case CODE: return "CODE";
      case IMAGE: return "IMAGE";
      case CLUSTER: return "CLUSTER";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private ConversationType __ConversationType_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "TEXT": return ConversationType.TEXT;
      case "CODE": return ConversationType.CODE;
      case "IMAGE": return ConversationType.IMAGE;
      case "CLUSTER": return ConversationType.CLUSTER;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
