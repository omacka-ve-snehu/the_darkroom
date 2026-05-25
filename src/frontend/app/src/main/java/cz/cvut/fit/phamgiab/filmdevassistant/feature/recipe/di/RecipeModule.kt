package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.di

import cz.cvut.fit.phamgiab.filmdevassistant.core.data.db.RecipeDatabase
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.RecipeRepository
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.api.RecipeRemoteDataSource
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.database.RecipeLocalDataSource
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.presentation.detail.DetailViewModel
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.presentation.discover.DiscoverViewModel
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.presentation.stored.StoredViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val recipeModule = module {
    factoryOf(::RecipeRemoteDataSource)

    single { get<RecipeDatabase>().recipeDao() }
    factoryOf(::RecipeLocalDataSource)

    singleOf(::RecipeRepository)

    viewModelOf(::DetailViewModel)
    viewModelOf(::DiscoverViewModel)
    viewModelOf(::StoredViewModel)
}