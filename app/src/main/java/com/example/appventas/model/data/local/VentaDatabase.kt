package com.example.appventas.model.data.local


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Base aparte para las ventas: así UsuarioDatabase queda igual
// Versión 2: se agregan clientes, convenios, estados, historial y comisiones
@Database(
    entities = [
        VentaEntity::class,
        ClienteEntity::class,
        ConvenioEntity::class,
        EstadoEntity::class,
        HistorialEntity::class,
        ComisionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class VentaDatabase : RoomDatabase() {

    abstract fun ventaDao(): VentaDao
    abstract fun clienteDao(): ClienteDao
    abstract fun convenioDao(): ConvenioDao
    abstract fun estadoDao(): EstadoDao
    abstract fun historialDao(): HistorialDao
    abstract fun comisionDao(): ComisionDao

    companion object {
        @Volatile
        private var INSTANCE: VentaDatabase? = null

        fun getDatabase(context: Context): VentaDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    VentaDatabase::class.java,
                    "venta_database"
                )
                    // Si cambia la versión se recrea la base (datos de prueba, no reales)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { INSTANCE = it }
            }
        } // fin getDatabase
    }
}
