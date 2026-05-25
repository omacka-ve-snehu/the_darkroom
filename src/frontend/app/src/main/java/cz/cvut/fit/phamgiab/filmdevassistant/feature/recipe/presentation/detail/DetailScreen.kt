package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.presentation.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import cz.cvut.fit.phamgiab.filmdevassistant.R
import cz.cvut.fit.phamgiab.filmdevassistant.core.domain.toTimerString
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.domain.Recipe
import cz.cvut.fit.phamgiab.filmdevassistant.core.presentation.common.EmptyState
import cz.cvut.fit.phamgiab.filmdevassistant.core.presentation.common.ErrorState
import org.koin.androidx.compose.koinViewModel
import kotlin.String

@Composable
fun DetailScreen(
    onTimerClick: (Int) -> Unit,
    viewModel: DetailViewModel = koinViewModel()
) {
    val screenState by viewModel.screenStateStream.collectAsStateWithLifecycle()
    val editOption by viewModel.editOptionStream.collectAsStateWithLifecycle()

    DetailScreen(
        screenState = screenState,
        editOption = editOption,
        onOptionSelected = viewModel::onOptionSelected,
        onTimerClick = onTimerClick,
        onSaveClicked = viewModel::toggleSaveStatus
    )
}

@Composable
private fun DetailScreen(
    screenState: DetailScreenState,
    editOption: EditOption,
    onOptionSelected: (EditOption) -> Unit,
    onTimerClick: (Int) -> Unit,
    onSaveClicked: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            CustomFAB(
                onClick = {
                    when(screenState) {
                        is DetailScreenState.Empty -> {}
                        is DetailScreenState.Loaded -> {
                            if (screenState.recipe != null) {
                                onTimerClick(screenState.recipe.devDuration)
                            }
                        }
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Top
        )
    ) { innerPadding->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            when(screenState) {
                is DetailScreenState.Empty -> EmptyState(stringResource(R.string.nothing_found))
                is DetailScreenState.Loaded -> {
                    if (screenState.recipe != null) {
                        LoadedState(
                            recipe = screenState.recipe,
                            editOption = editOption,
                            onOptionSelected = onOptionSelected,
                            onSaveClicked = onSaveClicked
                        )
                    } else {
                        ErrorState()
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomFAB(
    onClick: () -> Unit
) {
    val shadowColor = MaterialTheme.colorScheme.primary.copy(alpha=0.5f)
    FloatingActionButton(
        onClick = onClick,
        shape = RectangleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.drawBehind {
            drawRect(
                color = shadowColor,
                topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                size = size
            )
        }
    ) {
        Icon(
            painter = painterResource(R.drawable.play),
            contentDescription = stringResource(R.string.start_timer_icon)
        )
    }
}

@Composable
private fun LoadedState(
    recipe: Recipe,
    editOption: EditOption,
    onOptionSelected: (EditOption) -> Unit,
    onSaveClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 25.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = recipe.filmStock,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        HeaderCard(
            process = recipe.processType,
            iso = recipe.iso,
            imageUrl = recipe.imageUrl
        )

        OptionsGrid(
            developer = recipe.developer,
            dilution = recipe.dilution,
            temperature = recipe.temperature,
            duration = recipe.devDuration,
            onOptionSelected = onOptionSelected,
            selectedEditOption = editOption,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
        BottomCard(
            isStored = recipe.isSaved,
            isEditOptionSelected = editOption != EditOption.NONE,
            onSaveClicked = onSaveClicked
        )
    }
}

@Composable
private fun BottomCard(
    isStored: Boolean,
    isEditOptionSelected: Boolean,
    onSaveClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onSaveClicked,
            shape = RectangleShape,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row {
                Icon(
                    painter = painterResource(if (isStored) {
                        R.drawable.bookmark_filled
                    } else {
                        R.drawable.bookmark
                    }),
                    contentDescription = stringResource(R.string.save_icon)
                )
                Text(
                    text = if (isStored) {
                        stringResource(R.string.bookmark_button_remove)
                    } else {
                        stringResource(R.string.bookmark_button_save)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        AnimatedVisibility(
            visible = isEditOptionSelected,
            enter = fadeIn(
                initialAlpha = 0.0f,
                animationSpec = tween(durationMillis = 150, easing = EaseOut)
            ) +
            slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 250, easing = EaseOut)
            ),
            exit = fadeOut(
                targetAlpha = 1.0f,
                animationSpec = tween(durationMillis = 150, easing = EaseOut)
            ) +
            slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 250, easing = EaseIn)
            )
        ) {
            Button(
                onClick = {},
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.onSurface),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row {
                    Icon(
                        painter = painterResource(R.drawable.edit),
                        contentDescription = stringResource(R.string.edit_icon)
                    )
                    Text(
                        text = stringResource(R.string.edit_button_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderCard(
    modifier: Modifier = Modifier,
    process: String,
    iso: Int,
    imageUrl: String
) {
    val context = LocalContext.current
    val model = remember(imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl.ifEmpty { null })
            .size(800, 300)
            .crossfade(true)
            .build()
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = BottomStart
    ) {
        AsyncImage(
            model = model,
            placeholder = painterResource(R.drawable.placeholder),
            fallback = painterResource(R.drawable.placeholder),
            contentDescription = stringResource(R.string.film_stock_image),
            modifier = Modifier.height(150.dp),
            contentScale = ContentScale.FillWidth,
        )
        Row(
            modifier = Modifier.padding(start = 20.dp, bottom = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.process_info, process),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.secondary)
                    .padding(5.dp)
            )
            Text(
                text = stringResource(R.string.iso_info, iso),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.secondary)
                    .padding(5.dp)
            )
        }
    }
}

@Composable
private fun OptionsGrid(
    modifier: Modifier = Modifier,
    developer: String,
    dilution: String,
    temperature: Int,
    duration: Int,
    onOptionSelected: (EditOption) -> Unit,
    selectedEditOption: EditOption
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OptionsCard(
                modifier = Modifier.weight(1f),
                description = stringResource(R.string.developer),
                value = developer,
                onClick = { onOptionSelected(EditOption.DEVELOPER) },
                isSelected = selectedEditOption == EditOption.DEVELOPER
            )
            OptionsCard(
                modifier = Modifier.weight(1f),
                description = stringResource(R.string.dilution),
                value = dilution,
                onClick = { onOptionSelected(EditOption.DILUTION) },
                isSelected = selectedEditOption == EditOption.DILUTION
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OptionsCard(
                modifier = Modifier.weight(1f),
                description = stringResource(R.string.temp),
                value = stringResource(R.string.recipe_temparature_unit, temperature),
                onClick = { onOptionSelected(EditOption.TEMPERATURE) },
                isSelected = selectedEditOption == EditOption.TEMPERATURE
            )
            OptionsCard(
                modifier = Modifier.weight(1f),
                description = stringResource(R.string.time),
                value = duration.toTimerString(),
                onClick = { onOptionSelected(EditOption.TIME) },
                isSelected = selectedEditOption == EditOption.TIME
            )
        }
    }
}

@Composable
private fun OptionsCard(
    modifier: Modifier = Modifier,
    description: String,
    value: String,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        }
    )
    val valueTextColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    )
    val descriptionTextColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxSize()
            .border(width = 1.dp, color = borderColor),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = descriptionTextColor
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueTextColor
            )
        }
    }
}