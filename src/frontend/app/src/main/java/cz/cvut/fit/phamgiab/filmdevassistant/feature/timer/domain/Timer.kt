package cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.domain

import kotlinx.serialization.Serializable

/**
 * Configuration for the agitation process during a specific development stage.
 * @param intervalSeconds Duration of the interval between agitations' starts.
 * @param durationSeconds How long agitation lasts each interval.
 */
@Serializable
data class AgitationConfig(
    val intervalSeconds: Int,
    val durationSeconds: Int
)

/**
 * Represents a single stage in the development process.
 */
@Serializable
data class RecipeStage(
    val name: String,
    val durationSeconds: Int,
    val agitationConfig: AgitationConfig? = null
)

/**
 * The recipe configuration containing all development stages.
 */
@Serializable
data class RecipeConfig(
    val stages: List<RecipeStage>
) {
    val totalDuration : Int = stages.sumOf { it.durationSeconds }
}

data class TimerState(
    val activeRecipe: RecipeConfig? = null,
    val currentStageName: String = "",
    val currentStageIndex: Int = 0,
    val totalStages: Int = 0,
    val stageRemainingSeconds: Int = 0,
    val stageProgress: Float = 0f,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val isAgitatingNow: Boolean = false,
    val actionCountdownSeconds: Int? = null, // time between next agitation and non-agitation
    val nextStageName: String = ""
)

/**
 * Object encapsulating the function responsible for calculating a TimerState from TimerRepository flows.
 */
object TimerStateCalculator {
    fun calculate(elapsedSeconds: Int, isRunning: Boolean, recipeConfig: RecipeConfig) : TimerState {
        val isFinished = elapsedSeconds >= recipeConfig.totalDuration

        val (stageIndex, stage, stageRemaining) = stageAt(elapsedSeconds, recipeConfig)
        val elapsedInStage = stage.durationSeconds - stageRemaining

        val (isAgitating, actionCountdown) = calculateAgitation(
            config = stage.agitationConfig.takeIf { !isFinished },
            elapsedSeconds = elapsedInStage
        )

        return TimerState(
            currentStageName = if (isFinished) "DONE" else stage.name,
            currentStageIndex = stageIndex,
            totalStages = recipeConfig.stages.size,
            stageRemainingSeconds = stageRemaining,
            stageProgress = if (isFinished) 1f else 1f - stageRemaining.toFloat() / stage.durationSeconds,
            isRunning = isRunning,
            isFinished = isFinished,
            isAgitatingNow = isAgitating,
            actionCountdownSeconds = actionCountdown?.takeIf { it <= stageRemaining },
            nextStageName = nextStageNameAfter(stageIndex, recipeConfig)
        )
    }

    /**
     * Determines which stage in the development process is currently active based on the total elapsed time.
     * @return A triplet containing the active stage index, the stage object itself, and the seconds remaining in this specific stage.
     */
    fun stageAt(elapsedSeconds: Int, recipeConfig: RecipeConfig): Triple<Int, RecipeStage, Int> {
        val stages = recipeConfig.stages
        var accum = 0
        for ((index, stage) in stages.withIndex()) {
            if (elapsedSeconds < accum + stage.durationSeconds) {
                val remaining = accum + stage.durationSeconds - elapsedSeconds
                return Triple(index, stage, remaining)
            }
            accum += stage.durationSeconds
        }
        return Triple(stages.lastIndex, stages.last(), 0)
    }

    /**
     * Calculates the agitation timings if AgitationConfig is provided.
     * @return A pair of a boolean indicating agitation is in process and how long until next stage change.
     */
    private fun calculateAgitation(config: AgitationConfig?, elapsedSeconds: Int): Pair<Boolean, Int?> {
        if (config == null) return Pair(false, null)

        val elapsedInInterval = elapsedSeconds % config.intervalSeconds
        return if (elapsedInInterval < config.durationSeconds) {
            Pair(true, config.durationSeconds - elapsedInInterval)
        } else {
            Pair(false, config.intervalSeconds - elapsedInInterval)
        }
    }


    private fun nextStageNameAfter(index: Int, recipeConfig: RecipeConfig) : String {
        val stage = recipeConfig.stages.getOrNull(index + 1)
        return stage?.name ?: ""
    }
}