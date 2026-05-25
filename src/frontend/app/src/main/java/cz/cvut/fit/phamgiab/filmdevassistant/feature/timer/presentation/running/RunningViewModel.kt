package cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.presentation.running

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.data.TimerManager
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.domain.TimerState
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.domain.TimerStateCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn

class RunningViewModel(
    private val timerManager: TimerManager
) : ViewModel() {

    // Combines three state flows of TimerManager and calculates the TimerState
    val timerStateStream : StateFlow<TimerState> = combine(
        timerManager.elapsedSeconds,
        timerManager.isRunning,
        timerManager.activeRecipe.filterNotNull(),
        TimerStateCalculator::calculate
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = TimerState()
    )

    fun toggleTimer() {
        Firebase.analytics.logEvent(
            if (timerManager.isRunning.value) "stopped_timer" else "started_timer"
        ) {
            param("elapsed_time", timerManager.elapsedSeconds.value.toLong())
            param("time", System.currentTimeMillis())
        }

        if (timerManager.isRunning.value) {
            timerManager.pause()
        } else {
            timerManager.start()
        }
    }

    fun resetTimer() {
        timerManager.reset()
    }

    fun skipCurrentStage() {
        timerManager.addSeconds(timerStateStream.value.stageRemainingSeconds)
    }
}