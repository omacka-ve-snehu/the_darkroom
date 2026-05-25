package cz.cvut.fit.phamgiab.filmdevassistant.core.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import cz.cvut.fit.phamgiab.filmdevassistant.R
import cz.cvut.fit.phamgiab.filmdevassistant.core.domain.BackStackKey

@Composable
fun MainScreen() {
    var currentTabIndex by rememberSaveable { mutableIntStateOf(0) }

    // Initialize two independent backstacks natively (one for discover and stored, second for timer)
    val recipeStack = rememberNavBackStack(BackStackKey.Discover)
    val timerStack = rememberNavBackStack(BackStackKey.TimerSetup(300))

    // Grouping bottom tab entries into their respective stacks
    // Discover (index 0) and Stored (index 1) belong to recipeStack, Timer (index 2) belongs to timerStack
    val stacks = listOf(recipeStack, recipeStack, timerStack)
    val currentStack = stacks[currentTabIndex]

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            val showBottomBar = (currentStack.lastOrNull() as? BackStackKey)?.showBottomBar ?: true
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 250, easing = EaseOut)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 250, easing = EaseIn)
                )
            ) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    bottomTabs.forEachIndexed { index, tab ->
                        val selected = currentTabIndex == index
                        NavigationBarItem(
                            painter = painterResource(id = tab.icon),
                            name = stringResource(tab.title),
                            selected = selected,
                            onClick = {
                                if (selected) {
                                    // Pop to root = clear the respective stack
                                    val stackToClear = stacks[index]
                                    stackToClear.clear()
                                    stackToClear.add(tab.backStackKey)
                                } else {
                                    currentTabIndex = index

                                    // If switching between Discover and Stored and not on detail screen ->
                                    // replace root entry in the recipeStack
                                    if (index < 2 && recipeStack.size == 1) {
                                        recipeStack.clear()
                                        recipeStack.add(tab.backStackKey)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { contentPadding ->
        Navigation(
            backStack = currentStack,
            onStartNewTimer = { devDuration ->
                // Switch to timer tab and clear current timer stack.
                currentTabIndex = 2
                timerStack.clear()
                timerStack.add(BackStackKey.TimerSetup(devDuration))
            },
            modifier = Modifier.padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
        )

    }
}

private data class BottomTab(
    val backStackKey: BackStackKey,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
)

private val bottomTabs = listOf(
    BottomTab(BackStackKey.Discover, R.string.discover, R.drawable.search),
    BottomTab(BackStackKey.Stored, R.string.stored, R.drawable.folder),
    BottomTab(BackStackKey.TimerSetup(300), R.string.timer, R.drawable.timer), // Default duration to 5 mins
)

@Composable
private fun RowScope.NavigationBarItem(
    painter: Painter,
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent,
        ),
        icon = {
            Icon(painter = painter, contentDescription = null, tint = contentColor)
        },
        label = {
            Text(text = name, style = MaterialTheme.typography.labelSmall, color = contentColor)
        }
    )
}
