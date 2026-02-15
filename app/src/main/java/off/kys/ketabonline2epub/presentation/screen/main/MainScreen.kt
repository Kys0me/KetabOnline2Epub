package off.kys.ketabonline2epub.presentation.screen.main

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import off.kys.ketabonline2epub.common.BookType
import off.kys.ketabonline2epub.common.file_saver.DocumentType
import off.kys.ketabonline2epub.common.file_saver.FileSaveResult
import off.kys.ketabonline2epub.presentation.event.MainUiEvent
import off.kys.ketabonline2epub.presentation.screen.main.components.BookSearchToolbar
import off.kys.ketabonline2epub.presentation.screen.main.components.MainContent
import off.kys.ketabonline2epub.presentation.screen.main.components.NewUpdateDialog
import off.kys.ketabonline2epub.presentation.viewmodel.MainViewModel
import off.kys.ketabonline2epub.util.save_file.rememberFileSaver
import org.koin.androidx.compose.koinViewModel

private const val TAG = "MainScreen"

@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val saveBook = rememberFileSaver { result ->
        when (result) {
            FileSaveResult.Success -> {
                val extension = state.downloadedFile!!.extension
                when(extension.lowercase()) {
                    "pdf" -> viewModel.onEvent(MainUiEvent.MarkAsDownloaded(BookType.PDF(state.bookId)))
                    "epub" -> viewModel.onEvent(MainUiEvent.MarkAsDownloaded(BookType.PDF(state.bookId)))
                    else -> Log.w(TAG, "MainScreen: Unknown file extension")
                }
            }
            else -> Log.w(TAG, "MainScreen: File saving failed")
        }
    }
    val isDownloading = state.isLoading && state.searchResults.isNotEmpty()

    // Handle File Saving Side Effect
    LaunchedEffect(key1 = state.downloadedFile) {
        state.downloadedFile?.let { file ->
            saveBook.save(
                fileName = "${state.bookId.value} - ${file.name}".removePrefix("-").trim(),
                type = DocumentType.EPUB,
                data = file.readBytes()
            )
            viewModel.onEvent(MainUiEvent.DownloadHandled)
        }
    }

    Scaffold(
        topBar = {
            BookSearchToolbar(
                mainUiState = state,
                onEvent = viewModel::onEvent
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Loading Indicator
            if (isDownloading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            MainContent(viewModel = viewModel)
        }
    }

    if (state.isUpdateAvailable) NewUpdateDialog(
        versionName = state.newVersionName,
        changelog = state.newVersionChangelog,
        updateUrl = state.updateUrl,
    ) {
        viewModel.onEvent(MainUiEvent.OnDismissUpdateDialog)
    }
}