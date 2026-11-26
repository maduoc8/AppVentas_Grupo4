package com.example.appventas.model.data.local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ComisionDao {

    @Insert
    suspend fun insertar(comision: ComisionEntity)

    // Todas las comisiones (lo que ve el encargado)
    @Query("SELECT * FROM comisiones ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<ComisionEntity>>

    // Solo las del vendedor conectado
    @Query("SELECT * FROM comisiones WHERE vendedorCorreo = :correo ORDER BY id DESC")
    fun obtenerPorVendedor(correo: String): Flow<List<ComisionEntity>>
}
