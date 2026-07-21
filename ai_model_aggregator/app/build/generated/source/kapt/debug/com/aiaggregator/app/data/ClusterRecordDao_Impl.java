package com.aiaggregator.app.data;

import android.database.Cursor;
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
public final class ClusterRecordDao_Impl implements ClusterRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ClusterRecordEntity> __insertionAdapterOfClusterRecordEntity;

  private final EntityDeletionOrUpdateAdapter<ClusterRecordEntity> __deletionAdapterOfClusterRecordEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllRecords;

  public ClusterRecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfClusterRecordEntity = new EntityInsertionAdapter<ClusterRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cluster_records` (`id`,`prompt`,`model_ids`,`results_json`,`created_at`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ClusterRecordEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getPrompt() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPrompt());
        }
        if (entity.getModelIds() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getModelIds());
        }
        if (entity.getResultsJson() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getResultsJson());
        }
        statement.bindLong(5, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfClusterRecordEntity = new EntityDeletionOrUpdateAdapter<ClusterRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `cluster_records` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ClusterRecordEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAllRecords = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cluster_records";
        return _query;
      }
    };
  }

  @Override
  public Object insertRecord(final ClusterRecordEntity record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfClusterRecordEntity.insert(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRecord(final ClusterRecordEntity record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfClusterRecordEntity.handle(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllRecords(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllRecords.acquire();
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
          __preparedStmtOfDeleteAllRecords.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ClusterRecordEntity>> getAllRecords() {
    final String _sql = "SELECT * FROM cluster_records ORDER BY created_at DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cluster_records"}, new Callable<List<ClusterRecordEntity>>() {
      @Override
      @NonNull
      public List<ClusterRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPrompt = CursorUtil.getColumnIndexOrThrow(_cursor, "prompt");
          final int _cursorIndexOfModelIds = CursorUtil.getColumnIndexOrThrow(_cursor, "model_ids");
          final int _cursorIndexOfResultsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "results_json");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<ClusterRecordEntity> _result = new ArrayList<ClusterRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ClusterRecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPrompt;
            if (_cursor.isNull(_cursorIndexOfPrompt)) {
              _tmpPrompt = null;
            } else {
              _tmpPrompt = _cursor.getString(_cursorIndexOfPrompt);
            }
            final String _tmpModelIds;
            if (_cursor.isNull(_cursorIndexOfModelIds)) {
              _tmpModelIds = null;
            } else {
              _tmpModelIds = _cursor.getString(_cursorIndexOfModelIds);
            }
            final String _tmpResultsJson;
            if (_cursor.isNull(_cursorIndexOfResultsJson)) {
              _tmpResultsJson = null;
            } else {
              _tmpResultsJson = _cursor.getString(_cursorIndexOfResultsJson);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ClusterRecordEntity(_tmpId,_tmpPrompt,_tmpModelIds,_tmpResultsJson,_tmpCreatedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
