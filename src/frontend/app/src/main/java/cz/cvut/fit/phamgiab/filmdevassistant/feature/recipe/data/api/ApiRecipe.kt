package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.api

import kotlinx.serialization.Serializable

@Serializable
data class ApiRecipe (
    val id: Long,
    val filmStock: String,
    val developer: String,
    val dilution: String,
    val iso: Int,
    val temp: Int,
    val time135: Int,
    val time120: Int,
    val imageUrl135: String,
    val imageUrl120: String
) {
}