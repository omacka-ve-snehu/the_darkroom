package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipe")
    fun getRecipesStream() : Flow<List<DbRecipe>>

    @Query("SELECT * FROM recipe WHERE id = :id")
    fun getRecipeStream(id: Long) : Flow<DbRecipe?>

    @Query("SELECT * FROM recipe WHERE id = :id")
    suspend fun getRecipe(id: Long) : DbRecipe?

    @Query("SELECT * FROM recipe WHERE id IN (:ids)")
    suspend fun getRecipesByIds(ids: List<Long>): List<DbRecipe>

    @Upsert
    suspend fun upsert(recipe: DbRecipe)

    @Delete
    suspend fun delete(recipe: DbRecipe)
}