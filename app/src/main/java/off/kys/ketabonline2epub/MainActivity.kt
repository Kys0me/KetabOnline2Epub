package off.kys.ketabonline2epub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.MainScope
import off.kys.ketabonline2epub.ui.theme.KetabOnline2EpubTheme

inline fun <I, O> I?.ifNull(block: () -> O) = this ?: block()

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