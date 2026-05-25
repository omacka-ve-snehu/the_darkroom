package cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.di

import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.data.TimerNotificationHelper
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.data.TimerManager
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.presentation.running.RunningViewModel
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.presentation.setup.SetupViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val timerModule = module {
    single { TimerNotificationHelper(androidContext()) }
    single { TimerManager(get()) }

    viewModelOf(::RunningViewModel)
    viewModelOf(::SetupViewModel)
}