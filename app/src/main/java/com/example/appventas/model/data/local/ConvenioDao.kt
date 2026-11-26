package com.example.appventas.model.data.local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConvenioDao {

    // REPLACE: lo que llega de la API reemplaza al convenio guardado con el mismo id
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(convenios: List<ConvenioEntity>)

    @Query("SELECT * FROM convenios ORDER BY categoria, nombre")
    fun obtenerTodos(): Flow<List<ConvenioEntity>>

    @Query("SELECT * FROM convenios WHERE id = :id")
    suspend fun obtenerPorId(id: Int): ConvenioEntity?

    @Query("SELECT COUNT(*) FROM convenios")
    suspend fun contar(): Int
}
