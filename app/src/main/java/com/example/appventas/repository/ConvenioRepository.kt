package com.example.appventas.repository

import com.example.appventas.model.Convenio
import com.example.appventas.model.data.local.ConvenioDao
import com.example.appventas.model.data.local.ConvenioEntity
import com.example.appventas.model.data.local.DatosIniciales
import com.example.appventas.remote.ApiService
import com.example.appventas.remote.RetrofitInstance
import kotlinx.coroutines.flow.Flow

// Este repositorio junta las dos fuentes de convenios: la API REST (Retrofit) y Room
class ConvenioRepository(
    private val dao: ConvenioDao,
    private val api: ApiService = RetrofitInstance.api
) {

    // Lo que se muestra en pantalla siempre viene de Room (funciona sin internet)
    fun obtenerConvenios(): Flow<List<ConvenioEntity>> = dao.obtenerTodos()

    suspend fun obtenerPorId(id: Int): ConvenioEntity? = dao.obtenerPorId(id)

    // Si Room está vacío, carga el catálogo base
    suspend fun cargarConveniosIniciales() {
        if (dao.contar() == 0) dao.insertarTodos(DatosIniciales.convenios)
    } // fin cargarConveniosIniciales

    // Consulta la API y guarda el catálogo en Room; devuelve cuántos convenios llegaron
    suspend fun sincronizarDesdeApi(): Int {
        val convenios = api.getConvenios()
        dao.insertarTodos(convenios.map { it.aEntity() })
        return convenios.size
    } // fin sincronizarDesdeApi

    // Convierte el convenio que llega en JSON a la tabla de Room
    private fun Convenio.aEntity() = ConvenioEntity(
        id = id,
        nombre = nombre,
        categoria = categoria,
        descripcion = descripcion,
        condiciones = condiciones,
        comision = comision
    )
}
