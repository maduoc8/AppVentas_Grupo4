package com.example.appventas.model.data.local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EstadoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(estados: List<EstadoEntity>)

    @Query("SELECT * FROM estados ORDER BY orden")
    fun obtenerTodos(): Flow<List<EstadoEntity>>

    @Query("SELECT COUNT(*) FROM estados")
    suspend fun contar(): Int
}
