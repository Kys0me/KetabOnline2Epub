package off.kys.ketabonline2epub

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import off.kys.ketabonline2epub.save_file.DocumentType
import off.kys.ketabonline2epub.save_file.rememberFileSaver
import off.kys.ketabonline2epub.ui.theme.KetabOnline2EpubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KetabOnline2EpubTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val saveBook = rememberFileSaver()
                    val searchResults = remember { mutableStateListOf<BookItem>() }
                    val viewModel = MainViewModel(
                        bookRepository = BookRepositoryImpl(this),
                        epubConverterRepository = EpubConverterRepositoryImpl(this)
                    )
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
                                modifier = Modifier.weight(1f),
                                value = bookName,
                                onValueChange = { bookName = it },
                                label = { Text(stringResource(R.string.book_name)) }
                            )
                            TextButton(
                                onClick = {
                                    viewModel.searchBook(bookName) {
                                        searchResults.clear()
                                        searchResults.addAll(it)
                                    }
                                }
                            ) {
                                Text(stringResource(R.string.search))
                            }
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items = searchResults, key = { it.id.value }) { book ->
                                ListItem(
                                    leadingContent = {
                                        AsyncImage(
                                            modifier = Modifier.size(
                                                width = 100.dp,
                                                height = 150.dp
                                            ),
                                            model = book.coverUrl,
                                            contentDescription = "Book cover"
                                        )
                                    },
                                    headlineContent = {
                                        Text(text = book.title)
                                    },
                                    supportingContent = {
                                        Text(text = book.author)
                                    },
                                    trailingContent = {
                                        TextButton(
                                            onClick = {
                                                viewModel.downloadBook(book.id) {
                                                    runOnUiThread {
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            "Downloaded",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                            .show()
                                                        it?.let { bookFile ->
                                                            saveBook.save(
                                                                fileName = bookFile.name,
                                                                type = DocumentType.ALL,
                                                                data = bookFile.readBytes()
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        ) {
                                            Text(stringResource(R.string.download))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}