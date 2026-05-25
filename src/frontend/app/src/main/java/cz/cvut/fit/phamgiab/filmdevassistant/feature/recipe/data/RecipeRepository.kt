package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data

import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.api.RecipeRemoteDataSource
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.database.RecipeLocalDataSource
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.domain.Recipe
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val localDataSource: RecipeLocalDataSource,
    private val remoteDataSource: RecipeRemoteDataSource
) {
    fun getSavedRecipesStream() : Flow<List<Recipe>> {
        return localDataSource.getRecipesStream()
    }

    fun getRecipeStream(id: Long) : Flow<Recipe?> {
        return localDataSource.getRecipeStream(id)
    }

    suspend fun getRecipe(id: Long) : Recipe? {
        return localDataSource.getRecipe(id)
    }

    suspend fun saveRecipe(recipe: Recipe) {
        return localDataSource.saveRecipe(recipe)
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        localDataSource.deleteRecipe(recipe)
    }

    private suspend fun mergeRemoteLocal(remoteRecipes: List<Recipe>) : List<Recipe> {
        val ids = remoteRecipes.map { it.id }
        val localRecipes = localDataSource.getRecipesByIds(ids).associateBy { it.id }

        return remoteRecipes.map { remoteRecipe ->
            localRecipes[remoteRecipe.id] ?: remoteRecipe
        }
    }

    suspend fun searchOnline(query: String) : List<Recipe> {
        val remoteRecipes = remoteDataSource.searchRecipes(query)
        return mergeRemoteLocal(remoteRecipes)
    }

    suspend fun getRandomRecipes(limit : Int = 20) : List<Recipe> {
        val remoteRecipes = remoteDataSource.getRandomRecipes(limit)
        return mergeRemoteLocal(remoteRecipes)
    }
}