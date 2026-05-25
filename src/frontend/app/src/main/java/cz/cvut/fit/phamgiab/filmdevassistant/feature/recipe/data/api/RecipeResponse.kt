package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.api

import kotlinx.serialization.Serializable

@Serializable
data class RecipeResponse(val results: List<ApiRecipe>)