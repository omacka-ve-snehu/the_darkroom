package cz.cvut.fit.phamgiab.filmdevassistant

import android.app.Application
import cz.cvut.fit.phamgiab.filmdevassistant.core.di.coreModule
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.di.recipeModule
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.di.timerModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application () {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(coreModule, recipeModule, timerModule)
        }
    }
}