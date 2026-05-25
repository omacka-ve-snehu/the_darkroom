package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.presentation.stored

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.RecipeRepository
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.domain.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoredViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {
    private val _screenStateStream = MutableStateFlow<StoredScreenState>(StoredScreenState.Empty)
    val screenStateStream = _screenStateStream.asStateFlow()

    init {
        viewModelScope.launch {
            _screenStateStream.value = StoredScreenState.Loading
            recipeRepository.getSavedRecipesStream().collect { recipes ->
                if (recipes.isEmpty()) {
                    _screenStateStream.value = StoredScreenState.Empty
                } else {
                    _screenStateStream.value = StoredScreenState.Loaded(recipes)
                }
            }
        }
    }

}

sealed interface StoredScreenState {
    data object Loading : StoredScreenState

    data object Empty : StoredScreenState

    data class Loaded(val recipes: List<Recipe>) : StoredScreenState
}