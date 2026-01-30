package off.kys.ketabonline2epub.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import off.kys.ketabonline2epub.presentation.screen.main.MainScreen
import off.kys.ketabonline2epub.presentation.theme.KetabOnline2EpubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KetabOnline2EpubTheme {
                MainScreen()
            }
        }
    }
}