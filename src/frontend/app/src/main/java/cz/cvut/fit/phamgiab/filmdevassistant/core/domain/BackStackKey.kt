package cz.cvut.fit.phamgiab.filmdevassistant.core.domain

import androidx.navigation3.runtime.NavKey
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.domain.Recipe
import kotlinx.serialization.Serializable

sealed class BackStackKey : NavKey {
    open val showBottomBar : Boolean = true

    @Serializable
    data object Discover : BackStackKey()

    @Serializable
    data object Stored : BackStackKey()

    @Serializable
    data class RecipeDetail(val recipe: Recipe) : BackStackKey() {
        override val showBottomBar = false
    }

    @Serializable
    data class TimerSetup(val initialDuration: Int) : BackStackKey()

    @Serializable
    data object TimerRunning : BackStackKey()


}