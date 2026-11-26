package com.example.appventas.model.data.local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialDao {

    @Insert
    suspend fun insertar(registro: HistorialEntity)

    // Del más antiguo al más nuevo, para leerlo como una línea de tiempo
    @Query("SELECT * FROM historial WHERE ventaId = :ventaId ORDER BY id ASC")
    fun obtenerPorVenta(ventaId: Long): Flow<List<HistorialEntity>>

    @Query("DELETE FROM historial WHERE ventaId = :ventaId")
    suspend fun eliminarPorVenta(ventaId: Long)
}
