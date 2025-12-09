package com.abcg.music.ui.screen.home

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.common.CHART_SUPPORTED_COUNTRY
import com.maxrave.common.Config
import com.maxrave.common.R
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.home.HomeItem
import com.maxrave.domain.data.model.home.chart.Chart
import com.maxrave.domain.data.model.mood.Mood
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.utils.toTrack
import com.maxrave.logger.Logger
import com.abcg.music.AppResString
import com.abcg.music.extension.isScrollingUp
import com.abcg.music.ui.component.CenterLoadingBox
import com.abcg.music.ui.component.Chip
import com.abcg.music.ui.component.DropdownButton
import com.abcg.music.ui.component.EndOfPage
import com.abcg.music.ui.component.HomeItem
import com.abcg.music.ui.component.HomeItemContentPlaylist
import com.abcg.music.ui.component.HomeShimmer
import com.abcg.music.ui.component.ItemArtistChart
import com.abcg.music.sdui.ui.DynamicUiRenderer

import com.abcg.music.ui.component.MoodMomentAndGenreHomeItem
import com.abcg.music.ui.component.QuickPicksItem
import com.abcg.music.ui.component.ReviewDialog
import com.abcg.music.ui.component.RippleIconButton
import com.abcg.music.ui.component.ShareSavedLyricsDialog
import com.abcg.music.ui.navigation.destination.home.HomeDestination
import com.abcg.music.ui.navigation.destination.home.MoodDestination
import com.abcg.music.ui.navigation.destination.home.NotificationDestination
import com.abcg.music.ui.navigation.destination.home.RecentlySongsDestination
import com.abcg.music.ui.navigation.destination.home.SettingsDestination
import com.abcg.music.ui.navigation.destination.list.ArtistDestination
import com.abcg.music.ui.navigation.destination.list.PlaylistDestination
import com.abcg.music.ui.navigation.destination.login.LoginDestination
import com.abcg.music.ui.theme.typo
import com.abcg.music.ui.theme.md_theme_dark_background
import com.abcg.music.ui.theme.md_theme_dark_surface
import com.abcg.music.viewModel.HomeViewModel
import com.abcg.music.viewModel.HomeViewModel.Companion.HOME_PARAMS_COMMUTE
import com.abcg.music.viewModel.HomeViewModel.Companion.HOME_PARAMS_ENERGIZE
import com.abcg.music.viewModel.HomeViewModel.Companion.HOME_PARAMS_FEEL_GOOD
import com.abcg.music.viewModel.HomeViewModel.Companion.HOME_PARAMS_FOCUS
import com.abcg.music.viewModel.HomeViewModel.Companion.HOME_PARAMS_PARTY
import com.abcg.music.viewModel.HomeViewModel.Companion.HOME_PARAMS_RELAX
import com.abcg.music.viewModel.HomeViewModel.Companion.HOME_PARAMS_ROMANCE
import com.abcg.music.viewModel.HomeViewModel.Companion.HOME_PARAMS_SAD
import com.abcg.music.viewModel.HomeViewModel.Companion.HOME_PARAMS_SLEEP
import com.abcg.music.viewModel.HomeViewModel.Companion.HOME_PARAMS_WORKOUT
import com.abcg.music.viewModel.SharedViewModel
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Calendar

private val listOfHomeChip =
    listOf(
        R.string.all,
        R.string.relax,
        R.string.sleep,
        R.string.energize,
        R.string.sad,
        R.string.romance,
        R.string.feel_good,
        R.string.workout,
        R.string.party,
        R.string.commute,
        R.string.focus,
    )

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@ExperimentalFoundationApi
@Composable
fun HomeScreen(
    onScrolling: (onTop: Boolean) -> Unit = {},
    viewModel: HomeViewModel =
        koinViewModel(),
    sharedViewModel: SharedViewModel =
        koinInject(),
    navController: NavController,
    dynamicUiViewModel: com.abcg.music.sdui.DynamicUiViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val isScrollingUp by scrollState.isScrollingUp()
    val accountInfo by viewModel.accountInfo.collectAsStateWithLifecycle()
    val homeData by viewModel.homeItemList.collectAsStateWithLifecycle()
    val newRelease by viewModel.newRelease.collectAsStateWithLifecycle()
    val chart by viewModel.chart.collectAsStateWithLifecycle()
    val moodMomentAndGenre by viewModel.exploreMoodItem.collectAsStateWithLifecycle()
    val chartLoading by viewModel.loadingChart.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    var accountShow by rememberSaveable {
        mutableStateOf(false)
    }
    val regionChart by viewModel.regionCodeChart.collectAsStateWithLifecycle()
    val reloadDestination by sharedViewModel.reloadDestination.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val chipRowState = rememberScrollState()
    val params by viewModel.params.collectAsStateWithLifecycle()

    val shouldShowLogInAlert by viewModel.showLogInAlert.collectAsStateWithLifecycle()

    val openAppTime by sharedViewModel.openAppTime.collectAsStateWithLifecycle()
    val shareLyricsPermissions by sharedViewModel.shareSavedLyrics.collectAsStateWithLifecycle()

    var showReviewDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showRequestShareLyricsPermissions by rememberSaveable {
        mutableStateOf(false)
    }

    var topAppBarHeightPx by rememberSaveable {
        mutableIntStateOf(0)
    }

    val hazeState =
        rememberHazeState(
            blurEnabled = true,
        )

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex }
            .collect {
                if (it <= 1) {
                    onScrolling.invoke(true)
                } else {
                    onScrolling.invoke(isScrollingUp)
                }
            }
    }

    val onRefresh: () -> Unit = {
        isRefreshing = true
        viewModel.getHomeItemList(params)
        Logger.w("HomeScreen", "onRefresh")
    }
    LaunchedEffect(key1 = reloadDestination) {
        if (reloadDestination == HomeDestination::class) {
            if (scrollState.firstVisibleItemIndex > 1) {
                Logger.w("HomeScreen", "scrollState.firstVisibleItemIndex: ${scrollState.firstVisibleItemIndex}")
                scrollState.animateScrollToItem(0)
                sharedViewModel.reloadDestinationDone()
            } else {
                Logger.w("HomeScreen", "scrollState.firstVisibleItemIndex: ${scrollState.firstVisibleItemIndex}")
                onRefresh.invoke()
            }
        }
    }
    LaunchedEffect(key1 = loading) {
        if (!loading) {
            isRefreshing = false
            sharedViewModel.reloadDestinationDone()
            coroutineScope.launch {
                pullToRefreshState.animateToHidden()
            }
        }
    }
    LaunchedEffect(key1 = homeData) {
        accountShow = homeData.find { it.subtitle == accountInfo?.first } == null
    }
    LaunchedEffect(openAppTime, shareLyricsPermissions) {
        Logger.w("HomeScreen", "openAppTime: $openAppTime, shareLyricsPermissions: $shareLyricsPermissions")
        if (openAppTime >= 10 && openAppTime % 10 == 0 && openAppTime <= 50) {
            showReviewDialog = true
        } else if ((openAppTime == 1 || openAppTime % 15 == 0) && openAppTime <= 60 && !shareLyricsPermissions) {
            showRequestShareLyricsPermissions = true
        } else {
            showReviewDialog = false
            showRequestShareLyricsPermissions = false
        }
    }

//    if (shouldShowGetDataSyncIdBottomSheet) {
//        GetDataSyncIdBottomSheet(
//            cookie = youTubeCookie,
//            onDismissRequest = {
//                shouldShowGetDataSyncIdBottomSheet = false
//            },
//        )
//    }

    if (showReviewDialog) {
        ReviewDialog(
            onDismissRequest = {
                sharedViewModel.onDoneReview(
                    isDismissOnly = true,
                )
                showReviewDialog = false
            },
            onDoneReview = {
                sharedViewModel.onDoneReview(
                    isDismissOnly = false,
                )
                showReviewDialog = false
            },
        )
    }

    if (showRequestShareLyricsPermissions) {
        ShareSavedLyricsDialog(
            onDismissRequest = {
                showRequestShareLyricsPermissions = false
                sharedViewModel.onDoneReview(
                    isDismissOnly = true,
                )
            },
            onConfirm = { contributor ->
                sharedViewModel.onDoneRequestingShareLyrics(
                    contributor,
                )
            },
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        md_theme_dark_background,
                        Color(0xFF13131F) // Slightly lighter/purple tinted bottom
                    )
                )
            )
    ) {
        PullToRefreshBox(
            modifier =
                Modifier
                    .hazeSource(hazeState),
            state = pullToRefreshState,
            onRefresh = onRefresh,
            isRefreshing = isRefreshing,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(
                                top =
                                    with(LocalDensity.current) {
                                        topAppBarHeightPx.toDp()
                                    },
                            ),
                    containerColor = PullToRefreshDefaults.indicatorContainerColor,
                    color = PullToRefreshDefaults.indicatorColor,
                    maxDistance = PullToRefreshDefaults.PositionalThreshold,
                )
            },
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Crossfade(targetState = loading, label = "Home Shimmer") { loading ->
                if (!loading) {
                    LazyColumn(
                        modifier =
                            Modifier
                                .padding(horizontal = 16.dp),
                        contentPadding =
                            PaddingValues(
                                top = with(LocalDensity.current) { topAppBarHeightPx.toDp() },
                                bottom = 24.dp,
                            ),
                        state = scrollState,
                        verticalArrangement = Arrangement.spacedBy(32.dp),
                    ) {
                        item {
                            val dynamicContent by dynamicUiViewModel.homeDynamicContent.collectAsStateWithLifecycle()
                            
                            Column {
                                if (dynamicContent != null) {
                                    DynamicUiRenderer(
                                        sections = dynamicContent!!.sections,
                                        modifier = Modifier.padding(bottom = 16.dp),
                                        navController = navController
                                    )
                                }
                                
                                if (accountInfo != null && accountShow) {
                                    AccountLayout(
                                        accountName = accountInfo?.first ?: "",
                                        url = accountInfo?.second ?: "",
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                                AnimatedVisibility(
                                    visible =
                                        homeData.find {
                                            it.title ==
                                                context.getString(
                                                    R.string.quick_picks,
                                                )
                                        } != null,
                                ) {
                                    QuickPicks(
                                        homeItem =
                                            (
                                                homeData.find {
                                                    it.title ==
                                                        context.getString(
                                                            R.string.quick_picks,
                                                        )
                                                } ?: return@AnimatedVisibility
                                            ).let { content ->
                                                content.copy(
                                                    contents =
                                                        content.contents.mapNotNull { ct ->
                                                            ct?.copy(
                                                                artists =
                                                                    ct.artists?.let { art ->
                                                                        if (art.size > 1) {
                                                                            art.dropLast(1)
                                                                        } else {
                                                                            art
                                                                        }
                                                                    },
                                                            )
                                                        },
                                                )
                                            },
                                        viewModel = viewModel,
                                    )
                                }
                            }
                        }
                        items(homeData, key = { it.hashCode() }) {
                            if (it.title != context.getString(R.string.quick_picks)) {
                                HomeItem(
                                    navController = navController,
                                    data = it,
                                )
                            }
                        }
                        items(newRelease, key = { it.hashCode() }) {
                            AnimatedVisibility(
                                visible = newRelease.isNotEmpty(),
                            ) {
                                HomeItem(
                                    navController = navController,
                                    data = it,
                                )
                            }
                        }
                        item {
                            AnimatedVisibility(
                                visible = moodMomentAndGenre != null,
                            ) {
                                moodMomentAndGenre?.let {
                                    MoodMomentAndGenre(
                                        mood = it,
                                        navController = navController,
                                    )
                                }
                            }
                        }
                        item {
                            Column(
                                Modifier
                                    .padding(vertical = 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                ChartTitle()
                                Spacer(modifier = Modifier.height(5.dp))
                                Crossfade(targetState = regionChart) {
                                    Logger.w("HomeScreen", "regionChart: $it")
                                    if (it != null) {
                                        DropdownButton(
                                            items = CHART_SUPPORTED_COUNTRY.itemsData.toList(),
                                            defaultSelected =
                                                CHART_SUPPORTED_COUNTRY.itemsData.getOrNull(
                                                    CHART_SUPPORTED_COUNTRY.items.indexOf(it),
                                                )
                                                    ?: CHART_SUPPORTED_COUNTRY.itemsData[1],
                                        ) {
                                            viewModel.exploreChart(
                                                CHART_SUPPORTED_COUNTRY.items[
                                                    CHART_SUPPORTED_COUNTRY.itemsData.indexOf(
                                                        it,
                                                    ),
                                                ],
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Crossfade(
                                    targetState = chartLoading,
                                    label = "Chart",
                                ) { loading ->
                                    if (!loading) {
                                        chart?.let {
                                            ChartData(
                                                chart = it,
                                                viewModel = viewModel,
                                                navController = navController,
                                                context = context,
                                            )
                                        }
                                    } else {
                                        CenterLoadingBox(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(400.dp),
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            EndOfPage()
                        }
                    }
                } else {
                    Column {
                        Spacer(
                            Modifier.height(
                                with(LocalDensity.current) {
                                    topAppBarHeightPx.toDp()
                                },
                            ),
                        )
                        HomeShimmer()
                    }
                }
            }
        }
        Column(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .hazeEffect(hazeState, style = HazeMaterials.thin()) {
                        blurEnabled = true
                    }.onGloballyPositioned { coordinates ->
                        topAppBarHeightPx = coordinates.size.height
                    },
        ) {
            AnimatedVisibility(
                visible = isScrollingUp,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                HomeTopAppBar(navController)
            }
            AnimatedVisibility(
                visible = !isScrollingUp,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Spacer(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                WindowInsets.statusBars,
                            ),
                )
            }
            Row(
                modifier =
                    Modifier
                        .horizontalScroll(chipRowState)
                        .padding(vertical = 12.dp, horizontal = 15.dp)
                        .background(Color.Transparent),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOfHomeChip.forEach { id ->
                    val isSelected =
                        when (params) {
                            HOME_PARAMS_RELAX -> id == R.string.relax
                            HOME_PARAMS_SLEEP -> id == R.string.sleep
                            HOME_PARAMS_ENERGIZE -> id == R.string.energize
                            HOME_PARAMS_SAD -> id == R.string.sad
                            HOME_PARAMS_ROMANCE -> id == R.string.romance
                            HOME_PARAMS_FEEL_GOOD -> id == R.string.feel_good
                            HOME_PARAMS_WORKOUT -> id == R.string.workout
                            HOME_PARAMS_PARTY -> id == R.string.party
                            HOME_PARAMS_COMMUTE -> id == R.string.commute
                            HOME_PARAMS_FOCUS -> id == R.string.focus
                            else -> id == R.string.all
                        }
                    Chip(
                        isAnimated = loading,
                        isSelected = isSelected,
                        text = stringResource(id = id),
                    ) {
                        when (id) {
                            R.string.all -> viewModel.setParams(null)
                            R.string.relax -> viewModel.setParams(HOME_PARAMS_RELAX)
                            R.string.sleep -> viewModel.setParams(HOME_PARAMS_SLEEP)
                            R.string.energize -> viewModel.setParams(HOME_PARAMS_ENERGIZE)
                            R.string.sad -> viewModel.setParams(HOME_PARAMS_SAD)
                            R.string.romance -> viewModel.setParams(HOME_PARAMS_ROMANCE)
                            R.string.feel_good -> viewModel.setParams(HOME_PARAMS_FEEL_GOOD)
                            R.string.workout -> viewModel.setParams(HOME_PARAMS_WORKOUT)
                            R.string.party -> viewModel.setParams(HOME_PARAMS_PARTY)
                            R.string.commute -> viewModel.setParams(HOME_PARAMS_COMMUTE)
                            R.string.focus -> viewModel.setParams(HOME_PARAMS_FOCUS)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(navController: NavController) {
    // Get current hour and update every minute
    var currentHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            delay(60000) // Update every minute
        }
    }
    
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(id = AppResString.app_name),
                    style = typo.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Text(
                    text =
                        when (currentHour) {
                            in 5..11 -> {
                                stringResource(R.string.good_morning)
                            }

                            in 12..17 -> {
                                stringResource(R.string.good_afternoon)
                            }

                            in 18..20 -> {
                                stringResource(R.string.good_evening)
                            }

                            else -> {
                                stringResource(R.string.good_night)
                            }
                        },
                    style = typo.bodySmall,
                )
            }
        },
        actions = {
            RippleIconButton(resId = R.drawable.outline_notifications_24) {
                navController.navigate(NotificationDestination)
            }
            RippleIconButton(resId = R.drawable.baseline_history_24) {
                navController.navigate(RecentlySongsDestination)
            }
            RippleIconButton(resId = R.drawable.baseline_settings_24) {
                navController.navigate(SettingsDestination)
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
    )
}

@Composable
fun AccountLayout(
    accountName: String,
    url: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.welcome_back),
            style = typo.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(url)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(url)
                        .crossfade(true)
                        .build(),
                placeholder = painterResource(R.drawable.holder),
                error = painterResource(R.drawable.holder),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape),
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = accountName,
                    style = typo.headlineSmall,
                    color = Color.White,
                )
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun QuickPicks(
    homeItem: HomeItem,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val lazyListState = rememberLazyGridState()
    val snapperFlingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyGridState = lazyListState, snapPosition = SnapPosition.Start))
    val density = LocalDensity.current
    var widthDp by remember {
        mutableStateOf(0.dp)
    }
    Column(
        Modifier
            .padding(vertical = 8.dp)
            .onGloballyPositioned { coordinates ->
                with(density) {
                    widthDp = (coordinates.size.width).toDp()
                }
            },
    ) {
        Text(
            text = stringResource(id = R.string.let_s_start_with_a_radio),
            style = typo.bodySmall,
        )
        Text(
            text = stringResource(id = R.string.quick_picks),
            style = typo.headlineMedium,
            color = Color.White,
            maxLines = 1,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
        )
        LazyHorizontalGrid(
            rows = GridCells.Fixed(4),
            modifier = Modifier.height(256.dp),
            state = lazyListState,
            flingBehavior = snapperFlingBehavior,
        ) {
            items(homeItem.contents, key = { it.hashCode() }) {
                if (it != null) {
                    QuickPicksItem(
                        onClick = {
                            val firstQueue: Track = it.toTrack()
                            viewModel.setQueueData(
                                QueueData.Data(
                                    listTracks = arrayListOf(firstQueue),
                                    firstPlayedTrack = firstQueue,
                                    playlistId = "RDAMVM${it.videoId}",
                                    playlistName = "\"${it.title}\" Radio",
                                    playlistType = PlaylistType.RADIO,
                                    continuation = null,
                                ),
                            )
                            viewModel.loadMediaItem(
                                firstQueue,
                                type = Config.SONG_CLICK,
                            )
                        },
                        data = it,
                        widthDp = widthDp,
                    )
                }
            }
        }
    }
}

@Composable
fun MoodMomentAndGenre(
    mood: Mood,
    navController: NavController,
) {
    val lazyListState1 = rememberLazyGridState()
    val snapperFlingBehavior1 = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyGridState = lazyListState1))

    val lazyListState2 = rememberLazyGridState()
    val snapperFlingBehavior2 = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyGridState = lazyListState2))

    Column(
        Modifier
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = stringResource(id = R.string.let_s_pick_a_playlist_for_you),
            style = typo.bodyMedium,
        )
        Text(
            text = stringResource(id = R.string.moods_amp_moment),
            style = typo.headlineMedium,
            maxLines = 1,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
        )
        LazyHorizontalGrid(
            rows = GridCells.Fixed(3),
            modifier = Modifier.height(210.dp),
            state = lazyListState1,
            flingBehavior = snapperFlingBehavior1,
        ) {
            items(mood.moodsMoments, key = { it.title }) {
                MoodMomentAndGenreHomeItem(title = it.title) {
                    navController.navigate(
                        MoodDestination(
                            it.params,
                        ),
                    )
                }
            }
        }
        Text(
            text = stringResource(id = R.string.genre),
            style = typo.headlineMedium,
            maxLines = 1,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
        )
        LazyHorizontalGrid(
            rows = GridCells.Fixed(3),
            modifier = Modifier.height(210.dp),
            state = lazyListState2,
            flingBehavior = snapperFlingBehavior2,
        ) {
            items(mood.genres, key = { it.title }) {
                MoodMomentAndGenreHomeItem(title = it.title) {
                    navController.navigate(
                        MoodDestination(
                            it.params,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun ChartTitle() {
    Column {
        Text(
            text = stringResource(id = R.string.what_is_best_choice_today),
            style = typo.bodyMedium,
        )
        Text(
            text = stringResource(id = R.string.chart),
            style = typo.headlineMedium,
            maxLines = 1,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
        )
    }
}

@Composable
fun ChartData(
    chart: Chart,
    viewModel: HomeViewModel,
    navController: NavController,
    context: Context,
) {
    var gridWidthDp by remember {
        mutableStateOf(0.dp)
    }
    val density = LocalDensity.current

    val lazyListState2 = rememberLazyGridState()
    val snapperFlingBehavior2 = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyGridState = lazyListState2))

    Column(
        Modifier.onGloballyPositioned { coordinates ->
            with(density) {
                gridWidthDp = (coordinates.size.width).toDp()
            }
        },
    ) {
        chart.listChartItem.forEach { item ->
            Text(
                text = item.title,
                style = typo.headlineMedium,
                maxLines = 1,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
            )
            val lazyListState = rememberLazyListState()
            val snapperFlingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyListState = lazyListState))
            LazyRow(flingBehavior = snapperFlingBehavior) {
                items(item.playlists.size, key = { index ->
                    val data = item.playlists[index]
                    data.id + data.title + index
                }) {
                    HomeItemContentPlaylist(
                        onClick = {
                            navController.navigate(
                                PlaylistDestination(
                                    playlistId = item.playlists[it].id,
                                    isYourYouTubePlaylist = false,
                                ),
                            )
                        },
                        data = item.playlists[it],
                    )
                }
            }
        }
        Text(
            text = stringResource(id = R.string.top_artists),
            style = typo.headlineMedium,
            maxLines = 1,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
        )
        LazyHorizontalGrid(
            rows = GridCells.Fixed(3),
            modifier = Modifier.height(240.dp),
            state = lazyListState2,
            flingBehavior = snapperFlingBehavior2,
        ) {
            items(chart.artists.itemArtists.size, key = { index ->
                val item = chart.artists.itemArtists[index]
                item.title + item.browseId + index
            }) {
                val data = chart.artists.itemArtists[it]
                ItemArtistChart(onClick = {
                    navController.navigate(
                        ArtistDestination(
                            channelId = data.browseId,
                        ),
                    )
                }, data = data, context = context, widthDp = gridWidthDp)
            }
        }
    }
}
