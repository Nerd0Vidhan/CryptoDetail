package `in`.mato.cryptodetail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import `in`.mato.cryptodetail.presentation.cryptolist.CryptoListRoute
import `in`.mato.cryptodetail.ui.theme.CryptoDetailTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoDetailTheme {
                CryptoListRoute()
            }
        }
    }
}
