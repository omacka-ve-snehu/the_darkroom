package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.api

import cz.cvut.fit.phamgiab.filmdevassistant.core.data.api.ApiClient
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.domain.Recipe
import io.ktor.http.HttpMethod

class RecipeRemoteDataSource (
    private val apiClient: ApiClient
) {
    suspend fun searchRecipes(query: String) : List<Recipe> {
        val response = apiClient.request<RecipeResponse>(
            endpoint = "search",
            method = HttpMethod.Get
        ) {
            url {
                if (query.isNotBlank()) parameters.append("q", query)
            }
        }
        return response.results.map { it.toDomainDraft() }
    }

    suspend fun getRandomRecipes(limit: Int = 20) : List<Recipe> {
        val response = apiClient.request<RecipeResponse>(
            endpoint = "random",
            method = HttpMethod.Get
        ) {
            url {
                parameters.append("limit", limit.toString())
            }
        }
        return response.results.map { it.toDomainDraft() }
    }

    /**
     * Maps ApiRecipe data class to domain Recipe data class. (Extracts only relevant information from API results)
     * By default selects 35mm format times as 35mm and 120 times are virtually the same.
     *
     * Currently only B&W process is supported as MDC contains only B&W process times.
     */
    private fun ApiRecipe.toDomainDraft(
        is120Format: Boolean = false
    ): Recipe {
        var devTime = time135
        if (is120Format && time120 > 0) {
            devTime = time120
        }

        val boxImage = if (is120Format && imageUrl120.isNotEmpty()) imageUrl120 else imageUrl135

        return Recipe(
            id = this.id,
            filmStock = this.filmStock,
            iso = this.iso,
            processType = "B&W",
            developer = this.developer,
            dilution = this.dilution,
            temperature = this.temp,
            devDuration = devTime,
            imageUrl = boxImage,
            isSaved = false
        )
    }
}