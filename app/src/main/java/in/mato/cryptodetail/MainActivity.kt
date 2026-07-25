package `in`.mato.cryptodetail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat
import `in`.mato.cryptodetail.presentation.cryptodetail.CryptoDetailRoute
import `in`.mato.cryptodetail.presentation.cryptolist.CryptoListRoute
import `in`.mato.cryptodetail.ui.theme.CryptoDetailTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        setContent {
            CryptoDetailTheme {
                CryptoNavGraph()
            }
        }
    }
}

@Composable
private fun CryptoNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "coins") {
        composable("coins") {
            CryptoListRoute(onCoinClick = { coinId -> navController.navigate("coin/$coinId") })
        }
        composable("coin/{coinId}") {
            CryptoDetailRoute(onBack = navController::popBackStack)
        }
    }
}
