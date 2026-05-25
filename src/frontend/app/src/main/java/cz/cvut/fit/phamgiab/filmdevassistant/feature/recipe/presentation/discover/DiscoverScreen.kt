package cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.presentation.discover

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.cvut.fit.phamgiab.filmdevassistant.R
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.domain.Recipe
import cz.cvut.fit.phamgiab.filmdevassistant.core.presentation.common.EmptyState
import cz.cvut.fit.phamgiab.filmdevassistant.core.presentation.common.ErrorState
import cz.cvut.fit.phamgiab.filmdevassistant.core.presentation.common.LoadingState
import cz.cvut.fit.phamgiab.filmdevassistant.core.presentation.common.NoMatchState
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.res.stringResource
import cz.cvut.fit.phamgiab.filmdevassistant.feature.recipe.presentation.common.RecipeList

@Composable
fun DiscoverScreen(
    onRecipeClick: (Recipe) -> Unit,
    viewModel: DiscoverViewModel = koinViewModel()
) {
    val screenState by viewModel.screenStateStream.collectAsStateWithLifecycle()
    val query by viewModel.queryStream.collectAsStateWithLifecycle()

    DiscoverScreen(
        query = query,
        screenState = screenState,
        onQueryChange = viewModel::searchCharacters,
        onClearClick = viewModel::clearText,
        onRecipeClick = { onRecipeClick(it) },
    )
}

@Composable
private fun DiscoverScreen(
    query: String,
    screenState: DiscoverScreenState,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(top = 15.dp, start = 25.dp, end = 25.dp),
        ) {
            Text(
                text = stringResource(R.string.discover_recipes_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            SearchBar(
                query = query,
                onQueryChange = onQueryChange,
                onClearClick = onClearClick,
                modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)
            )


            when (screenState) {
                is DiscoverScreenState.Loading -> LoadingState()
                is DiscoverScreenState.Empty -> EmptyState(stringResource(R.string.empty))
                is DiscoverScreenState.Error -> ErrorState()
                is DiscoverScreenState.NoMatch -> NoMatchState()
                is DiscoverScreenState.Loaded -> RecipeList(
                    recipes = screenState.recipes,
                    onRecipeClick = onRecipeClick
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = query,
        onValueChange = onQueryChange,
        maxLines = 1,
        placeholder = {
            Text(
                text = stringResource(R.string.search_example),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
            unfocusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = stringResource(R.string.search_icon),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = {
                    onClearClick()
                }) {
                    Icon(
                        painter = painterResource(R.drawable.close),
                        contentDescription = stringResource(R.string.search_clear_icon),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    )
}