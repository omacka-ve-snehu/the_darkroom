package cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.presentation.running

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import cz.cvut.fit.phamgiab.filmdevassistant.R
import cz.cvut.fit.phamgiab.filmdevassistant.core.domain.toTimerString
import cz.cvut.fit.phamgiab.filmdevassistant.core.presentation.common.EmptyState
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.domain.TimerState
import org.koin.androidx.compose.koinViewModel

@Composable
fun RunningScreen(
    onBackToSetup: () -> Unit,
    viewModel: RunningViewModel = koinViewModel()
) {
    val timerState by viewModel.timerStateStream.collectAsStateWithLifecycle()

    NotificationPermissionHandler(
        content = {
            RunningScreen(
                timerState = timerState,
                onToggleTimer = viewModel::toggleTimer,
                onResetTimer = viewModel::resetTimer,
                onSkipTimer = viewModel::skipCurrentStage,
                onBackToSetup = {
                    viewModel.resetTimer()
                    onBackToSetup()
                }
            )
        }
    )
}

/**
 * Handles the notification permissions and the appropriate UI.
 * If permission is granted, shows content.
 * If permission is denied multiple times, shows an empty screen with instructions.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionHandler(
    content: @Composable () -> Unit
) {
    val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    if (permissionState == null || permissionState.status.isGranted) {
        content()
    } else {
        if (permissionState.status.shouldShowRationale)  {
            AlertDialog(
                onDismissRequest = { },
                title = { Text(text = stringResource(R.string.permission_request_title)) },
                text = { Text(stringResource(R.string.permission_request_rationale)) },
                confirmButton = {
                    Button(onClick = { permissionState.launchPermissionRequest() }) {
                        Text(text = stringResource(R.string.ok_text))
                    }
                }
            )
        } else {
            LaunchedEffect(Unit) {
                permissionState.launchPermissionRequest()
            }
            EmptyState(stringResource(R.string.permission_request_denied))
        }
    }
}

@Composable
private fun RunningScreen(
    timerState: TimerState,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSkipTimer: () -> Unit,
    onBackToSetup: () -> Unit
) {
    Scaffold(
        contentColor = MaterialTheme.colorScheme.background
    ) { innerPadding->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 25.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            StageProgressBar(
                stageName = timerState.currentStageName,
                currentProgress = timerState.stageProgress
            )
            TotalProgressBar(
                currentStage = timerState.currentStageIndex + timerState.isFinished.compareTo(false),
                totalStages = timerState.totalStages
            )
            Timer(
                stageRemaining = timerState.stageRemainingSeconds,
                actionCountDown = timerState.actionCountdownSeconds,
                isAgitating = timerState.isAgitatingNow
            )
            AnimatedVisibility(
                visible = timerState.nextStageName.isNotEmpty(),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 250, easing = EaseOut)
                ) + shrinkVertically(
                    animationSpec = tween(durationMillis = 250, easing = EaseOut)
                )
            ) {
                InfoBar(
                    nextStageName = timerState.nextStageName ?: stringResource(R.string.no_next_stage)
                )
            }
            Controls(
                isRunning = timerState.isRunning,
                onPlayPauseClick = onToggleTimer,
                onResetClick = onResetTimer,
                onSkipClick = onSkipTimer,
                onBackToSetupClick = onBackToSetup
            )
        }
    }
}

@Composable
private fun StageProgressBar(stageName: String, currentProgress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearEasing
        ),
    ) // animates progress bar

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = stageName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(12.dp)
        )
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline,
            strokeCap = StrokeCap.Square,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
    }
}

@Composable
private fun TotalProgressBar(currentStage: Int, totalStages: Int) {
    val infiniteTransition = rememberInfiniteTransition() // pulsating animation indicating current stage
    val pulsingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until totalStages) {
            val segmentColor = when {
                i < currentStage -> {
                    MaterialTheme.colorScheme.primary
                }
                i == currentStage -> {
                    MaterialTheme.colorScheme.primary.copy(alpha = pulsingAlpha)
                }
                else -> {
                    MaterialTheme.colorScheme.outline
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(color = segmentColor)
            )
        }
    }
}

@Composable
private fun Timer(
    stageRemaining: Int,
    actionCountDown: Int?,
    isAgitating: Boolean
) {
    val splitTimer = actionCountDown != null // if stage contains agitation, split timer

    AnimatedContent(
        targetState = splitTimer,
        transitionSpec = {
            fadeIn(tween(400)) togetherWith
                fadeOut(tween(400)) using
                SizeTransform(clip = false)
        },
    ) { showSplitTimer ->
        if (showSplitTimer) {
            SplitTimer(stageRemaining, actionCountDown ?: 0, isAgitating)
        } else {
            val stageTimeText = stageRemaining.toTimerString()

            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp, bottom = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stageTimeText,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = stringResource(R.string.total_remaining),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun SplitTimer(
    stageRemaining: Int,
    actionRemaining: Int,
    isAgitating: Boolean
) {
    val stageTimeText = stageRemaining.toTimerString()
    val actionTimeText = actionRemaining.toTimerString()

    val activeColor by animateColorAsState(
        targetValue = if (isAgitating) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondary
        },
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stageTimeText,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = stringResource(R.string.total_remaining),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Column (
            modifier = Modifier.padding(top = 15.dp, bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = actionTimeText,
                style = MaterialTheme.typography.displayLarge,
                color = activeColor
            )
            Text(
                text = if (isAgitating) {
                    stringResource(R.string.agitate_instruction)
                } else {
                    stringResource(R.string.next_agitation)
                },
                style = MaterialTheme.typography.labelMedium,
                color = activeColor
            )
        }
    }
}

@Composable
private fun InfoBar(
    nextStageName: String
) {
    Text(
        text = stringResource(R.string.up_next_stage, nextStageName.uppercase()),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primary)
            .padding(14.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun Controls(
    isRunning: Boolean,
    onPlayPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onSkipClick: () -> Unit,
    onBackToSetupClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ControlsButton(
                modifier = Modifier.weight(1f),
                onClick = onPlayPauseClick,
                iconResource = if (isRunning) {
                    R.drawable.pause
                } else {
                    R.drawable.play
                },
                contentDescription = stringResource(R.string.playback_button_icon),
                text = if (isRunning) {
                    stringResource(R.string.playback_button_description_pause)
                } else {
                    stringResource(R.string.playback_button_description_play)
                }
            )
            ControlsButton(
                modifier = Modifier.weight(1f),
                onClick = onResetClick,
                iconResource = R.drawable.restart,
                contentDescription = stringResource(R.string.reset_button_icon),
                text = stringResource(R.string.reset_button_description)
            )
            ControlsButton(
                modifier = Modifier.weight(1f),
                onClick = onSkipClick,
                iconResource = R.drawable.skip,
                contentDescription = stringResource(R.string.skip_button_icon),
                text = stringResource(R.string.skip_button_description)
            )
        }
        ControlsButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBackToSetupClick,
            iconResource = R.drawable.back,
            contentDescription = stringResource(R.string.back_to_setup_button_icon),
            text = stringResource(R.string.back_to_setup_button_description)
        )
    }
}

@Composable
private fun ControlsButton(
    modifier: Modifier = Modifier,
    iconResource: Int,
    contentDescription: String,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = RectangleShape,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(iconResource),
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
