package com.librespec.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ForensicLogDao _forensicLogDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `forensic_logs` (`testId` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `predictedClass` TEXT NOT NULL, `confidenceScore` REAL NOT NULL, `baselineVector` TEXT NOT NULL, `plateauVector` TEXT NOT NULL, `appGeneratedHash` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `syncStatus` INTEGER NOT NULL, `apiResponse` TEXT, PRIMARY KEY(`testId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'c9484495927231a9584ea79ca69750b4')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `forensic_logs`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsForensicLogs = new HashMap<String, TableInfo.Column>(11);
        _columnsForensicLogs.put("testId", new TableInfo.Column("testId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForensicLogs.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForensicLogs.put("predictedClass", new TableInfo.Column("predictedClass", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForensicLogs.put("confidenceScore", new TableInfo.Column("confidenceScore", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForensicLogs.put("baselineVector", new TableInfo.Column("baselineVector", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForensicLogs.put("plateauVector", new TableInfo.Column("plateauVector", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForensicLogs.put("appGeneratedHash", new TableInfo.Column("appGeneratedHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForensicLogs.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForensicLogs.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForensicLogs.put("syncStatus", new TableInfo.Column("syncStatus", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsForensicLogs.put("apiResponse", new TableInfo.Column("apiResponse", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysForensicLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesForensicLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoForensicLogs = new TableInfo("forensic_logs", _columnsForensicLogs, _foreignKeysForensicLogs, _indicesForensicLogs);
        final TableInfo _existingForensicLogs = TableInfo.read(db, "forensic_logs");
        if (!_infoForensicLogs.equals(_existingForensicLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "forensic_logs(com.librespec.data.ForensicLog).\n"
                  + " Expected:\n" + _infoForensicLogs + "\n"
                  + " Found:\n" + _existingForensicLogs);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "c9484495927231a9584ea79ca69750b4", "0860f6cdf30337123f0df4b48362a707");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "forensic_logs");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `forensic_logs`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ForensicLogDao.class, ForensicLogDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ForensicLogDao forensicLogDao() {
    if (_forensicLogDao != null) {
      return _forensicLogDao;
    } else {
      synchronized(this) {
        if(_forensicLogDao == null) {
          _forensicLogDao = new ForensicLogDao_Impl(this);
        }
        return _forensicLogDao;
      }
    }
  }
}
