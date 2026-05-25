package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe")
data class DbRecipe (
    @PrimaryKey val id: Long,
    val filmStock: String,
    val iso: Int,
    val processType: String,
    val developer: String,
    val dilution: String,
    val temperature: Int,
    val devDuration: Int,
    val imageUrl: String,
    val isSaved: Boolean
)