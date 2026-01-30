package off.kys.ketabonline2epub

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<BookRepository> { BookRepositoryImpl(get()) }
    single<EpubConverterRepository> { EpubConverterRepositoryImpl(get()) }

    viewModel { MainViewModel(get(), get(), get()) }
}