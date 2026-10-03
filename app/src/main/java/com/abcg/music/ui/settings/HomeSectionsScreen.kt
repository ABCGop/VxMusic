package com.abcg.music.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.abcg.music.data.local.HomeSection
import com.abcg.music.data.local.SettingsPreferences
import com.abcg.music.ui.common.ExpressiveHeader
import com.abcg.music.ui.common.adaptiveContentWidth
import com.abcg.music.ui.common.safeDrawingBottomPadding
import com.abcg.music.ui.common.safeHorizontalContentPadding
import com.abcg.music.ui.player.LocalMiniPlayerScrollClearance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private fun HomeSection.icon(): ImageVector = when (this) {
    HomeSection.HERO -> Icons.Filled.Home
    HomeSection.QUICK_TILES -> Icons.Filled.Dashboard
    HomeSection.TASTE_STRIP -> Icons.Filled.Sell
    HomeSection.QUICK_PICKS -> Icons.Filled.Bolt
    HomeSection.BECAUSE_YOU_LISTEN_TO -> Icons.Filled.History
    HomeSection.FRESH_FINDS -> Icons.Filled.Whatshot
    HomeSection.JUMP_BACK_IN -> Icons.Filled.Replay
    HomeSection.MIXES -> Icons.Filled.Shuffle
    HomeSection.SPOTLIGHT -> Icons.Filled.Star
    HomeSection.TOP_ARTISTS -> Icons.Filled.People
    HomeSection.HEAVY_ROTATION -> Icons.Filled.Favorite
    HomeSection.ALBUMS -> Icons.Filled.Album
    HomeSection.CHARTS -> Icons.Filled.TrendingUp
    HomeSection.NEW_RELEASES -> Icons.Filled.NewReleases
    HomeSection.FRIENDS -> Icons.Filled.Group
}

@HiltViewModel
class HomeSectionsViewModel @Inject constructor(
    private val settingsPreferences: SettingsPreferences,
) : ViewModel() {
    val hiddenSections: StateFlow<Set<String>> = settingsPreferences.settings
        .map { it.hiddenHomeSections }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun setSectionVisible(section: HomeSection, visible: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setHomeSectionVisible(section.id, visible)
        }
    }

    fun showAll() {
        viewModelScope.launch {
            settingsPreferences.showAllHomeSections()
        }
    }
}

@Composable
fun HomeSectionsScreen(
    onBack: () -> Unit,
    viewModel: HomeSectionsViewModel = hiltViewModel(),
) {
    val hidden by viewModel.hiddenSections.collectAsStateWithLifecycle()
    val visibleCount = HomeSection.entries.size - hidden.size

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .adaptiveContentWidth(maxWidth = 860.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        ) {
            ExpressiveHeader(
                title = stringResource(com.abcg.music.R.string.settings_home_sections),
                subtitle = stringResource(
                    com.abcg.music.R.string.home_sections_visible,
                    visibleCount,
                    HomeSection.entries.size,
                ),
                onBack = onBack,
                actions = {
                    TextButton(onClick = viewModel::showAll, enabled = hidden.isNotEmpty()) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(com.abcg.music.R.string.home_sections_show_all))
                    }
                },
            )

            Text(
                stringResource(com.abcg.music.R.string.home_sections_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )

            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = 32.dp + LocalMiniPlayerScrollClearance.current + safeDrawingBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().safeHorizontalContentPadding(),
            ) {
                items(HomeSection.entries, key = { it.id }) { section ->
                    val visible = section.id !in hidden
                    Card(
                        onClick = { viewModel.setSectionVisible(section, !visible) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(42.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        section.icon(),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(section.titleRes),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    stringResource(section.subtitleRes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = visible,
                                onCheckedChange = { viewModel.setSectionVisible(section, it) },
                            )
                        }
                    }
                }
            }
        }
    }
}
