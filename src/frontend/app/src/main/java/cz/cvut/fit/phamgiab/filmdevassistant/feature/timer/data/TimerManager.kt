package cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.data

import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.domain.RecipeConfig
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.domain.TimerStateCalculator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * A class responsible for running a coroutine in the background for the actual timer.
 * Serves as the single source of truth, observed by the ViewModels.
 * Handles the communication between the state of the timer and notification updates as well.
 *
 * In the future, a more robust implementation through a foreground service would be more appropriate.
 */
class TimerManager(
    private val timerNotificationHelper: TimerNotificationHelper,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val second = 1000L
    private val timerScope = CoroutineScope(defaultDispatcher)
    private var timerJob: Job? = null

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds = _elapsedSeconds.asStateFlow()

    private val _activeRecipe = MutableStateFlow<RecipeConfig?>(null)
    val activeRecipe = _activeRecipe.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    fun loadRecipe(recipe: RecipeConfig) {
        pause()
        _activeRecipe.value = recipe
        _elapsedSeconds.value = 0
    }

    fun start() {
        if (_isRunning.value || _activeRecipe.value == null) return
        _isRunning.value = true

        timerJob = timerScope.launch {
            while (true) {
                val activeRecipe = _activeRecipe.value
                val elapsedSeconds = _elapsedSeconds.value

                if (activeRecipe != null) {
                    if (elapsedSeconds >= activeRecipe.totalDuration) {
                        timerNotificationHelper.dismiss()
                        pause()
                        break
                    } else {
                        // extract necessary information for the persistent notification
                        val (_, stage, remaining) = TimerStateCalculator.stageAt(elapsedSeconds, activeRecipe)
                        timerNotificationHelper.updateTimerNotification(stage.name, remaining)
                    }
                } else {
                    break
                }

                delay(second)
                _elapsedSeconds.value += 1
            }
        }
    }

    fun pause() {
        timerJob?.cancel()
        _isRunning.value = false
    }

    fun reset() {
        pause()
        _elapsedSeconds.value = 0
        timerNotificationHelper.dismiss()
    }

    fun addSeconds(seconds: Int) {
        _elapsedSeconds.value += seconds
    }
}