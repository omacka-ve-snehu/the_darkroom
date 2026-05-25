package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.database

import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.domain.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeLocalDataSource(
    private val recipeDao: RecipeDao
) {
    fun getRecipesStream() : Flow<List<Recipe>> {
        return recipeDao.getRecipesStream().map { list->
            list.map { it.toDomain() }
        }
    }

    fun getRecipeStream(id: Long) : Flow<Recipe?> {
        return recipeDao.getRecipeStream(id).map { it?.toDomain() }
    }

    suspend fun getRecipe(id: Long) : Recipe? {
        return recipeDao.getRecipe(id)?.toDomain()
    }

    suspend fun getRecipesByIds(ids: List<Long>): List<Recipe> {
        return recipeDao.getRecipesByIds(ids).map { it.toDomain() }
    }

    suspend fun saveRecipe(recipe: Recipe) {
        return recipeDao.upsert(recipe.toDb())
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.delete(recipe.toDb())
    }

    private fun DbRecipe.toDomain(): Recipe {
        return Recipe(
            id = id,
            filmStock = filmStock,
            iso = iso,
            processType = processType,
            developer = developer,
            dilution = dilution,
            temperature = temperature,
            devDuration = devDuration,
            imageUrl = imageUrl,
            isSaved = true
        )
    }

    private fun Recipe.toDb(): DbRecipe {
        return DbRecipe(
            id = id,
            filmStock = filmStock,
            iso = iso,
            processType = processType,
            developer = developer,
            dilution = dilution,
            temperature = temperature,
            devDuration = devDuration,
            imageUrl = imageUrl,
            isSaved = true
        )
    }
}