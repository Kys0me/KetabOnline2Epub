package off.kys.ketabonline2epub

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import off.kys.ketabonline2epub.data.repository.BookRepositoryImpl
import org.junit.Test

class BookRepositoryImplTest {

    @Test
    fun testSearchBooks() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = BookRepositoryImpl(context)
        val query = "الرفق بالزوجات"
        repository.searchBooks(query, 1).forEach(::println)
        assert(true)
    }

}