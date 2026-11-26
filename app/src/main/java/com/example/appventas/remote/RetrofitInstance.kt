package com.example.appventas.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton que contiene la configuración de Retrofit
object RetrofitInstance {

    // Servicio externo: catálogo de convenios en formato JSON publicado en el repositorio del grupo
    private const val BASE_URL = "https://raw.githubusercontent.com/maduoc8/AppVentas_Grupo4/main/"

    // Se instancia el servicio de la API una sola vez
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // URL base de la API
            .addConverterFactory(GsonConverterFactory.create()) // Conversor JSON
            .build()
            .create(ApiService::class.java) // Implementa la interfaz ApiService
    }
}
