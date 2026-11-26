package com.example.appventas.model.data.local

import com.example.appventas.model.ESTADO_EN_REVISION
import com.example.appventas.model.ESTADO_OBSERVADA
import com.example.appventas.model.ESTADO_RECHAZADA
import com.example.appventas.model.ESTADO_VALIDADA

// Datos de ejemplo que se cargan en Room la primera vez (todos ficticios)
object DatosIniciales {

    // Vendedor para probar sin tener que crearlo (el admin / encargado es admin / 123)
    val vendedorDePrueba = UsuarioEntity(
        nombre = "vendedor",
        correo = "vendedor@appventas.cl",
        clave = "123456",
        direccion = "Av. Ficticia 123"
    )

    val clientes = listOf(
        ClienteEntity(1, "Ana Torres", "11.111.111-1", "+56 9 0000 0001"),
        ClienteEntity(2, "Bruno Díaz", "22.222.222-2", "+56 9 0000 0002"),
        ClienteEntity(3, "Carla Muñoz", "33.333.333-3", "+56 9 0000 0003"),
        ClienteEntity(4, "Diego Rojas", "44.444.444-4", "+56 9 0000 0004"),
        ClienteEntity(5, "Elena Soto", "55.555.555-5", "+56 9 0000 0005")
    )

    // Catálogo base para trabajar sin internet (la API trae la versión completa)
    val convenios = listOf(
        ConvenioEntity(
            1, "Dental Sonrisa Total", "Dental",
            "Limpieza, tapaduras y urgencias dentales",
            "Hasta 40% de descuento en prestaciones. Carencia de 30 días.", 5.0
        ),
        ConvenioEntity(
            2, "Taller AutoCheck", "Automotriz",
            "Mantención preventiva y revisión pre-técnica",
            "20% de descuento en mano de obra. Solo vehículos particulares.", 4.0
        ),
        ConvenioEntity(
            3, "Clínica VetAmigo", "Veterinario",
            "Consultas y vacunas para mascotas",
            "Consulta a precio preferente. Máximo 2 mascotas por cliente.", 6.0
        ),
        ConvenioEntity(
            4, "Óptica VisiónClara", "Otros",
            "Lentes ópticos y de sol",
            "25% de descuento en marcos. No acumulable con otras promociones.", 3.0
        )
    )

    val estados = listOf(
        EstadoEntity(ESTADO_EN_REVISION, "Enviada por el vendedor, espera revisión", 1),
        EstadoEntity(ESTADO_OBSERVADA, "Requiere corrección del vendedor", 2),
        EstadoEntity(ESTADO_VALIDADA, "Aprobada por el encargado de digitación", 3),
        EstadoEntity(ESTADO_RECHAZADA, "Inválida: no se puede modificar", 4)
    )
}
