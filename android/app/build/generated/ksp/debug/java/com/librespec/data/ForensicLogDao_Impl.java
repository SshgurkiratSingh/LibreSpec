package com.librespec.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
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
public final class ForensicLogDao_Impl implements ForensicLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ForensicLog> __insertionAdapterOfForensicLog;

  private final EntityDeletionOrUpdateAdapter<ForensicLog> __updateAdapterOfForensicLog;

  public ForensicLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfForensicLog = new EntityInsertionAdapter<ForensicLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `forensic_logs` (`testId`,`timestamp`,`predictedClass`,`confidenceScore`,`baselineVector`,`plateauVector`,`appGeneratedHash`,`latitude`,`longitude`,`syncStatus`,`apiResponse`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ForensicLog entity) {
        statement.bindString(1, entity.getTestId());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindString(3, entity.getPredictedClass());
        statement.bindDouble(4, entity.getConfidenceScore());
        statement.bindString(5, entity.getBaselineVector());
        statement.bindString(6, entity.getPlateauVector());
        statement.bindString(7, entity.getAppGeneratedHash());
        statement.bindDouble(8, entity.getLatitude());
        statement.bindDouble(9, entity.getLongitude());
        statement.bindLong(10, entity.getSyncStatus());
        if (entity.getApiResponse() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getApiResponse());
        }
      }
    };
    this.__updateAdapterOfForensicLog = new EntityDeletionOrUpdateAdapter<ForensicLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `forensic_logs` SET `testId` = ?,`timestamp` = ?,`predictedClass` = ?,`confidenceScore` = ?,`baselineVector` = ?,`plateauVector` = ?,`appGeneratedHash` = ?,`latitude` = ?,`longitude` = ?,`syncStatus` = ?,`apiResponse` = ? WHERE `testId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ForensicLog entity) {
        statement.bindString(1, entity.getTestId());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindString(3, entity.getPredictedClass());
        statement.bindDouble(4, entity.getConfidenceScore());
        statement.bindString(5, entity.getBaselineVector());
        statement.bindString(6, entity.getPlateauVector());
        statement.bindString(7, entity.getAppGeneratedHash());
        statement.bindDouble(8, entity.getLatitude());
        statement.bindDouble(9, entity.getLongitude());
        statement.bindLong(10, entity.getSyncStatus());
        if (entity.getApiResponse() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getApiResponse());
        }
        statement.bindString(12, entity.getTestId());
      }
    };
  }

  @Override
  public Object insert(final ForensicLog log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfForensicLog.insert(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ForensicLog log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfForensicLog.handle(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPendingLogs(final Continuation<? super List<ForensicLog>> $completion) {
    final String _sql = "SELECT * FROM forensic_logs WHERE syncStatus = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ForensicLog>>() {
      @Override
      @NonNull
      public List<ForensicLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTestId = CursorUtil.getColumnIndexOrThrow(_cursor, "testId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfPredictedClass = CursorUtil.getColumnIndexOrThrow(_cursor, "predictedClass");
          final int _cursorIndexOfConfidenceScore = CursorUtil.getColumnIndexOrThrow(_cursor, "confidenceScore");
          final int _cursorIndexOfBaselineVector = CursorUtil.getColumnIndexOrThrow(_cursor, "baselineVector");
          final int _cursorIndexOfPlateauVector = CursorUtil.getColumnIndexOrThrow(_cursor, "plateauVector");
          final int _cursorIndexOfAppGeneratedHash = CursorUtil.getColumnIndexOrThrow(_cursor, "appGeneratedHash");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfApiResponse = CursorUtil.getColumnIndexOrThrow(_cursor, "apiResponse");
          final List<ForensicLog> _result = new ArrayList<ForensicLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ForensicLog _item;
            final String _tmpTestId;
            _tmpTestId = _cursor.getString(_cursorIndexOfTestId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpPredictedClass;
            _tmpPredictedClass = _cursor.getString(_cursorIndexOfPredictedClass);
            final float _tmpConfidenceScore;
            _tmpConfidenceScore = _cursor.getFloat(_cursorIndexOfConfidenceScore);
            final String _tmpBaselineVector;
            _tmpBaselineVector = _cursor.getString(_cursorIndexOfBaselineVector);
            final String _tmpPlateauVector;
            _tmpPlateauVector = _cursor.getString(_cursorIndexOfPlateauVector);
            final String _tmpAppGeneratedHash;
            _tmpAppGeneratedHash = _cursor.getString(_cursorIndexOfAppGeneratedHash);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final int _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getInt(_cursorIndexOfSyncStatus);
            final String _tmpApiResponse;
            if (_cursor.isNull(_cursorIndexOfApiResponse)) {
              _tmpApiResponse = null;
            } else {
              _tmpApiResponse = _cursor.getString(_cursorIndexOfApiResponse);
            }
            _item = new ForensicLog(_tmpTestId,_tmpTimestamp,_tmpPredictedClass,_tmpConfidenceScore,_tmpBaselineVector,_tmpPlateauVector,_tmpAppGeneratedHash,_tmpLatitude,_tmpLongitude,_tmpSyncStatus,_tmpApiResponse);
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
  public Flow<List<ForensicLog>> getAllLogs() {
    final String _sql = "SELECT * FROM forensic_logs ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"forensic_logs"}, new Callable<List<ForensicLog>>() {
      @Override
      @NonNull
      public List<ForensicLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTestId = CursorUtil.getColumnIndexOrThrow(_cursor, "testId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfPredictedClass = CursorUtil.getColumnIndexOrThrow(_cursor, "predictedClass");
          final int _cursorIndexOfConfidenceScore = CursorUtil.getColumnIndexOrThrow(_cursor, "confidenceScore");
          final int _cursorIndexOfBaselineVector = CursorUtil.getColumnIndexOrThrow(_cursor, "baselineVector");
          final int _cursorIndexOfPlateauVector = CursorUtil.getColumnIndexOrThrow(_cursor, "plateauVector");
          final int _cursorIndexOfAppGeneratedHash = CursorUtil.getColumnIndexOrThrow(_cursor, "appGeneratedHash");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfApiResponse = CursorUtil.getColumnIndexOrThrow(_cursor, "apiResponse");
          final List<ForensicLog> _result = new ArrayList<ForensicLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ForensicLog _item;
            final String _tmpTestId;
            _tmpTestId = _cursor.getString(_cursorIndexOfTestId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpPredictedClass;
            _tmpPredictedClass = _cursor.getString(_cursorIndexOfPredictedClass);
            final float _tmpConfidenceScore;
            _tmpConfidenceScore = _cursor.getFloat(_cursorIndexOfConfidenceScore);
            final String _tmpBaselineVector;
            _tmpBaselineVector = _cursor.getString(_cursorIndexOfBaselineVector);
            final String _tmpPlateauVector;
            _tmpPlateauVector = _cursor.getString(_cursorIndexOfPlateauVector);
            final String _tmpAppGeneratedHash;
            _tmpAppGeneratedHash = _cursor.getString(_cursorIndexOfAppGeneratedHash);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final int _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getInt(_cursorIndexOfSyncStatus);
            final String _tmpApiResponse;
            if (_cursor.isNull(_cursorIndexOfApiResponse)) {
              _tmpApiResponse = null;
            } else {
              _tmpApiResponse = _cursor.getString(_cursorIndexOfApiResponse);
            }
            _item = new ForensicLog(_tmpTestId,_tmpTimestamp,_tmpPredictedClass,_tmpConfidenceScore,_tmpBaselineVector,_tmpPlateauVector,_tmpAppGeneratedHash,_tmpLatitude,_tmpLongitude,_tmpSyncStatus,_tmpApiResponse);
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
