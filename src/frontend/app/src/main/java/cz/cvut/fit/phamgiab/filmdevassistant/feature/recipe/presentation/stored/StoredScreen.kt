package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.presentation.stored

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.cvut.fit.phamgiab.filmdevassistant.core.presentation.common.EmptyState
import cz.cvut.fit.phamgiab.filmdevassistant.core.presentation.common.LoadingState
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.domain.Recipe
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.res.stringResource
import cz.cvut.fit.phamgiab.filmdevassistant.R
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.presentation.common.RecipeList

@Composable
fun StoredScreen(
    onRecipeClick: (Recipe) -> Unit,
    viewModel: StoredViewModel = koinViewModel()
) {
    val screenState by viewModel.screenStateStream.collectAsStateWithLifecycle()

    StoredScreen(
        onRecipeClick = onRecipeClick,
        screenState = screenState
    )
}

@Composable
private fun StoredScreen(
    onRecipeClick: (Recipe) -> Unit,
    screenState: StoredScreenState
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Top
        )
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(top = 15.dp, start = 25.dp, end = 25.dp)
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.stored_recipes_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            when (screenState) {
                is StoredScreenState.Loading -> LoadingState()
                is StoredScreenState.Empty -> EmptyState(stringResource(R.string.recipes_empty))
                is StoredScreenState.Loaded -> RecipeList(
                    recipes = screenState.recipes,
                    onRecipeClick = onRecipeClick
                )
            }
        }
    }
}