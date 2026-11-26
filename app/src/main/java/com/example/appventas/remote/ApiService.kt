package com.example.appventas.remote

import com.example.appventas.model.Convenio
import retrofit2.http.GET

// Esta interfaz define los endpoints HTTP de la API de convenios
interface ApiService {

    // Define una solicitud GET al catálogo de convenios y servicios
    @GET("api/convenios.json")
    suspend fun getConvenios(): List<Convenio>
}
