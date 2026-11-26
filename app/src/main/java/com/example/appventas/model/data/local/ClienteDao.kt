package com.example.appventas.model.data.local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {

    // REPLACE: si el cliente ya existe (mismo id) se reemplaza y no se duplica
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(clientes: List<ClienteEntity>)

    @Query("SELECT * FROM clientes ORDER BY nombre")
    fun obtenerTodos(): Flow<List<ClienteEntity>>

    @Query("SELECT COUNT(*) FROM clientes")
    suspend fun contar(): Int
}
