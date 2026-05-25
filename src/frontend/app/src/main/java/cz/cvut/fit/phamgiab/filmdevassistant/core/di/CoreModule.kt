package cz.cvut.fit.phamgiab.filmdevassistant.core.di

import cz.cvut.fit.phamgiab.filmdevassistant.core.data.api.ApiClient
import cz.cvut.fit.phamgiab.filmdevassistant.core.data.db.RecipeDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModule = module {
    single { RecipeDatabase.newInstance(androidContext()) }
    singleOf(::ApiClient)
}