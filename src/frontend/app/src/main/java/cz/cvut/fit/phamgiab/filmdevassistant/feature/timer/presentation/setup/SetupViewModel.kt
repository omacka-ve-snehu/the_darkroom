package cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.presentation.setup

import androidx.lifecycle.ViewModel
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.data.TimerManager
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.domain.AgitationConfig
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.domain.RecipeConfig
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.domain.RecipeStage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SetupViewModel(
    private val initialDuration: Int,
    private val timerManager: TimerManager
) : ViewModel() {
    private val _manualDeveloperTimeStream = MutableStateFlow(initialDuration)
    val manualDeveloperTimeStream = _manualDeveloperTimeStream.asStateFlow()

    fun updateManualTime(newTimeSeconds: Int) {
        if (newTimeSeconds >= 0) {
            _manualDeveloperTimeStream.value = newTimeSeconds
        }
    }

    fun startTimer() {
        // create a placeholder recipe with the manual duration setting
        val config = RecipeConfig(
            listOf(
                RecipeStage("Prewash", 60),
                RecipeStage("Developer", _manualDeveloperTimeStream.value, AgitationConfig(60, 10)),
                RecipeStage("Stop Bath", 60),
                RecipeStage("Fixer", 180),
                RecipeStage("Rinse", 180)
            )
        )
        timerManager.loadRecipe(config)
    }
}