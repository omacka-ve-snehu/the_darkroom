package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.domain

import kotlinx.serialization.Serializable

@Serializable // serializable so that it can be passed in the navigation graph
data class Recipe(
    val id: Long,
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