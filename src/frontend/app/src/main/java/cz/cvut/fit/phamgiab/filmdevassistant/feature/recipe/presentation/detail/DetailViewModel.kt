package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.RecipeRepository
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.domain.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val recipe: Recipe,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _screenStateStream = MutableStateFlow<DetailScreenState>(DetailScreenState.Empty)
    val screenStateStream = _screenStateStream.asStateFlow()

    private val _editOptionStream = MutableStateFlow(EditOption.NONE)
    val editOptionStream = _editOptionStream.asStateFlow()

    init {
        // immediately load the passed recipe (might come from remote, then load DB version)
        _screenStateStream.value = DetailScreenState.Loaded(recipe)

        viewModelScope.launch {
            recipeRepository.getRecipeStream(recipe.id).collect { dbRecipe ->
                if (dbRecipe != null) {
                    _screenStateStream.value = DetailScreenState.Loaded(dbRecipe)
                } else {
                    // if not in DB, force isSaved to false
                    _screenStateStream.value = DetailScreenState.Loaded(recipe.copy(isSaved = false))
                }
            }
        }
    }

    fun onOptionSelected(option: EditOption) {
        if (_editOptionStream.value == option) {
            _editOptionStream.value = EditOption.NONE
        } else {
            _editOptionStream.value = option
        }
    }

    fun toggleSaveStatus() {
        viewModelScope.launch {
            val currentState = _screenStateStream.value

            if (currentState is DetailScreenState.Loaded && currentState.recipe != null) {
                val currentRecipe = currentState.recipe
                Firebase.analytics.logEvent(
                    if (currentRecipe.isSaved) "deleted_recipe" else "saved_recipe"
                ) {
                    param("recipe_id", currentRecipe.id)
                    param("time", System.currentTimeMillis())
                }
                if (currentRecipe.isSaved) {
                    recipeRepository.deleteRecipe(currentRecipe)
                } else {
                    recipeRepository.saveRecipe(currentRecipe)
                }
            }
        }
    }
}


enum class EditOption { NONE, DEVELOPER, DILUTION, TEMPERATURE, TIME }

sealed interface DetailScreenState {
    data object Empty : DetailScreenState

    data class Loaded(val recipe: Recipe?) : DetailScreenState
}