package com.example.appventas.repository

import com.example.appventas.model.ESTADO_OBSERVADA
import com.example.appventas.model.ESTADO_RECHAZADA
import com.example.appventas.model.ESTADO_VALIDADA
import com.example.appventas.model.calcularComision
import com.example.appventas.model.data.local.ClienteDao
import com.example.appventas.model.data.local.ClienteEntity
import com.example.appventas.model.data.local.ComisionDao
import com.example.appventas.model.data.local.ComisionEntity
import com.example.appventas.model.data.local.DatosIniciales
import com.example.appventas.model.data.local.EstadoDao
import com.example.appventas.model.data.local.EstadoEntity
import com.example.appventas.model.data.local.HistorialDao
import com.example.appventas.model.data.local.HistorialEntity
import com.example.appventas.model.data.local.VentaDao
import com.example.appventas.model.data.local.VentaEntity
import com.example.appventas.model.formatoPesos
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VentaRepository(
    private val dao: VentaDao,
    private val historialDao: HistorialDao,
    private val comisionDao: ComisionDao,
    private val clienteDao: ClienteDao,
    private val estadoDao: EstadoDao
) {

    // ---------- Catálogos: clientes ficticios y estados ----------

    // La primera vez llena las tablas con los datos de ejemplo
    suspend fun cargarDatosIniciales() {
        if (clienteDao.contar() == 0) clienteDao.insertarTodos(DatosIniciales.clientes)
        if (estadoDao.contar() == 0) estadoDao.insertarTodos(DatosIniciales.estados)
    } // fin cargarDatosIniciales

    fun obtenerClientes(): Flow<List<ClienteEntity>> = clienteDao.obtenerTodos()

    fun obtenerEstados(): Flow<List<EstadoEntity>> = estadoDao.obtenerTodos()

    // ---------- Ventas ----------

    fun obtenerVentasPorVendedor(correo: String) =
        dao.obtenerPorVendedor(correo)

    fun obtenerTodasLasVentas(): Flow<List<VentaEntity>> = dao.obtenerTodas()

    fun observarVenta(id: Long): Flow<VentaEntity?> = dao.observarPorId(id)

    suspend fun obtenerPorId(id: Long): VentaEntity? {
        return dao.obtenerPorId(id)
    } // fin obtenerPorId

    suspend fun obtenerPorCodigo(codigo: String): VentaEntity? {
        return dao.obtenerPorCodigo(codigo)
    } // fin obtenerPorCodigo

    suspend fun contarPorCodigo(codigo: String): Int {
        return dao.contarPorCodigo(codigo)
    } // fin contarPorCodigo

    // El vendedor envía una venta nueva y queda anotada en el historial
    suspend fun registrarVenta(venta: VentaEntity, usuario: String) {
        val id = dao.insertar(venta)
        anotarHistorial(
            ventaId = id,
            usuario = usuario,
            accion = "Venta registrada y enviada",
            estado = venta.estado,
            detalle = "Monto ${formatoPesos(venta.monto)}"
        )
    } // fin registrarVenta

    // El vendedor corrige una venta (en revisión u observada) y la vuelve a enviar
    suspend fun corregirVenta(venta: VentaEntity, usuario: String) {
        dao.actualizar(venta)
        anotarHistorial(
            ventaId = venta.id,
            usuario = usuario,
            accion = "Venta corregida y reenviada",
            estado = venta.estado,
            detalle = ""
        )
    } // fin corregirVenta

    suspend fun eliminar(venta: VentaEntity) {
        historialDao.eliminarPorVenta(venta.id)
        dao.eliminar(venta)
    } // fin eliminar

    // El encargado aprueba, observa o rechaza. Si queda validada se genera la comisión simulada
    suspend fun revisarVenta(
        venta: VentaEntity,
        nuevoEstado: String,
        observacion: String,
        usuario: String,
        porcentajeComision: Double
    ) {
        dao.actualizar(
            venta.copy(
                estado = nuevoEstado,
                observacionRevision = observacion.ifBlank { null }
            )
        )

        val accion = when (nuevoEstado) {
            ESTADO_VALIDADA -> "Venta aprobada (validada)"
            ESTADO_OBSERVADA -> "Venta observada (requiere corrección)"
            ESTADO_RECHAZADA -> "Venta rechazada (inválida)"
            else -> "Cambio de estado"
        }
        anotarHistorial(venta.id, usuario, accion, nuevoEstado, observacion)

        if (nuevoEstado == ESTADO_VALIDADA) {
            comisionDao.insertar(
                ComisionEntity(
                    ventaId = venta.id,
                    vendedorNombre = venta.vendedorNombre,
                    vendedorCorreo = venta.vendedorCorreo,
                    convenio = venta.tipoServicio,
                    montoVenta = venta.monto,
                    porcentaje = porcentajeComision,
                    montoComision = calcularComision(venta.monto, porcentajeComision),
                    fecha = ahora()
                )
            )
        }
    } // fin revisarVenta

    // ---------- Historial y comisiones ----------

    fun obtenerHistorial(ventaId: Long): Flow<List<HistorialEntity>> =
        historialDao.obtenerPorVenta(ventaId)

    fun obtenerComisiones(): Flow<List<ComisionEntity>> = comisionDao.obtenerTodas()

    fun obtenerComisionesPorVendedor(correo: String): Flow<List<ComisionEntity>> =
        comisionDao.obtenerPorVendedor(correo)

    private suspend fun anotarHistorial(
        ventaId: Long,
        usuario: String,
        accion: String,
        estado: String,
        detalle: String
    ) {
        historialDao.insertar(
            HistorialEntity(
                ventaId = ventaId,
                fecha = ahora(),
                usuario = usuario,
                accion = accion,
                estado = estado,
                detalle = detalle
            )
        )
    } // fin anotarHistorial

    // Fecha y hora actual: dd/mm/aaaa hh:mm
    private fun ahora(): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
}
