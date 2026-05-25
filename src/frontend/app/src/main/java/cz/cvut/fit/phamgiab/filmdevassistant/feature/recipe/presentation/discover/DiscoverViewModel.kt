package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.presentation.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.cvut.fit.phamgiab.filmdevassistant.core.domain.HttpException
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.data.RecipeRepository
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.domain.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {
    private val _screenStateStream = MutableStateFlow<DiscoverScreenState>(DiscoverScreenState.Empty())
    val screenStateStream = _screenStateStream.asStateFlow()

    private val _query = MutableStateFlow("")
    val queryStream = _query.asStateFlow()


    init {
        viewModelScope.launch {
            _query
                .debounce(500)
                .collectLatest { query->
                    _screenStateStream.value = DiscoverScreenState.Loading(query)
                    try {
                        if (query.isBlank()) {
                            _screenStateStream.value = DiscoverScreenState.Loading()

                            val randomRecipes = recipeRepository.getRandomRecipes()
                            if (randomRecipes.isEmpty()) {
                                _screenStateStream.value = DiscoverScreenState.Error()
                            } else {
                                _screenStateStream.value = DiscoverScreenState.Loaded(
                                    query = query,
                                    recipes = randomRecipes
                                )
                            }

                        } else {
                            _screenStateStream.value = DiscoverScreenState.Loaded(
                                query = query,
                                recipes = recipeRepository.searchOnline(query)
                            )
                        }
                    } catch(e: Exception) {
                        if (e is HttpException && e.statusCode == 404) {
                            _screenStateStream.value = DiscoverScreenState.NoMatch(query)
                        } else {
                            _screenStateStream.value = DiscoverScreenState.Error(query)
                        }

                    }
                }
        }
    }

    fun searchCharacters(query: String) {
        _query.value = query
    }

    fun clearText() {
        _query.value = ""
    }
}

sealed interface DiscoverScreenState {
    val query: String
    data class Loading(override val query: String = "") : DiscoverScreenState

    data class Error(override val query: String = "") : DiscoverScreenState

    data class Empty(override val query: String = "") : DiscoverScreenState

    data class NoMatch(override val query: String= "") : DiscoverScreenState

    data class Loaded(override val query: String = "", val recipes: List<Recipe>) : DiscoverScreenState
}