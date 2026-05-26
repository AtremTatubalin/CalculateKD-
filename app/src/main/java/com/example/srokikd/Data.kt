package com.example.srokikd

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.ds by preferencesDataStore("norms")

@Entity(tableName = "history")
data class HistoryEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val project: String, val profile: String, val labor: Double, val days: Int, val payload: String)
@Dao interface HistoryDao { @Query("SELECT * FROM history ORDER BY id DESC") fun all(): Flow<List<HistoryEntity>>; @Insert suspend fun insert(entity: HistoryEntity); @Query("DELETE FROM history WHERE id=:id") suspend fun delete(id: Long) }
@Database(entities=[HistoryEntity::class], version=1, exportSchema=false) abstract class AppDb: RoomDatabase(){ abstract fun historyDao(): HistoryDao }

class Repos(context: Context) {
    private val db = Room.databaseBuilder(context, AppDb::class.java, "app.db").build()
    val history = db.historyDao().all()
    private val key = stringPreferencesKey("config")
    suspend fun saveHistory(e: HistoryEntity) = db.historyDao().insert(e)
    suspend fun deleteHistory(id: Long) = db.historyDao().delete(id)
    suspend fun loadConfig(): NormConfig { val raw = context.ds.data.map{it[key]}.first(); return if (raw.isNullOrBlank()) Defaults.config() else Json.decodeFromString(SerNorm.serializer(), raw).toNorm() }
    suspend fun saveConfig(c: NormConfig) { context.ds.edit { it[key] = Json.encodeToString(SerNorm.from(c)) } }
}

@kotlinx.serialization.Serializable data class SerNorm(val base: Map<String, Map<String, Double>>, val character: Map<String, Double>, val pkg: Map<String, Double>, val inputs: Map<String, Double>, val approval: Map<String, Double>) {
    fun toNorm() = NormConfig(base.mapKeys { ProductProfile.valueOf(it.key) }.mapValues { e -> e.value.mapKeys { Scale.valueOf(it.key) } }, character, pkg, inputs, approval)
    companion object { fun from(c: NormConfig) = SerNorm(c.baseHours.mapKeys { it.key.name }.mapValues { e -> e.value.mapKeys { it.key.name } }, c.character, c.pkg, c.inputs, c.approval) }
}
