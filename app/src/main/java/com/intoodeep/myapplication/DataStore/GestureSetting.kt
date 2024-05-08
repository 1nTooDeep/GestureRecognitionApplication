package com.intoodeep.myapplication.DataStore

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "gesture_states")
data class GestureSetting(@PrimaryKey(autoGenerate = true)
                          val id: Int = 0,
                          @ColumnInfo(name = "gesture_name")
                          val gestureName: String,
                          @ColumnInfo(name = "is_checked")
                          val isChecked: Boolean)

// 定义 DAO 接口
@Dao
interface GestureStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: GestureSetting)

    @Query("SELECT * FROM gesture_states WHERE gesture_name = :gestureName")
    suspend fun getStateByName(gestureName: String): GestureSetting?
}

// 定义 Room Database
@Database(entities = [GestureSetting::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gestureStateDao(): GestureStateDao
}

// 保存和读取状态
suspend fun saveSwitchState(context: Context, gestureName: String, isChecked: Boolean) {
    val db = Room.databaseBuilder(context, AppDatabase::class.java, "gesture_db").build()
    val state = GestureSetting(gestureName = gestureName, isChecked = isChecked)
    db.gestureStateDao().insertOrUpdate(state)
}

suspend fun getSwitchState(context: Context, gestureName: String): Boolean {
    val db = Room.databaseBuilder(context, AppDatabase::class.java, "gesture_db").build()
    val state = db.gestureStateDao().getStateByName(gestureName) ?: return false
    return state.isChecked
}