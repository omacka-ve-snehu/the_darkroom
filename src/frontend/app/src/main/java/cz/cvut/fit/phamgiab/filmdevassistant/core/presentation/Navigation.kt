package cz.cvut.fit.phamgiab.filmdevassistant.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import cz.cvut.fit.phamgiab.filmdevassistant.core.domain.BackStackKey
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.presentation.detail.DetailScreen
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.presentation.discover.DiscoverScreen
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.presentation.stored.StoredScreen
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.presentation.running.RunningScreen
import cz.cvut.fit.phamgiab.filmdevassistant.feature.timer.presentation.setup.SetupScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.collections.listOf

@Composable
fun Navigation(
    backStack: NavBackStack<NavKey>,
    onStartNewTimer: (Int) -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = {
            backStack.removeAt(backStack.lastIndex)
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<BackStackKey.Discover> {
                DiscoverScreen(
                    onRecipeClick = { backStack.add(BackStackKey.RecipeDetail(it) ) },
                )
            }

            entry<BackStackKey.Stored> {
                StoredScreen(
                    onRecipeClick = { backStack.add(BackStackKey.RecipeDetail(it)) }
                )
            }

            entry<BackStackKey.RecipeDetail> { key ->
                DetailScreen(
                    viewModel = koinViewModel { parametersOf(key.recipe) },
                    onTimerClick = { devDuration ->
                        onStartNewTimer(devDuration)
                    }
                )
            }

            entry<BackStackKey.TimerSetup> { key ->
                SetupScreen(
                    viewModel = koinViewModel { parametersOf(key.initialDuration) },
                    onStartTimerClick = { backStack.add(BackStackKey.TimerRunning) }
                )
            }

            entry<BackStackKey.TimerRunning> {
                RunningScreen(
                    onBackToSetup = { backStack.removeLastOrNull() }
                )
            }
        },
    )
}