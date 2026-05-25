package cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.presentation.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.cvut.fit.phamgiab.filmdevassistant.R
import cz.cvut.fit.phamgiab.filmdevassistant.core.domain.toMinutesSeconds

@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    onStartTimerClick: () -> Unit,
) {
    val devDuration by viewModel.manualDeveloperTimeStream.collectAsStateWithLifecycle()

    SetupScreen(
        devDuration = devDuration,
        onDevDurationChange = viewModel::updateManualTime,
        onStartTimerClick = {
            viewModel.startTimer()
            onStartTimerClick()
        }
    )
}

@Composable
private fun SetupScreen(
    devDuration: Int,
    onDevDurationChange: (Int) -> Unit,
    onStartTimerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 25.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outline),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.ready_to_start),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            TimeSelector(
                devDuration = devDuration,
                onDevDurationChange = onDevDurationChange
            )

            Spacer(modifier = Modifier.height(64.dp))


            Button(
                onClick = onStartTimerClick,
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.start_timer),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun TimeSelector(
    devDuration: Int,
    onDevDurationChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val (minutes, seconds) = devDuration.toMinutesSeconds()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeColumn(
            value = minutes,
            label = stringResource(R.string.minute),
            onIncrement = { onDevDurationChange(devDuration + 60) }, // +1 minute
            onDecrement = { if (devDuration >= 60) onDevDurationChange(devDuration - 60) } // -1 minute
        )
        Text(
            text = stringResource(R.string.minute_seconds_separator),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 25.dp)
        )
        TimeColumn(
            value = seconds,
            label = stringResource(R.string.second),
            onIncrement = { onDevDurationChange(devDuration + 1) }, // +1 second
            onDecrement = { if (devDuration >= 1) onDevDurationChange(devDuration - 1) } // -1 second
        )
    }
}

@Composable
private fun TimeColumn(
    value: Int,
    label: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onIncrement) {
            Icon(
                painter = painterResource(R.drawable.arrow_up),
                contentDescription = stringResource(R.string.increase_time, label),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(64.dp)
            )
        }

        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        IconButton(onClick = onDecrement) {
            Icon(
                painter = painterResource(R.drawable.arrow_down),
                contentDescription = stringResource(R.string.decrease_time, label),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(64.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}