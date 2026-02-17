package off.kys.ketabonline2epub.di

import off.kys.ketabonline2epub.data.repository.BookRepositoryImpl
import off.kys.ketabonline2epub.data.repository.DownloadStatusRepositoryImpl
import off.kys.ketabonline2epub.data.repository.EpubConverterRepositoryImpl
import off.kys.ketabonline2epub.domain.repository.BookRepository
import off.kys.ketabonline2epub.domain.repository.DownloadStatusRepository
import off.kys.ketabonline2epub.domain.repository.EpubConverterRepository
import off.kys.ketabonline2epub.presentation.viewmodel.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<BookRepository> { BookRepositoryImpl(get()) }
    single<EpubConverterRepository> { EpubConverterRepositoryImpl(get()) }
    single<DownloadStatusRepository> { DownloadStatusRepositoryImpl(get()) }

    viewModel {
        MainViewModel(
            application = get(),
            bookRepository = get(),
            epubConverterRepository = get(),
            downloadStatus = get()
        )
    }
}