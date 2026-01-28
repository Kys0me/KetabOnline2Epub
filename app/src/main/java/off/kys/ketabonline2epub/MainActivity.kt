package off.kys.ketabonline2epub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import off.kys.ketabonline2epub.ui.theme.KetabOnline2EpubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KetabOnline2EpubTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel = MainViewModel()
                    var bookName by remember {
                        mutableStateOf("")
                    }

                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        Row {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = bookName,
                                onValueChange = { bookName = it },
                                label = { Text(stringResource(R.string.book_name)) }
                            )
                            TextButton(
                                onClick = {
                                    viewModel.searchBook(bookName)
                                }
                            ) {
                                Text(stringResource(R.string.download))
                            }
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items = emptyList<String>()) {

                            }
                        }
                    }
                }
            }
        }
    }
}