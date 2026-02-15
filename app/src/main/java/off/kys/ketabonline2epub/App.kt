package off.kys.ketabonline2epub

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import off.kys.ketabonline2epub.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Clear app cache and start up
        cacheDir.listFiles()?.forEach { it.deleteRecursively() }

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        // Initialize The PDFBox Resource Loader
        PDFBoxResourceLoader.init(this)
    }

}