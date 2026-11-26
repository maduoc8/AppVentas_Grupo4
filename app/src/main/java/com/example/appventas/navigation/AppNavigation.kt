package com.example.appventas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appventas.model.data.local.UsuarioDatabase
import com.example.appventas.model.data.local.VentaDatabase
import com.example.appventas.repository.AuthRepository
import com.example.appventas.repository.ConvenioRepository
import com.example.appventas.repository.VentaRepository
import com.example.appventas.ui.screen.ComisionesScreen
import com.example.appventas.ui.screen.ConveniosScreen
import com.example.appventas.ui.screen.DetalleVentaScreen
import com.example.appventas.ui.screen.InicioVendedorScreen
import com.example.appventas.ui.screen.ListaVentasScreen
import com.example.appventas.ui.screen.LoginScreen
import com.example.appventas.ui.screen.MuestraDatosScreen
import com.example.appventas.ui.screen.QrScannerScreen
import com.example.appventas.ui.screen.QrVentaScreen
import com.example.appventas.ui.screen.RegistroScreen
import com.example.appventas.ui.screen.ResumenScreen
import com.example.appventas.ui.screen.VentaFormScreen
import com.example.appventas.ui.screen.VentasRecibidasScreen
import com.example.appventas.viewmodel.ComisionViewModel
import com.example.appventas.viewmodel.ConvenioViewModel
import com.example.appventas.viewmodel.LoginViewModel
import com.example.appventas.viewmodel.QrViewModel
import com.example.appventas.viewmodel.RevisionViewModel
import com.example.appventas.viewmodel.UsuarioViewModel
import com.example.appventas.viewmodel.VentaViewModel

@Composable
fun AppNavigation(
    qrViewModel: QrViewModel,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit
){
    val navController = rememberNavController()
    val context = LocalContext.current

    //  Aquí creamos el ViewModel
    val usuarioViewModel: UsuarioViewModel = viewModel()

    // Repositorios: ViewModel -> Repository -> DAO -> Room (y la API para convenios)
    val ventaDb = remember { VentaDatabase.getDatabase(context) }
    val ventaRepository = remember {
        VentaRepository(
            dao = ventaDb.ventaDao(),
            historialDao = ventaDb.historialDao(),
            comisionDao = ventaDb.comisionDao(),
            clienteDao = ventaDb.clienteDao(),
            estadoDao = ventaDb.estadoDao()
        )
    }
    val convenioRepository = remember { ConvenioRepository(ventaDb.convenioDao()) }

    // ViewModel de ventas del vendedor (lo comparten la lista, el formulario y la cámara)
    val ventaViewModel: VentaViewModel = viewModel {
        VentaViewModel(ventaRepository, convenioRepository)
    }

    // ViewModel del admin / encargado (lista recibida, detalle, validación e historial)
    val revisionViewModel: RevisionViewModel = viewModel {
        RevisionViewModel(ventaRepository, convenioRepository)
    }

    val comisionViewModel: ComisionViewModel = viewModel {
        ComisionViewModel(ventaRepository)
    }

    // Catálogo de convenios: al crearse consulta la API REST
    val convenioViewModel: ConvenioViewModel = viewModel {
        ConvenioViewModel(convenioRepository)
    }

    NavHost(
        navController=navController, startDestination = "login"
    ){ // inicio NavHost
        composable("login"){
            // El login también revisa los vendedores que el admin guardó en Room
            val loginViewModel: LoginViewModel = viewModel {
                LoginViewModel(AuthRepository(usuarioDao = UsuarioDatabase.getDatabase(context).usuarioDao()))
            }
            LoginScreen(navController=navController, vm=loginViewModel)
        }//fin composable 1

        composable(
            route="muestraDatos/{username}",
            arguments = listOf(
                navArgument("username"){
                    type= NavType.StringType
                }
            )
        ) // fin composable
        { //defino la ruta
            backStackEntry ->
            val username=backStackEntry.arguments?.getString("username").orEmpty()

            // El admin es el encargado de digitación: escucha todas las ventas recibidas
            LaunchedEffect(username) {
                revisionViewModel.iniciarEncargado(username)
            }
            MuestraDatosScreen(
                username=username,
                navController=navController,
                viewModel=usuarioViewModel,
                revisionViewModel=revisionViewModel
            )

        }

        composable("registro") {
            RegistroScreen(navController, usuarioViewModel)
        }

        composable("resumen") {
            ResumenScreen(navController, usuarioViewModel)
        }

        // Lector de códigos QR
        composable("qrScanner") {
            QrScannerScreen(
                viewModel = qrViewModel,
                hasCameraPermission = hasCameraPermission,
                onRequestPermission = onRequestPermission
            )
        }

        // ---------- Sección del vendedor ----------

        composable(
            route="inicioVendedor/{nombre}/{correo}",
            arguments = listOf(
                navArgument("nombre"){
                    type= NavType.StringType
                },
                navArgument("correo"){
                    type= NavType.StringType
                }
            )
        ) { backStackEntry ->
            val nombre=backStackEntry.arguments?.getString("nombre").orEmpty()
            val correo=backStackEntry.arguments?.getString("correo").orEmpty()

            // Carga solo las ventas de este vendedor
            LaunchedEffect(correo) {
                ventaViewModel.iniciarVendedor(nombre, correo)
            }
            InicioVendedorScreen(nombre=nombre, correo=correo, navController=navController, viewModel=ventaViewModel)
        }

        composable("listaVentas") {
            ListaVentasScreen(navController, ventaViewModel)
        }

        composable("ventaForm") {
            VentaFormScreen(
                navController = navController,
                viewModel = ventaViewModel,
                hasCameraPermission = hasCameraPermission,
                onRequestPermission = onRequestPermission
            )
        }

        // Cámara: lee el QR del código de autorización de la venta
        composable("qrVenta") {
            QrVentaScreen(
                hasCameraPermission = hasCameraPermission,
                onRequestPermission = onRequestPermission,
                navController = navController,
                viewModel = ventaViewModel
            )
        }

        // Detalle de una venta del vendedor (estado, comprobante e historial)
        composable(
            route = "detalleVenta/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            DetalleVentaScreen(
                ventaId = id,
                puedeRevisar = false,
                navController = navController,
                viewModel = revisionViewModel,
                onCorregir = { venta ->
                    ventaViewModel.cargarVentaParaEditar(venta)
                    navController.navigate("ventaForm")
                }
            )
        }

        // ---------- Sección del admin / encargado de digitación ----------

        composable("ventasRecibidas") {
            VentasRecibidasScreen(navController, revisionViewModel)
        }

        // Revisar comprobante, validar código y aprobar / observar / rechazar
        composable(
            route = "revisionVenta/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            DetalleVentaScreen(
                ventaId = id,
                puedeRevisar = true,
                navController = navController,
                viewModel = revisionViewModel
            )
        }

        // ---------- Compartidas ----------

        // Sin correo: comisiones de todos (admin). Con correo: solo las del vendedor
        composable(
            route = "comisiones?correo={correo}",
            arguments = listOf(
                navArgument("correo") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val correo = backStackEntry.arguments?.getString("correo")

            LaunchedEffect(correo) {
                comisionViewModel.cargar(correo)
            }
            ComisionesScreen(navController, comisionViewModel)
        }

        // Catálogo de convenios y servicios (API REST)
        composable("convenios") {
            ConveniosScreen(navController, convenioViewModel)
        }


    }//Fin Nav Host
}//fin AppNavigation
