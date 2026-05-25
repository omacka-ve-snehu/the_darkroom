package cz.cvut.fit.phamgiab.filmdevassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cz.cvut.fit.phamgiab.filmdevassistant.core.presentation.MainScreen
import cz.cvut.fit.phamgiab.filmdevassistant.core.presentation.theme.FilmDevAssistantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FilmDevAssistantTheme {
                MainScreen()
            }
        }
    }
}
