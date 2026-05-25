package cz.cvut.fit.phamgiab.filmdevassistant.core.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.database.RecipeDao
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.database.DbRecipe
@Database(version = 1, entities = [DbRecipe::class])
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao() : RecipeDao

    companion object {
        fun newInstance(context: Context) : RecipeDatabase {
            return Room.databaseBuilder(context, RecipeDatabase::class.java, "recipe.db").build()
        }
    }
}
