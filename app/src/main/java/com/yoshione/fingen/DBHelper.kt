package com.yoshione.fingen

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.os.SystemClock
import android.provider.BaseColumns
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.yoshione.fingen.dao.*
import com.yoshione.fingen.db.UpdateHelper
import com.yoshione.fingen.interfaces.IOnUnzipComplete
import com.yoshione.fingen.managers.CabbageManager
import com.yoshione.fingen.model.BaseModel
import com.yoshione.fingen.model.Cabbage
import com.yoshione.fingen.utils.FileUtils
import com.yoshione.fingen.utils.Lg
import com.yoshione.fingen.utils.Translit
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteOpenHelper
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class DBHelper(context: Context, private val mOriginDB: Boolean) : SQLiteOpenHelper(
    context,
    if (mOriginDB) DATABASE_ORIGIN_NAME else DATABASE_NAME,
    null,
    if (mOriginDB) DATABASE_ORIGIN_VERSION else DATABASE_VERSION
), BaseColumns {
    private var arrayTableName: ArrayList<String> = ArrayList()
    val tag: String = "Database"

    val database: SQLiteDatabase

    //</editor-fold>
    private val mContext: Context

    init {
        this.mContext = context.applicationContext
        val dbh = DatabaseUpgradeHelper.getInstance()
        dbh.isUpgrading = true
        database = writableDatabase
        dbh.isUpgrading = false
        while (dbh.isUpgrading) {
            SystemClock.sleep(10)
        }
    }

    constructor(context: Context) : this(context, false) {}

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, CabbagesDAO.SQL_CREATE_TABLE)
        db.execSQL(CabbagesDAO.SQL_CREATE_TABLE)
        var cabbage: Cabbage?
        val codes = Arrays.asList("RUB", "USD", "EUR", "UAH", "BYN", "KZT", "ABC")
        for (code in codes) {
            cabbage = CabbageManager.createFromCode(code, mContext)
            if (cabbage != null) {
                db.insertOrThrow(CabbagesDAO.TABLE, null, cabbage.cv)
            }
        }
        try {
            arrayTableName.add(tableCreateNamedApps(db))
        } catch (e: Exception) {
            Log.i(tag, "onCreate: ${e.message}")
        }
        try {
            arrayTableName.add(tableCreateNamedCookies(db))
        } catch (e: Exception) {
            Log.i(tag, "onCreate: ${e.message}")
        }
        try {
            arrayTableName.add(tableCreateNotifyRepeatedSend(db))
        } catch (e: Exception) {
            Log.i(tag, "onCreate: ${e.message}")
        }


        Log.d(TAG, AccountsDAO.SQL_CREATE_TABLE)
        db.execSQL(AccountsDAO.SQL_CREATE_TABLE)
        Log.d(TAG, ProjectsDAO.SQL_CREATE_TABLE)
        db.execSQL(ProjectsDAO.SQL_CREATE_TABLE)
        Log.d(TAG, DepartmentsDAO.SQL_CREATE_TABLE)
        db.execSQL(DepartmentsDAO.SQL_CREATE_TABLE)
        Log.d(TAG, LocationsDAO.SQL_CREATE_TABLE)
        db.execSQL(LocationsDAO.SQL_CREATE_TABLE)
        Log.d(TAG, CategoriesDAO.SQL_CREATE_TABLE)
        db.execSQL(CategoriesDAO.SQL_CREATE_TABLE)
        Log.d(TAG, PayeesDAO.SQL_CREATE_TABLE)
        db.execSQL(PayeesDAO.SQL_CREATE_TABLE)
        Log.d(TAG, TransactionsDAO.SQL_CREATE_TABLE)
        db.execSQL(TransactionsDAO.SQL_CREATE_TABLE)
        Log.d(TAG, TransactionsDAO.SQL_CREATE_INDEX)
        db.execSQL(TransactionsDAO.SQL_CREATE_INDEX)
        Log.d(TAG, SmsDAO.SQL_CREATE_TABLE)
        db.execSQL(SmsDAO.SQL_CREATE_TABLE)
        Log.d(TAG, SmsMarkersDAO.SQL_CREATE_TABLE)
        db.execSQL(SmsMarkersDAO.SQL_CREATE_TABLE)
        Log.d(TAG, CreditsDAO.SQL_CREATE_TABLE)
        db.execSQL(CreditsDAO.SQL_CREATE_TABLE)
        Log.d(TAG, BudgetDAO.SQL_CREATE_TABLE)
        db.execSQL(BudgetDAO.SQL_CREATE_TABLE)
        Log.d(TAG, BudgetCreditsDAO.SQL_CREATE_TABLE)
        db.execSQL(BudgetCreditsDAO.SQL_CREATE_TABLE)
        Log.d(TAG, TemplatesDAO.SQL_CREATE_TABLE)
        db.execSQL(TemplatesDAO.SQL_CREATE_TABLE)
        Log.d(TAG, SimpleDebtsDAO.SQL_CREATE_TABLE)
        db.execSQL(SimpleDebtsDAO.SQL_CREATE_TABLE)
        Log.d(TAG, SendersDAO.SQL_CREATE_TABLE)
        db.execSQL(SendersDAO.SQL_CREATE_TABLE)
        Log.d(TAG, AccountsSetsRefDAO.SQL_CREATE_TABLE)
        db.execSQL(AccountsSetsRefDAO.SQL_CREATE_TABLE)
        Log.d(TAG, AccountsSetsLogDAO.SQL_CREATE_TABLE)
        db.execSQL(AccountsSetsLogDAO.SQL_CREATE_TABLE)
        Log.d(TAG, ProductsDAO.SQL_CREATE_TABLE)
        db.execSQL(ProductsDAO.SQL_CREATE_TABLE)
        Log.d(TAG, ProductEntrysDAO.SQL_CREATE_TABLE)
        db.execSQL(ProductEntrysDAO.SQL_CREATE_TABLE)
        Log.d(TAG, ProductEntrysDAO.SQL_CREATE_INDEX)
        db.execSQL(ProductEntrysDAO.SQL_CREATE_INDEX)
        val cv = ContentValues()
        cv.put(ProductsDAO.COL_ID, 0)
        cv.put(ProductsDAO.COL_NAME, "default_product")
        db.insert(ProductsDAO.TABLE, "", cv)
        Log.d(TAG, RunningBalanceDAO.SQL_CREATE_TABLE)
        db.execSQL(RunningBalanceDAO.SQL_CREATE_TABLE)
        db.execSQL(RunningBalanceDAO.SQL_CREATE_INDEX_ACCOUNTS)
        db.execSQL(RunningBalanceDAO.SQL_CREATE_INDEX_TRANSACTIONS)
        db.execSQL(RunningBalanceDAO.SQL_CREATE_INDEX_DATETIME)
    }

    @SuppressLint("DefaultLocale", "CallNeedsPermission")
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Lg.log(TAG, "Upgrade database $oldVersion -> $newVersion")

        //Сделали на всякий случай бэкап
        try {
            backupDB(false)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (oldVersion < 17) {
            UpdateHelper.update17(db, mContext)
        }
        if (oldVersion < 18) {
            UpdateHelper.update18(db, mContext)
        }
        if (oldVersion < 19) {
            UpdateHelper.update19(db)
        }
        if (oldVersion < 20) {
            UpdateHelper.update20(db)
        }
        if (oldVersion < 21) {
            UpdateHelper.update21(db)
        }
        if (oldVersion < 22) {
            UpdateHelper.update22(db)
        }
        if (oldVersion < 23) {
            UpdateHelper.update23(db)
        }
        if (oldVersion < 24) {
            UpdateHelper.update24(db)
        }
        if (oldVersion < 25) {
            UpdateHelper.update25(db)
        }
        if (oldVersion < 26) {
            UpdateHelper.update26(db)
        }
        if (oldVersion < 27) {
            UpdateHelper.update27(db) { database: SQLiteDatabase? -> updateRunningBalance(db) }
        }
        if (oldVersion < 28) {
            try {
                updateRunningBalance(db)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (oldVersion < 29) {
            UpdateHelper.update29(db)
        }
        if (oldVersion < 30) {
            UpdateHelper.update30(db)
        }
        if (oldVersion < 31) {
            UpdateHelper.update31(db, mContext)
        }
        if (oldVersion < 32) {
            UpdateHelper.update32(db)
        }
        if (oldVersion < 34) {
            UpdateHelper.update33(db)
        }
        if (oldVersion < 36) {
            UpdateHelper.update35(db)
        }
        if (oldVersion < 37) {
            UpdateHelper.update36(db)
        }
        if (oldVersion < 25) {
            try {
                updateRunningBalance(db)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Downgrade database $oldVersion -> $newVersion")
        if (oldVersion >= 37 && newVersion == DATABASE_ORIGIN_VERSION) {
            db.execSQL("ALTER TABLE ref_Departments RENAME TO ref_Departments_old")
            db.execSQL(
                "CREATE TABLE ref_Departments ("
                        + BaseDAO.COMMON_FIELDS + ", "
                        + BaseDAO.COL_NAME + " TEXT NOT NULL, "
                        + DepartmentsDAO.COL_IS_ACTIVE + " INTEGER NOT NULL, "
                        + BaseDAO.COL_PARENT_ID + " INTEGER REFERENCES [" + DepartmentsDAO.TABLE + "]([" + BaseDAO.COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
                        + BaseDAO.COL_ORDER_NUMBER + " INTEGER, "
                        + BaseDAO.COL_FULL_NAME + " TEXT, "
                        + BaseDAO.COL_SEARCH_STRING + " TEXT, "
                        + "UNIQUE (" + BaseDAO.COL_NAME + ", " + BaseDAO.COL_PARENT_ID + ", " + BaseDAO.COL_SYNC_DELETED + ") ON CONFLICT ABORT);"
            )
            db.execSQL(
                "INSERT INTO ref_Departments (" + BaseDAO.COL_ID + ", " + BaseDAO.COL_SYNC_FBID + ", " + BaseDAO.COL_SYNC_TS + ", " + BaseDAO.COL_SYNC_DELETED + ", "
                        + BaseDAO.COL_SYNC_DIRTY + ", " + BaseDAO.COL_SYNC_LAST_EDITED + ", " + BaseDAO.COL_NAME + ", " + DepartmentsDAO.COL_IS_ACTIVE + ", "
                        + BaseDAO.COL_PARENT_ID + ", " + BaseDAO.COL_ORDER_NUMBER + ", " + BaseDAO.COL_FULL_NAME + ", " + BaseDAO.COL_SEARCH_STRING + ")"
                        + " SELECT " + BaseDAO.COL_ID + ", " + BaseDAO.COL_SYNC_FBID + ", " + BaseDAO.COL_SYNC_TS + ", " + BaseDAO.COL_SYNC_DELETED + ", "
                        + BaseDAO.COL_SYNC_DIRTY + ", " + BaseDAO.COL_SYNC_LAST_EDITED + ", " + BaseDAO.COL_NAME + ", " + DepartmentsDAO.COL_IS_ACTIVE + ", "
                        + BaseDAO.COL_PARENT_ID + ", " + BaseDAO.COL_ORDER_NUMBER + ", " + BaseDAO.COL_FULL_NAME + ", " + BaseDAO.COL_SEARCH_STRING
                        + " FROM ref_Departments_old"
            )
            db.execSQL("DROP TABLE ref_Departments_old")
            db.execSQL("ALTER TABLE log_Products RENAME TO log_Products_old")
            db.execSQL(
                "CREATE TABLE log_Products ("
                        + BaseDAO.COMMON_FIELDS + ", "
                        + ProductEntrysDAO.COL_TRANSACTION_ID + " INTEGER REFERENCES [" + TransactionsDAO.TABLE + "]([" + BaseDAO.COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
                        + ProductEntrysDAO.COL_PRODUCT_ID + " INTEGER REFERENCES [" + ProductsDAO.TABLE + "]([" + BaseDAO.COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
                        + ProductEntrysDAO.COL_CATEGORY_ID + " INTEGER DEFAULT -1 REFERENCES [" + CategoriesDAO.TABLE + "]([" + BaseDAO.COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
                        + ProductEntrysDAO.COL_PROJECT_ID + " INTEGER DEFAULT -1 REFERENCES [" + ProjectsDAO.TABLE + "]([" + BaseDAO.COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
                        + ProductEntrysDAO.COL_PRICE + " REAL NOT NULL DEFAULT 0, "
                        + ProductEntrysDAO.COL_QUANTITY + " REAL NOT NULL DEFAULT 1 CHECK (Quantity >= 0));"
            )
            db.execSQL(
                "INSERT INTO log_Products (" + BaseDAO.COL_ID + ", " + BaseDAO.COL_SYNC_FBID + ", " + BaseDAO.COL_SYNC_TS + ", " + BaseDAO.COL_SYNC_DELETED + ", "
                        + BaseDAO.COL_SYNC_DIRTY + ", " + BaseDAO.COL_SYNC_LAST_EDITED + ", " + ProductEntrysDAO.COL_TRANSACTION_ID + ", "
                        + ProductEntrysDAO.COL_PRODUCT_ID + ", " + ProductEntrysDAO.COL_CATEGORY_ID + ", " + ProductEntrysDAO.COL_PROJECT_ID + ", "
                        + ProductEntrysDAO.COL_PRICE + ", " + ProductEntrysDAO.COL_QUANTITY + ")"
                        + " SELECT " + BaseDAO.COL_ID + ", " + BaseDAO.COL_SYNC_FBID + ", " + BaseDAO.COL_SYNC_TS + ", " + BaseDAO.COL_SYNC_DELETED + ", "
                        + BaseDAO.COL_SYNC_DIRTY + ", " + BaseDAO.COL_SYNC_LAST_EDITED + ", " + ProductEntrysDAO.COL_TRANSACTION_ID + ", "
                        + ProductEntrysDAO.COL_PRODUCT_ID + ", " + ProductEntrysDAO.COL_CATEGORY_ID + ", " + ProductEntrysDAO.COL_PROJECT_ID + ", "
                        + ProductEntrysDAO.COL_PRICE + ", " + ProductEntrysDAO.COL_QUANTITY
                        + " FROM log_Products_old"
            )
            db.execSQL("DROP TABLE log_Products_old")
        }
        if (oldVersion >= 36 && newVersion == DATABASE_ORIGIN_VERSION) {
            db.execSQL("ALTER TABLE ref_Accounts RENAME TO ref_Accounts_old")
            db.execSQL(
                "CREATE TABLE " + AccountsDAO.TABLE + " ("
                        + AccountsDAO.COMMON_FIELDS + ", "
                        + AccountsDAO.COL_TYPE + " INTEGER NOT NULL, "
                        + AccountsDAO.COL_NAME + " TEXT NOT NULL, "
                        + AccountsDAO.COL_CURRENCY + " INTEGER REFERENCES [" + CabbagesDAO.TABLE + "]([" + BaseDAO.COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
                        + AccountsDAO.COL_EMITENT + " TEXT, "
                        + AccountsDAO.COL_LAST4DIGITS + " INTEGER, "
                        + AccountsDAO.COL_COMMENT + " TEXT, "
                        + AccountsDAO.COL_START_BALANCE + " REAL NOT NULL, "
                        + AccountsDAO.COL_IS_CLOSED + " INTEGER NOT NULL, "
                        + AccountsDAO.COL_ORDER + " INTEGER, "
                        + AccountsDAO.COL_CREDIT_LIMIT + " REAL, "
                        + AccountsDAO.COL_SEARCH_STRING + " TEXT, "
                        + "UNIQUE (" + AccountsDAO.COL_NAME + ", " + AccountsDAO.COL_SYNC_DELETED + ") ON CONFLICT ABORT);"
            )
            db.execSQL(
                "INSERT INTO ref_Accounts (" + AccountsDAO.COL_ID + ", " + AccountsDAO.COL_SYNC_FBID + ", " + AccountsDAO.COL_SYNC_TS + ", " + AccountsDAO.COL_SYNC_DELETED + ", "
                        + AccountsDAO.COL_SYNC_DIRTY + ", " + AccountsDAO.COL_SYNC_LAST_EDITED + ", " + AccountsDAO.COL_TYPE + ", " + AccountsDAO.COL_NAME + ", "
                        + AccountsDAO.COL_CURRENCY + ", " + AccountsDAO.COL_EMITENT + ", " + AccountsDAO.COL_LAST4DIGITS + ", " + AccountsDAO.COL_COMMENT + ", "
                        + AccountsDAO.COL_START_BALANCE + ", " + AccountsDAO.COL_IS_CLOSED + ", " + AccountsDAO.COL_ORDER + ", " + AccountsDAO.COL_CREDIT_LIMIT + ", "
                        + AccountsDAO.COL_SEARCH_STRING + ")"
                        + " SELECT " + AccountsDAO.COL_ID + ", " + AccountsDAO.COL_SYNC_FBID + ", " + AccountsDAO.COL_SYNC_TS + ", " + AccountsDAO.COL_SYNC_DELETED + ", "
                        + AccountsDAO.COL_SYNC_DIRTY + ", " + AccountsDAO.COL_SYNC_LAST_EDITED + ", " + AccountsDAO.COL_TYPE + ", " + AccountsDAO.COL_NAME + ", "
                        + AccountsDAO.COL_CURRENCY + ", " + AccountsDAO.COL_EMITENT + ", " + AccountsDAO.COL_LAST4DIGITS + ", " + AccountsDAO.COL_COMMENT + ", "
                        + AccountsDAO.COL_START_BALANCE + ", " + AccountsDAO.COL_IS_CLOSED + ", " + AccountsDAO.COL_ORDER + ", " + AccountsDAO.COL_CREDIT_LIMIT + ", "
                        + AccountsDAO.COL_SEARCH_STRING
                        + " FROM ref_Accounts_old"
            )
            db.execSQL("DROP TABLE ref_Accounts_old")
        } else super.onDowngrade(db, oldVersion, newVersion)
    }

    val sqliteVersion: String
        get() {
            val cursor = database.rawQuery("select sqlite_version() AS sqlite_version", null)
            var result = "N\\A"
            try {
                if (cursor.moveToFirst()) {
                    result = cursor.getString(0)
                }
            } finally {
                cursor.close()
            }
            return result
        }

    @Throws(IOException::class)
    fun rebuildDB() {
        val db = database
        db.beginTransaction()
        val tableNames = arrayOf(
            AccountsDAO.TABLE,
            AccountsSetsRefDAO.TABLE,
            AccountsSetsLogDAO.TABLE,
            CategoriesDAO.TABLE,
            PayeesDAO.TABLE,
            ProjectsDAO.TABLE,
            LocationsDAO.TABLE,
            DepartmentsDAO.TABLE,
            SimpleDebtsDAO.TABLE,
            TransactionsDAO.TABLE,
            TemplatesDAO.TABLE,
            SmsDAO.TABLE,
            SmsMarkersDAO.TABLE,
            CreditsDAO.TABLE,
            BudgetDAO.TABLE,
            BudgetCreditsDAO.TABLE,
            SendersDAO.TABLE,
            ProductsDAO.TABLE,
            ProductEntrysDAO.TABLE
        )
        for (tableName in tableNames) {
            db.delete(tableName, BaseDAO.COL_SYNC_DELETED + " > 0", null)
        }
        updateFullNames("ref_Categories", true, db)
        updateFullNames("ref_Payees", true, db)
        updateFullNames("ref_Projects", true, db)
        updateFullNames("ref_Locations", true, db)
        updateFullNames("ref_Departments", true, db)
        updateFullNames("ref_Accounts", false, db)
        updateFullNames("ref_SimpleDebts", false, db)
        updateFullNames("log_Templates", false, db)
        updateRunningBalance(db)
        updateLogProducts(db)
        db.setTransactionSuccessful()
        db.endTransaction()
        db.execSQL("VACUUM")
    }

    @Throws(IOException::class)
    fun updateRunningBalance(database: SQLiteDatabase) {
        val sql = readQueryFromAssets("sql/update_running_balance.sql", mContext)
        for (s in sql) {
            if (s != null && !s.isEmpty() && s != "\n") {
                Log.d(TAG, s)
                database.execSQL(s)
            }
        }
    }

    fun updateLogProducts(db: SQLiteDatabase) {
        db.execSQL("DELETE FROM log_Products WHERE TransactionID < 0")
        val cursor = db.rawQuery(
            "SELECT _id, Amount FROM log_Transactions " +
                    "WHERE _id not in (SELECT TransactionID FROM log_Products) AND Deleted = 0",
            null
        )
        if (cursor != null) {
            ProductEntrysDAO.updateLogProducts(db, cursor)
            cursor.close()
        }
    }

    private val dbPath: String
        private get() = mContext.getDatabasePath(if (mOriginDB) DATABASE_ORIGIN_NAME else DATABASE_NAME)
            .toString()

    @Throws(IOException::class)
    fun backupDB(vacuum: Boolean): File? {
        var backup: File? = null
        if (vacuum) {
            database.execSQL("VACUUM")
        }
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val backupPath = FileUtils.getExtFingenBackupFolder()
            @SuppressLint("SimpleDateFormat") val backupFile =
                SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(
                    Date()
                ) + ".zip"
            if (!backupPath.isEmpty()) {
                val preferences = PreferenceManager.getDefaultSharedPreferences(mContext)
                val password = preferences.getString("backup_password", "")
                val enableProtection = preferences.getBoolean("enable_backup_password", false)
                backup = if (enableProtection && !password!!.isEmpty()) {
                    FileUtils.zipAndEncrypt(
                        dbPath,
                        backupPath + backupFile,
                        password,
                        DATABASE_NAME
                    )
                } else {
                    FileUtils.zip(
                        dbPath,
                        backupPath + backupFile,
                        DATABASE_NAME
                    )
                }
                Log.d(TAG, String.format("File %s saved", backupFile))
            }
        }
        return backup
    }

    @Throws(IOException::class)
    fun backupToOriginDB(vacuum: Boolean): File? {
        var backup: File? = null
        if (vacuum) {
            database.execSQL("VACUUM")
        }
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            FileUtils.copyFile(dbPath, mContext.getDatabasePath(DATABASE_ORIGIN_NAME).path)
            val dbh = DBHelper(mContext, true)
            backup = dbh.backupDB(vacuum)
            dbh.close()
            mContext.deleteDatabase(DATABASE_ORIGIN_NAME)
        }
        return backup
    }

    fun showRestoreDialog(filename: String, activity: AppCompatActivity) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.ttl_confirm_action)
        builder.setMessage(R.string.msg_confirm_restore_db)

        // Set up the buttons
        builder.setPositiveButton("OK") { dialog: DialogInterface?, which: Int ->
            restoreDB(
                filename,
                activity
            )
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.show()
    }

    private inner class OnOkListener(
        var mZipFile: String,
        var mLocation: String,
        var mInput: EditText,
        var mIOnUnzipComplete: IOnUnzipComplete
    ) : DialogInterface.OnClickListener {
        override fun onClick(dialogInterface: DialogInterface, i: Int) {
            FileUtils.unzipAndDecrypt(
                mZipFile,
                mLocation,
                mInput.text.toString(),
                mIOnUnzipComplete
            )
        }
    }

    @Synchronized
    private fun restoreDB(filename: String, activity: AppCompatActivity) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val file = File(filename)
            val db = File(dbPath)
            val completeListener: IOnUnzipComplete = object : IOnUnzipComplete {
                override fun onComplete() {
                    if (db.delete()) {
                        val restored = File(db.parent + "/fingen.db.ex")
                        restored.renameTo(db)
                    }
                    val mStartActivity = Intent(mContext, ActivityMain::class.java)
                    val mPendingIntentId = 123456
                    val mPendingIntent = PendingIntent.getActivity(
                        mContext,
                        mPendingIntentId,
                        mStartActivity,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                    val mgr = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    mgr[AlarmManager.RTC, System.currentTimeMillis() + 100] = mPendingIntent
                    System.exit(0)
                }

                override fun onError() {
                    Toast.makeText(activity, "Error on restore DB", Toast.LENGTH_SHORT).show()
                }

                override fun onWrongPassword() {
                    val builder = AlertDialog.Builder(activity)
                    builder.setTitle(activity.getString(R.string.ttl_enter_password))
                    val input = activity.layoutInflater.inflate(
                        R.layout.template_edittext,
                        null
                    ) as EditText
                    input.setText("")
                    input.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    builder.setView(input)
                    builder.setPositiveButton(
                        "OK",
                        OnOkListener(file.toString(), db.parent + "/", input, this)
                    )
                    builder.show()
                    input.requestFocus()
                }
            }
            if (file.exists()) {
                val password = PreferenceManager.getDefaultSharedPreferences(mContext)
                    .getString("backup_password", "")
                FileUtils.unzipAndDecrypt(
                    file.toString(),
                    db.parent + "/",
                    password,
                    completeListener
                )
            }
        }
    }

    @Synchronized
    fun clearDB() {
        database.beginTransaction()
        val c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
        if (c.moveToFirst()) {
            while (!c.isAfterLast) {
                database.delete(c.getString(0), null, null)
                c.moveToNext()
            }
        }
        c.close()
        database.setTransactionSuccessful()
        database.endTransaction()
        database.rawQuery("VACUUM", null)
    }

    companion object {
        private var mInstance: DBHelper? = null
        private const val DATABASE_ORIGIN_NAME = "origin_fingen.db"
        private const val DATABASE_ORIGIN_VERSION = 35
        private const val DATABASE_NAME = "fingen.db"
        const val DATABASE_VERSION = 38
        const val TAG = "DBHelper"

        //common fields
        private fun getFullNameColumn(tableName: String): String {
            return "(SELECT path FROM (with recursive m(path, _id, name) AS (SELECT Name, _id, Name FROM $tableName WHERE ParentId = -1 UNION ALL  SELECT path||'\\'||t.Name, t._id, t.Name FROM $tableName t, m WHERE t.ParentId = m._id) SELECT * FROM m where _id = $tableName._id)) AS FullName"
        }

        @Throws(IOException::class)
        private fun readQueryFromAssets(name: String, context: Context): Array<String> {
            val buf = StringBuilder()
            val inputStream = context.assets.open(name)
            val `in` = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            var str: String
            while (`in`.readLine().also { str = it } != null) {
                buf.append(str)
                if (!str.isEmpty()) {
                    buf.append("\n")
                }
            }
            `in`.close()
            return buf.toString().split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }

        //<editor-fold desc="Временные таблицы для подсчета суммы транзакций">
        const val T_SEARCH_TRANSACTIONS = "search_Transactions"
        @JvmStatic
        @Synchronized
        fun getInstance(ctx: Context): DBHelper? {
            if (mInstance == null) {
                mInstance = DBHelper(ctx)
            }
            return mInstance
        }

        @JvmStatic
        fun updateFullNames(tableName: String, useFullName: Boolean, db: SQLiteDatabase) {
            val nameColumn: String
            nameColumn = if (tableName == TransactionsDAO.TABLE) {
                if (useFullName) getFullNameColumn(tableName) else TransactionsDAO.COL_COMMENT
            } else {
                if (useFullName) getFullNameColumn(tableName) else "Name"
            }
            val fields: Array<String>
            fields = if (useFullName) {
                arrayOf(
                    BaseDAO.COL_ID,
                    nameColumn,
                    BaseDAO.COL_SEARCH_STRING,
                    BaseDAO.COL_FULL_NAME
                )
            } else {
                arrayOf(BaseDAO.COL_ID, nameColumn, BaseDAO.COL_SEARCH_STRING)
            }
            db.query(tableName, fields, BaseDAO.COL_SYNC_DELETED + " = 0", null, null, null, null)
                .use { cursor ->
                    val cv = ContentValues()
                    var translit: String
                    if (cursor.moveToFirst()) {
                        while (!cursor.isAfterLast) {
                            cv.clear()
                            if (useFullName) {
                                cv.put(BaseDAO.COL_FULL_NAME, cursor.getString(1))
                            }
                            translit = Translit.toTranslit(
                                cursor.getString(1).lowercase(Locale.getDefault())
                            )
                            if (cursor.getString(2) != translit) {
                                cv.put(BaseDAO.COL_SEARCH_STRING, translit)
                            }
                            if (cv.size() != 0) {
                                db.update(tableName, cv, "_id = " + cursor.getString(0), null)
                            }
                            cursor.moveToNext()
                        }
                    }
                }
        }

        @JvmStatic
        fun addSyncDataToCV(values: ContentValues, baseModel: BaseModel): ContentValues {
            values.put(BaseDAO.COL_SYNC_FBID, baseModel.fbid)
            values.put(BaseDAO.COL_SYNC_TS, baseModel.ts)
            values.put(BaseDAO.COL_SYNC_DELETED, 0)
            values.put(BaseDAO.COL_SYNC_DIRTY, if (baseModel.isDirty) 1 else 0)
            values.put(BaseDAO.COL_SYNC_LAST_EDITED, baseModel.lastEdited)
            return values
        }

        @JvmStatic
        @Synchronized
        fun getMaxDel(db: SQLiteDatabase, tableName: String?): Int {
            val cursor = db.query(
                tableName,
                arrayOf(String.format("MAX(%s) AS MAXDEL", BaseDAO.COL_SYNC_DELETED)),
                null,
                null,
                null,
                null,
                null
            )
            var maxDel = 0
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        while (!cursor.isAfterLast) {
                            maxDel = cursor.getInt(0)
                            cursor.moveToNext()
                        }
                    }
                } finally {
                    cursor.close()
                }
            }
            return maxDel
        }
    }

    private fun tableCreateNamedApps(db: SQLiteDatabase): String {
        db.execSQL(
            "create table apps (" +
                    "id integer primary key autoincrement," +
                    " package_name text," +
                    " isActive boolean);"
        )
        return "apps"
    }

    private fun tableCreateNamedCookies(db: SQLiteDatabase): String {
        db.execSQL(
            "create table cookies (" +
                    "id integer primary key autoincrement," +
                    " email text," +
                    " access_token text, " +
                    "refresh_token text, " +
                    "server_name text);"
        )
        return "cookies"
    }

    private fun tableCreateNotifyRepeatedSend(db: SQLiteDatabase): String {
        db.execSQL(
            "create table notify_repeated_send (" +
                    "id integer primary key autoincrement," +
                    " params text," +
                    " url text);"
        )
        return "notify_repeated_send"
    }
}