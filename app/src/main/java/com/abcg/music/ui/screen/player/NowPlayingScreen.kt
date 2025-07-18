@file:OptIn(ExperimentalMaterial3Api::class)

package com.abcg.music.ui.screen.player

import android.os.Build
import android.util.Log
import android.view.View
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.toBitmap
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kmpalette.rememberPaletteState
import com.abcg.music.R
import com.abcg.music.extension.GradientAngle
import com.abcg.music.extension.GradientOffset
import com.abcg.music.extension.findActivity
import com.abcg.music.extension.formatDuration
import com.abcg.music.extension.getColorFromPalette
import com.abcg.music.extension.getScreenSizeInfo
import com.abcg.music.extension.isElementVisible
import com.abcg.music.extension.navigateSafe
import com.abcg.music.extension.parseTimestampToMilliseconds
import com.abcg.music.service.RepeatState
import com.abcg.music.ui.component.DescriptionView
import com.abcg.music.ui.component.ExplicitBadge
import com.abcg.music.ui.component.FullscreenLyricsSheet
import com.abcg.music.ui.component.HeartCheckBox
import com.abcg.music.ui.component.InfoPlayerBottomSheet
import com.abcg.music.ui.component.LyricsView
import com.abcg.music.ui.component.MediaPlayerView
import com.abcg.music.ui.component.NowPlayingBottomSheet
import com.abcg.music.ui.component.PlayPauseButton
import com.abcg.music.ui.component.QueueBottomSheet
import com.abcg.music.ui.theme.blackMoreOverlay
import com.abcg.music.ui.theme.md_theme_dark_background
import com.abcg.music.ui.theme.overlay
import com.abcg.music.ui.theme.seed
import com.abcg.music.ui.theme.typo
import com.abcg.music.viewModel.LyricsProvider
import com.abcg.music.viewModel.SharedViewModel
import com.abcg.music.viewModel.UIEvent
import com.moriatsushi.insetsx.statusBars
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class, ExperimentalHazeMaterialsApi::class)
@UnstableApi
@ExperimentalMaterial3Api
@Composable
fun NowPlayingScreen(
    sharedViewModel: SharedViewModel,
    navController: NavController,
) {
    val screenInfo = getScreenSizeInfo()

    @Suppress("ktlint:standard:property-naming")
    val TAG = "NowPlayingScreen"
    val context = LocalContext.current
    val localDensity = LocalDensity.current
    val uriHandler = LocalUriHandler.current

    // ViewModel State
    val controllerState by sharedViewModel.controllerState.collectAsState()
    val screenDataState by sharedViewModel.nowPlayingScreenData.collectAsState()
    val timelineState by sharedViewModel.timeline.collectAsState()
    val likeStatus by sharedViewModel.likeStatus.collectAsState()

    val shouldShowVideo by sharedViewModel.getVideo.collectAsState()
    // State
    val mainScrollState = rememberScrollState()

    var showHideMiddleLayout by rememberSaveable {
        mutableStateOf(true)
    }

    var showSheet by rememberSaveable {
        mutableStateOf(false)
    }

    var showFullscreenLyrics by rememberSaveable {
        mutableStateOf(false)
    }

    var showQueueBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    var showInfoBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    var shouldShowToolbar by remember {
        mutableStateOf(false)
    }

    // Palette state
    val paletteState = rememberPaletteState()

    val startColor =
        remember {
            Animatable(md_theme_dark_background)
        }
    val endColor =
        remember {
            Animatable(md_theme_dark_background)
        }
    val gradientOffset by remember {
        mutableStateOf(GradientOffset(GradientAngle.CW135))
    }

    var bitmap by remember {
        mutableStateOf<ImageBitmap?>(null)
    }

    var spotShadowColor by remember {
        mutableStateOf(Color.White)
    }

    val blurBg by sharedViewModel.blurBg.collectAsState()

    LaunchedEffect(bitmap) {
        val bm = bitmap
        if (bm != null) {
            paletteState.generate(bm)
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { paletteState.palette }
            .distinctUntilChanged()
            .collectLatest {
                spotShadowColor = it.getColorFromPalette()
                startColor.animateTo(it.getColorFromPalette())
                endColor.animateTo(md_theme_dark_background)
            }
    }

    LaunchedEffect(spotShadowColor) {
        Log.d(TAG, "spotShadowColor: $spotShadowColor")
    }

    LaunchedEffect(true) {
        val activity = context.findActivity()
        val bottom = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        val miniplayer = activity.findViewById<ComposeView>(R.id.miniplayer)
        if (bottom.visibility != View.GONE || miniplayer.visibility != View.GONE) {
            bottom.visibility = View.GONE
            miniplayer.visibility = View.GONE
        }
    }

    // Height
    var topAppBarHeightDp by rememberSaveable {
        mutableIntStateOf(0)
    }
    var middleLayoutHeightDp by rememberSaveable {
        mutableIntStateOf(0)
    }
    var infoLayoutHeightDp by rememberSaveable {
        mutableIntStateOf(0)
    }
    var middleLayoutPaddingDp by rememberSaveable {
        mutableIntStateOf(0)
    }
    val minimumPaddingDp by rememberSaveable {
        mutableIntStateOf(
            30,
        )
    }
    LaunchedEffect(
        topAppBarHeightDp,
        screenInfo,
        infoLayoutHeightDp,
        minimumPaddingDp,
    ) {
        if (topAppBarHeightDp > 0 && middleLayoutHeightDp > 0 && infoLayoutHeightDp > 0 && screenInfo.hDP > 0) {
            val result = (screenInfo.hDP - topAppBarHeightDp - middleLayoutHeightDp - infoLayoutHeightDp - minimumPaddingDp) / 2
            middleLayoutPaddingDp =
                if (result > minimumPaddingDp) {
                    result
                } else {
                    minimumPaddingDp
                }
        }
    }

    var sliderValue by rememberSaveable {
        mutableFloatStateOf(0f)
    }
    LaunchedEffect(key1 = timelineState) {
        sliderValue =
            if (timelineState.total > 0L) {
                timelineState.current.toFloat() * 100 / timelineState.total.toFloat()
            } else {
                0f
            }
    }
    LaunchedEffect(key1 = screenDataState) {
        showHideMiddleLayout = screenDataState.canvasData == null
    }

    // Show ControlLayout Or Show Artist Badge
    var showHideControlLayout by rememberSaveable {
        mutableStateOf(true)
    }
    val controlLayoutAlpha: Float by animateFloatAsState(
        targetValue = if (showHideControlLayout) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = 500,
                easing = LinearEasing,
            ),
        label = "ControlLayoutAlpha",
    )

    var showHideJob by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(key1 = showHideJob) {
        if (!showHideJob) {
            delay(5000)
            if (mainScrollState.value == 0) showHideControlLayout = false
            showHideJob = true
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow {
            screenDataState
        }.distinctUntilChangedBy {
            it.canvasData?.url
        }.collectLatest {
            if (it.canvasData != null && mainScrollState.value == 0) {
                showHideJob = false
            } else {
                showHideJob = true
                showHideControlLayout = true
            }
        }
    }

    LaunchedEffect(key1 = showHideControlLayout) {
        if (showHideControlLayout && screenDataState.canvasData != null && mainScrollState.value == 0) {
            showHideJob = false
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { mainScrollState.value }
            .distinctUntilChanged()
            .collect {
                if (it > 0 && !showHideControlLayout && screenDataState.canvasData != null) {
                    showHideJob = true
                    showHideControlLayout = true
                } else if (showHideControlLayout && it == 0 && screenDataState.canvasData != null) {
                    showHideJob = false
                }
            }
    }

    // Fullscreen overlay
    var showHideFullscreenOverlay by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = showHideFullscreenOverlay) {
        if (showHideFullscreenOverlay) {
            delay(3000)
            showHideFullscreenOverlay = false
        }
    }

    if (showSheet) {
        NowPlayingBottomSheet(
            onDismiss = {
                showSheet = false
            },
            navController = navController,
            song = null, // Auto set now playing
            setSleepTimerEnable = true,
            changeMainLyricsProviderEnable = true,
        )
    }

    if (showFullscreenLyrics) {
        FullscreenLyricsSheet(
            sharedViewModel = sharedViewModel,
            color = startColor.value,
            navController = navController,
            shouldHaze = sharedViewModel.blurFullscreenLyrics(),
        ) {
            showFullscreenLyrics = false
        }
    }

    if (showQueueBottomSheet) {
        QueueBottomSheet(
            onDismiss = {
                showQueueBottomSheet = false
            },
        )
    }

    if (showInfoBottomSheet) {
        InfoPlayerBottomSheet(
            onDismiss = {
                showInfoBottomSheet = false
            },
        )
    }

    val hazeState = rememberHazeState(
        blurEnabled = true
    )

    Box {
        if (blurBg && screenDataState.canvasData == null) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(screenDataState.thumbnailURL)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(screenDataState.thumbnailURL + "BIGGER")
                        .crossfade(550)
                        .build(),
                contentDescription = "",
                contentScale = ContentScale.FillHeight,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                        .hazeSource(hazeState),
            )
        }
        Column(
            Modifier
                .verticalScroll(mainScrollState)
                .pointerInput(Unit) {
                    var isSwipeHandled = false
                    detectHorizontalDragGestures(
                        onDragEnd = { isSwipeHandled = false },
                    ) { change, dragAmount ->
                        change.consume()
                        if (!isSwipeHandled) {
                            when {
                                // Swipe left (negative dragAmount)
                                dragAmount < -90 -> {
                                    if (controllerState.isNextAvailable) {
                                        sharedViewModel.onUIEvent(UIEvent.Next)
                                        isSwipeHandled = true
                                    }
                                }
                                // Swipe right (positive dragAmount)
                                dragAmount > 90 -> {
                                    if (controllerState.isPreviousAvailable) {
                                        sharedViewModel.onUIEvent(UIEvent.Previous)
                                        isSwipeHandled = true
                                    }
                                }
                            }
                        }
                    }
                }.then(
                    if (showHideMiddleLayout) {
                        if (blurBg && screenDataState.canvasData == null) {
                            Modifier
                                .background(Color.Transparent)
                                .hazeEffect(hazeState, style = CupertinoMaterials.thin()) {
                                    blurEnabled = true
                                }
                        } else {
                            Modifier
                                .background(
                                    Brush.linearGradient(
                                        colors =
                                            listOf(
                                                startColor.value,
                                                endColor.value,
                                            ),
                                        start = gradientOffset.start,
                                        end = gradientOffset.end,
                                    ),
                                )
                        }
                    } else {
                        Modifier.background(md_theme_dark_background)
                    },
                ),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Canvas Layout
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .height(screenInfo.hDP.dp)
                            .fillMaxWidth()
                            .alpha(
                                if (!showHideMiddleLayout) 1f else 0f,
                            ),
                ) {
                    // Canvas Layout
                    Crossfade(targetState = screenDataState.canvasData?.isVideo) { isVideo ->
                        if (isVideo == true) {
                            screenDataState.canvasData?.url?.let {
                                MediaPlayerView(
                                    url = it,
                                    modifier =
                                        Modifier
                                            .fillMaxHeight()
                                            .wrapContentWidth(unbounded = true, align = Alignment.CenterHorizontally)
                                            .align(Alignment.Center),
                                )
                            }
                        } else if (isVideo == false) {
                            AsyncImage(
                                model =
                                    ImageRequest
                                        .Builder(LocalContext.current)
                                        .data(screenDataState.canvasData?.url)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .diskCacheKey(screenDataState.canvasData?.url)
                                        .crossfade(550)
                                        .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                    Crossfade(
                        targetState = (screenDataState.canvasData != null && showHideControlLayout),
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .align(
                                    Alignment.BottomCenter,
                                ),
                    ) {
                        if (it) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colorStops =
                                                    arrayOf(
                                                        0.2f to overlay,
                                                        1f to Color.Black,
                                                    ),
                                            ),
                                        ),
                            )
                        }
                    }
                }

                TopAppBar(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .onGloballyPositioned {
                                topAppBarHeightDp =
                                    with(localDensity) {
                                        it.size.height
                                            .toDp()
                                            .value
                                            .toInt()
                                    }
                            },
                    colors =
                        TopAppBarDefaults.topAppBarColors().copy(
                            containerColor = Color.Transparent,
                        ),
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = stringResource(id = R.string.now_playing_upper),
                                style = typo.bodyMedium,
                                color = Color.White,
                            )
                            Text(
                                text = screenDataState.playlistName,
                                style = typo.labelMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .basicMarquee(
                                            iterations = Int.MAX_VALUE,
                                            animationMode = MarqueeAnimationMode.Immediately,
                                        ).focusable(),
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_keyboard_arrow_down_24),
                                contentDescription = "",
                                tint = Color.White,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            showSheet = true
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_more_vert_24),
                                contentDescription = "",
                                tint = Color.White,
                            )
                        }
                    },
                )
                Column {
                    Spacer(
                        modifier =
                            Modifier.height(
                                topAppBarHeightDp.dp,
                            ),
                    )
                    Box {
                        Column(
                            Modifier
                                .fillMaxWidth(),
                        ) {
                            Spacer(
                                modifier =
                                    Modifier
                                        .animateContentSize()
                                        .height(
                                            middleLayoutPaddingDp.dp,
                                        ).fillMaxWidth(),
                            )

                            // Middle Layout
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 40.dp)
                                        .onGloballyPositioned {
                                            middleLayoutHeightDp =
                                                with(localDensity) {
                                                    it.size.height
                                                        .toDp()
                                                        .value
                                                        .toInt()
                                                }
                                        }.alpha(
                                            if (showHideMiddleLayout) 1f else 0f,
                                        ).aspectRatio(1f),
                            ) {
                                // IS SONG => Show Artwork
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier =
                                        Modifier
                                            .align(Alignment.Center)
                                            .background(Color.Transparent)
                                            .shadow(
                                                elevation = 3.dp,
                                                shape = RoundedCornerShape(8.dp),
                                                spotColor =
                                                    spotShadowColor.copy(
                                                        alpha = 0.6f,
                                                    ),
                                                ambientColor = Color.Transparent,
                                            ),
                                ) {
                                    AsyncImage(
                                        model =
                                            ImageRequest
                                                .Builder(LocalContext.current)
                                                .data(screenDataState.thumbnailURL)
                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                .diskCacheKey(screenDataState.thumbnailURL + "BIGGER")
                                                .crossfade(550)
                                                .build(),
                                        contentDescription = "",
                                        onSuccess = {
                                            bitmap =
                                                it.result.image
                                                    .toBitmap()
                                                    .asImageBitmap()
                                        },
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(id = R.drawable.holder),
                                        modifier =
                                            Modifier
                                                .align(Alignment.Center)
                                                .padding(3.dp)
                                                .fillMaxWidth()
                                                .background(Color.Transparent)
                                                .aspectRatio(
                                                    if (!screenDataState.isVideo) 1f else 16f / 9,
                                                ).clip(
                                                    RoundedCornerShape(8.dp),
                                                ).alpha(
                                                    if (!screenDataState.isVideo || !shouldShowVideo) 1f else 0f,
                                                ),
                                    )
                                }

                                // IS VIDEO => Show Video
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = screenDataState.isVideo && shouldShowVideo,
                                    modifier = Modifier.align(Alignment.Center),
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(16f / 9)
                                                .clip(
                                                    RoundedCornerShape(8.dp),
                                                ).background(
                                                    md_theme_dark_background,
                                                ),
                                    ) {
                                        // Player
                                        Box(Modifier.fillMaxSize()) {
                                            MediaPlayerView(player = koinInject(), modifier = Modifier.align(Alignment.Center))
                                        }
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .clickable(
                                                        onClick = { showHideFullscreenOverlay = !showHideFullscreenOverlay },
                                                        indication = null,
                                                        interactionSource =
                                                            remember {
                                                                MutableInteractionSource()
                                                            },
                                                    ),
                                        ) {
                                            Crossfade(
                                                targetState = showHideFullscreenOverlay,
                                            ) {
                                                if (it) {
                                                    Box(
                                                        modifier =
                                                            Modifier
                                                                .fillMaxSize()
                                                                .background(
                                                                    Brush.verticalGradient(
                                                                        colorStops =
                                                                            arrayOf(
                                                                                0.03f to blackMoreOverlay,
                                                                                0.15f to overlay,
                                                                                0.8f to Color.Transparent,
                                                                            ),
                                                                    ),
                                                                ),
                                                    ) {
                                                        IconButton(onClick = {
                                                            navController.navigateSafe(
                                                                R.id.action_global_fullscreenFragment,
                                                            )
                                                        }, Modifier.align(Alignment.TopEnd)) {
                                                            Icon(
                                                                painter = painterResource(id = R.drawable.baseline_fullscreen_24),
                                                                contentDescription = "",
                                                                tint = Color.White,
                                                            )
                                                        }
                                                        Row(
                                                            Modifier
                                                                .align(Alignment.Center)
                                                                .fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                                        ) {
                                                            FilledTonalIconButton(
                                                                colors =
                                                                    IconButtonDefaults.iconButtonColors().copy(
                                                                        containerColor = Color.Transparent,
                                                                    ),
                                                                modifier =
                                                                    Modifier
                                                                        .size(48.dp)
                                                                        .aspectRatio(1f)
                                                                        .clip(
                                                                            CircleShape,
                                                                        ),
                                                                onClick = {
                                                                    sharedViewModel.onUIEvent(UIEvent.Backward)
                                                                },
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Filled.Replay5,
                                                                    tint = Color.White,
                                                                    contentDescription = "",
                                                                    modifier =
                                                                        Modifier
                                                                            .size(36.dp)
                                                                            .alpha(0.8f),
                                                                )
                                                            }
                                                            FilledTonalIconButton(
                                                                colors =
                                                                    IconButtonDefaults.iconButtonColors().copy(
                                                                        containerColor = Color.Transparent,
                                                                    ),
                                                                modifier =
                                                                    Modifier
                                                                        .size(48.dp)
                                                                        .aspectRatio(1f)
                                                                        .clip(
                                                                            CircleShape,
                                                                        ),
                                                                onClick = {
                                                                    sharedViewModel.onUIEvent(UIEvent.Forward)
                                                                },
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Filled.Forward5,
                                                                    tint = Color.White,
                                                                    contentDescription = "",
                                                                    modifier =
                                                                        Modifier
                                                                            .size(36.dp)
                                                                            .alpha(0.8f),
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(
                                modifier =
                                    Modifier
                                        .animateContentSize()
                                        .height(
                                            middleLayoutPaddingDp.dp,
                                        ).fillMaxWidth(),
                            )

                            // Info Layout
                            Box {
                                Column(
                                    Modifier
                                        .alpha(controlLayoutAlpha)
                                        .onGloballyPositioned {
                                            infoLayoutHeightDp =
                                                with(localDensity) {
                                                    it.size.height
                                                        .toDp()
                                                        .value
                                                        .toInt()
                                                }
                                        },
                                ) {
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 40.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        AnimatedVisibility(screenDataState.canvasData != null) {
                                            AsyncImage(
                                                model =
                                                    ImageRequest
                                                        .Builder(LocalContext.current)
                                                        .data(screenDataState.thumbnailURL)
                                                        .diskCachePolicy(CachePolicy.ENABLED)
                                                        .diskCacheKey(screenDataState.thumbnailURL + "BIGGER")
                                                        .crossfade(true)
                                                        .build(),
                                                placeholder = painterResource(R.drawable.holder),
                                                error = painterResource(R.drawable.holder),
                                                contentDescription = null,
                                                contentScale = ContentScale.FillWidth,
                                                modifier =
                                                    Modifier
                                                        .heightIn(0.dp, 55.dp)
                                                        .width(55.dp)
                                                        .padding(end = 10.dp)
                                                        .clip(
                                                            RoundedCornerShape(4.dp),
                                                        ).align(Alignment.CenterVertically),
                                            )
                                        }

                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = screenDataState.nowPlayingTitle,
                                                style = typo.headlineMedium,
                                                maxLines = 1,
                                                color = Color.White,
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                                        .basicMarquee(
                                                            iterations = Int.MAX_VALUE,
                                                            animationMode = MarqueeAnimationMode.Immediately,
                                                        ).focusable(),
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            LazyRow(verticalAlignment = Alignment.CenterVertically) {
                                                item {
                                                    androidx.compose.animation.AnimatedVisibility(visible = screenDataState.isExplicit) {
                                                        ExplicitBadge(
                                                            modifier =
                                                                Modifier
                                                                    .size(20.dp)
                                                                    .padding(end = 4.dp)
                                                                    .weight(1f),
                                                        )
                                                    }
                                                }
                                                item {
                                                    Text(
                                                        text = screenDataState.artistName,
                                                        style = typo.bodyMedium,
                                                        maxLines = 1,
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .wrapContentHeight(align = Alignment.CenterVertically)
                                                                .basicMarquee(
                                                                    iterations = Int.MAX_VALUE,
                                                                    animationMode = MarqueeAnimationMode.Immediately,
                                                                ).focusable()
                                                                .clickable {
                                                                    val song = sharedViewModel.nowPlayingState.value?.songEntity
                                                                    navController.navigateSafe(
                                                                        R.id.action_global_artistFragment,
                                                                        bundleOf(
                                                                            "channelId" to
                                                                                (
                                                                                    song?.artistId?.firstOrNull()
                                                                                        ?: screenDataState.songInfoData?.authorId
                                                                                ),
                                                                        ),
                                                                    )
                                                                },
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.size(10.dp))
                                        HeartCheckBox(checked = controllerState.isLiked, size = 32) {
                                            sharedViewModel.onUIEvent(UIEvent.ToggleLike)
                                        }
                                    }
                                    // Real Slider
                                    Box(
                                        Modifier
                                            .padding(
                                                top = 15.dp,
                                            ).padding(horizontal = 40.dp)
                                            .isElementVisible {
                                                shouldShowToolbar = !it
                                            },
                                    ) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(24.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Crossfade(timelineState.loading) {
                                                if (it) {
                                                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                                        LinearProgressIndicator(
                                                            modifier =
                                                                Modifier
                                                                    .fillMaxWidth()
                                                                    .height(4.dp)
                                                                    .padding(
                                                                        horizontal = 3.dp,
                                                                    ).clip(
                                                                        RoundedCornerShape(8.dp),
                                                                    ),
                                                            color = Color.Gray,
                                                            trackColor = Color.DarkGray,
                                                            strokeCap = StrokeCap.Round,
                                                        )
                                                    }
                                                } else {
                                                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                                        LinearProgressIndicator(
                                                            progress = { timelineState.bufferedPercent.toFloat() / 100 },
                                                            modifier =
                                                                Modifier
                                                                    .fillMaxWidth()
                                                                    .height(4.dp)
                                                                    .padding(
                                                                        horizontal = 3.dp,
                                                                    ).clip(
                                                                        RoundedCornerShape(8.dp),
                                                                    ),
                                                            color = Color.Gray,
                                                            trackColor =
                                                                Color.Gray.copy(
                                                                    alpha = 0.6f,
                                                                ),
                                                            strokeCap = StrokeCap.Round,
                                                            drawStopIndicator = {},
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                            Slider(
                                                value = sliderValue,
                                                onValueChange = {
                                                    sharedViewModel.onUIEvent(
                                                        UIEvent.UpdateProgress(it),
                                                    )
                                                },
                                                valueRange = 0f..100f,
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 3.dp)
                                                        .align(
                                                            Alignment.TopCenter,
                                                        ),
                                                track = { sliderState ->
                                                    SliderDefaults.Track(
                                                        modifier =
                                                            Modifier
                                                                .height(5.dp),
                                                        enabled = true,
                                                        sliderState = sliderState,
                                                        colors =
                                                            SliderDefaults.colors().copy(
                                                                thumbColor = Color.White,
                                                                activeTrackColor = Color.White,
                                                                inactiveTrackColor = Color.Transparent,
                                                            ),
                                                        thumbTrackGapSize = 0.dp,
                                                        drawTick = { _, _ -> },
                                                        drawStopIndicator = null,
                                                    )
                                                },
                                                thumb = {
                                                    SliderDefaults.Thumb(
                                                        modifier =
                                                            Modifier
                                                                .height(18.dp)
                                                                .width(8.dp)
                                                                .padding(
                                                                    vertical = 4.dp,
                                                                ),
                                                        thumbSize = DpSize(8.dp, 8.dp),
                                                        interactionSource =
                                                            remember {
                                                                MutableInteractionSource()
                                                            },
                                                        colors =
                                                            SliderDefaults.colors().copy(
                                                                thumbColor = Color.White,
                                                                activeTrackColor = Color.White,
                                                                inactiveTrackColor = Color.Transparent,
                                                            ),
                                                        enabled = true,
                                                    )
                                                },
                                            )
                                        }
                                    }
                                    // Time Layout
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 40.dp),
                                    ) {
                                        Text(
                                            text = formatDuration(timelineState.current, context),
                                            style = typo.bodyMedium,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Left,
                                        )
                                        Text(
                                            text = formatDuration(timelineState.total, context),
                                            style = typo.bodyMedium,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Right,
                                        )
                                    }

                                    Spacer(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(5.dp),
                                    )
                                    // Control Button Layout
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(96.dp)
                                                .padding(horizontal = 40.dp),
                                    ) {
                                        FilledTonalIconButton(
                                            colors =
                                                IconButtonDefaults.iconButtonColors().copy(
                                                    containerColor = Color.Transparent,
                                                ),
                                            modifier =
                                                Modifier
                                                    .size(48.dp)
                                                    .aspectRatio(1f)
                                                    .clip(
                                                        CircleShape,
                                                    ).weight(1f),
                                            onClick = {
                                                sharedViewModel.onUIEvent(UIEvent.Shuffle)
                                            },
                                        ) {
                                            Crossfade(targetState = controllerState.isShuffle, label = "Shuffle Button") { isShuffle ->
                                                if (!isShuffle) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Shuffle,
                                                        tint = Color.White,
                                                        contentDescription = "",
                                                        modifier = Modifier.size(32.dp),
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Filled.Shuffle,
                                                        tint = seed,
                                                        contentDescription = "",
                                                        modifier = Modifier.size(32.dp),
                                                    )
                                                }
                                            }
                                        }
                                        FilledTonalIconButton(
                                            colors =
                                                IconButtonDefaults.iconButtonColors().copy(
                                                    containerColor = Color.Transparent,
                                                ),
                                            modifier =
                                                Modifier
                                                    .size(72.dp)
                                                    .aspectRatio(1f)
                                                    .clip(
                                                        CircleShape,
                                                    ).weight(1f),
                                            onClick = {
                                                if (controllerState.isPreviousAvailable) {
                                                    sharedViewModel.onUIEvent(UIEvent.Previous)
                                                }
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.SkipPrevious,
                                                tint = if (controllerState.isPreviousAvailable) Color.White else Color.Gray,
                                                contentDescription = "",
                                                modifier = Modifier.size(52.dp),
                                            )
                                        }
                                        FilledTonalIconButton(
                                            colors =
                                                IconButtonDefaults.iconButtonColors().copy(
                                                    containerColor = Color.Transparent,
                                                ),
                                            modifier =
                                                Modifier
                                                    .size(96.dp)
                                                    .aspectRatio(1f)
                                                    .clip(
                                                        CircleShape,
                                                    ).weight(1f),
                                            onClick = {
                                                sharedViewModel.onUIEvent(UIEvent.PlayPause)
                                            },
                                        ) {
                                            Crossfade(targetState = controllerState.isPlaying) { isPlaying ->
                                                if (!isPlaying) {
                                                    Icon(
                                                        imageVector = Icons.Filled.PlayCircle,
                                                        tint = Color.White,
                                                        contentDescription = "",
                                                        modifier = Modifier.size(72.dp),
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Filled.PauseCircle,
                                                        tint = Color.White,
                                                        contentDescription = "",
                                                        modifier = Modifier.size(72.dp),
                                                    )
                                                }
                                            }
                                        }
                                        FilledTonalIconButton(
                                            colors =
                                                IconButtonDefaults.iconButtonColors().copy(
                                                    containerColor = Color.Transparent,
                                                ),
                                            modifier =
                                                Modifier
                                                    .size(72.dp)
                                                    .aspectRatio(1f)
                                                    .clip(
                                                        CircleShape,
                                                    ).weight(1f),
                                            onClick = {
                                                if (controllerState.isNextAvailable) {
                                                    sharedViewModel.onUIEvent(UIEvent.Next)
                                                }
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.SkipNext,
                                                tint = if (controllerState.isNextAvailable) Color.White else Color.Gray,
                                                contentDescription = "",
                                                modifier = Modifier.size(52.dp),
                                            )
                                        }
                                        FilledTonalIconButton(
                                            colors =
                                                IconButtonDefaults.iconButtonColors().copy(
                                                    containerColor = Color.Transparent,
                                                ),
                                            modifier =
                                                Modifier
                                                    .size(48.dp)
                                                    .aspectRatio(1f)
                                                    .clip(
                                                        CircleShape,
                                                    ).weight(1f),
                                            onClick = {
                                                sharedViewModel.onUIEvent(UIEvent.Repeat)
                                            },
                                        ) {
                                            Crossfade(targetState = controllerState.repeatState) { rs ->
                                                when (rs) {
                                                    is RepeatState.None -> {
                                                        Icon(
                                                            imageVector = Icons.Filled.Repeat,
                                                            tint = Color.White,
                                                            contentDescription = "",
                                                            modifier = Modifier.size(32.dp),
                                                        )
                                                    }

                                                    RepeatState.All -> {
                                                        Icon(
                                                            imageVector = Icons.Filled.Repeat,
                                                            tint = seed,
                                                            contentDescription = "",
                                                            modifier = Modifier.size(32.dp),
                                                        )
                                                    }

                                                    RepeatState.One -> {
                                                        Icon(
                                                            imageVector = Icons.Filled.RepeatOne,
                                                            tint = seed,
                                                            contentDescription = "",
                                                            modifier = Modifier.size(32.dp),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    // List Bottom Buttons
                                    // 24.dp
                                    Box(
                                        modifier =
                                            Modifier
                                                .height(32.dp)
                                                .fillMaxWidth()
                                                .padding(horizontal = 40.dp),
                                    ) {
                                        IconButton(
                                            modifier =
                                                Modifier
                                                    .size(24.dp)
                                                    .aspectRatio(1f)
                                                    .align(Alignment.CenterStart)
                                                    .clip(
                                                        CircleShape,
                                                    ),
                                            onClick = {
                                                showInfoBottomSheet = true
                                            },
                                        ) {
                                            Icon(imageVector = Icons.Outlined.Info, tint = Color.White, contentDescription = "")
                                        }
                                        Row(
                                            Modifier.align(Alignment.CenterEnd),
                                        ) {
                                            Crossfade(
                                                targetState = likeStatus,
                                            ) {
                                                if (it) {
                                                    IconButton(
                                                        modifier =
                                                            Modifier
                                                                .size(24.dp)
                                                                .aspectRatio(1f)
                                                                .clip(
                                                                    CircleShape,
                                                                ),
                                                        onClick = {
                                                            sharedViewModel.addToYouTubeLiked()
                                                        },
                                                    ) {
                                                        Icon(imageVector = Icons.Filled.Done, tint = Color.White, contentDescription = "")
                                                    }
                                                } else {
                                                    IconButton(
                                                        modifier =
                                                            Modifier
                                                                .size(24.dp)
                                                                .aspectRatio(1f)
                                                                .clip(
                                                                    CircleShape,
                                                                ),
                                                        onClick = {
                                                            sharedViewModel.addToYouTubeLiked()
                                                        },
                                                    ) {
                                                        Icon(imageVector = Icons.Filled.Add, tint = Color.White, contentDescription = "")
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.size(8.dp))
                                            IconButton(
                                                modifier =
                                                    Modifier
                                                        .size(24.dp)
                                                        .aspectRatio(1f)
                                                        .clip(
                                                            CircleShape,
                                                        ),
                                                onClick = {
                                                    showQueueBottomSheet = true
                                                },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Outlined.QueueMusic,
                                                    tint = Color.White,
                                                    contentDescription = "",
                                                )
                                            }
                                        }
                                    }
                                }
                                androidx.compose.animation.AnimatedVisibility(visible = !showHideControlLayout) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .height(
                                                    infoLayoutHeightDp.dp,
                                                ).fillMaxWidth()
                                                .padding(
                                                    vertical = 20.dp,
                                                    horizontal = 40.dp,
                                                ).clickable(
                                                    onClick = {
                                                        if (mainScrollState.value == 0) {
                                                            showHideJob = true
                                                            showHideControlLayout = !showHideControlLayout
                                                        }
                                                    },
                                                    indication = null,
                                                    interactionSource =
                                                        remember {
                                                            MutableInteractionSource()
                                                        },
                                                ),
                                        contentAlignment = Alignment.BottomStart,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            val thumb = screenDataState.songInfoData?.authorThumbnail
                                            AsyncImage(
                                                model =
                                                    ImageRequest
                                                        .Builder(LocalContext.current)
                                                        .data(thumb)
                                                        .diskCachePolicy(CachePolicy.ENABLED)
                                                        .diskCacheKey(thumb)
                                                        .crossfade(550)
                                                        .build(),
                                                placeholder = painterResource(R.drawable.holder),
                                                error = painterResource(R.drawable.holder),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier =
                                                    Modifier
                                                        .size(42.dp)
                                                        .clip(
                                                            CircleShape,
                                                        ),
                                            )
                                            Spacer(modifier = Modifier.size(12.dp))
                                            Text(
                                                text = screenDataState.songInfoData?.author ?: "",
                                                style = typo.labelMedium,
                                                color = Color.White,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // Touch Area
                        androidx.compose.animation.AnimatedVisibility(visible = screenDataState.canvasData != null) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(
                                            (middleLayoutPaddingDp * 2 + middleLayoutHeightDp).dp,
                                        ).clickable(
                                            onClick = {
                                                if (mainScrollState.value == 0) {
                                                    showHideJob = true
                                                    showHideControlLayout = !showHideControlLayout
                                                }
                                            },
                                            indication = null,
                                            interactionSource =
                                                remember {
                                                    MutableInteractionSource()
                                                },
                                        ),
                            )
                        }
                    }
                    Column(Modifier.padding(horizontal = 40.dp)) {
                        // Lyrics Layout
                        AnimatedVisibility(
                            visible = screenDataState.lyricsData != null,
                            modifier = Modifier.padding(top = 10.dp),
                        ) {
                            ElevatedCard(
                                onClick = {},
                                shape = RoundedCornerShape(8.dp),
                                colors =
                                    CardDefaults.elevatedCardColors().copy(
                                        containerColor = startColor.value,
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(15.dp)) {
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(id = R.string.lyrics),
                                            style = typo.labelMedium,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f),
                                        )
                                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                            TextButton(
                                                onClick = {
                                                    showFullscreenLyrics = true
                                                },
                                                contentPadding = PaddingValues(0.dp),
                                                modifier =
                                                    Modifier
                                                        .height(20.dp)
                                                        .width(40.dp),
                                            ) {
                                                Text(text = stringResource(id = R.string.show))
                                            }
                                        }
                                    }
                                    // Lyrics Layout
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(300.dp),
                                    ) {
                                        screenDataState.lyricsData?.let {
                                            LyricsView(
                                                lyricsData = it,
                                                timeLine = sharedViewModel.timeline,
                                                onLineClick = { f ->
                                                    sharedViewModel.onUIEvent(UIEvent.UpdateProgress(f))
                                                },
                                            )
                                        }
                                    }

                                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                        Text(
                                            text =
                                                when (screenDataState.lyricsData?.lyrics?.syncType) {
                                                    "LINE_SYNCED" -> stringResource(id = R.string.line_synced)
                                                    else -> stringResource(id = R.string.unsynced)
                                                },
                                            style = typo.bodySmall,
                                            textAlign = TextAlign.End,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 10.dp),
                                        )
                                        Text(
                                            text =
                                                when (screenDataState.lyricsData?.lyricsProvider) {
                                                    LyricsProvider.MUSIXMATCH -> stringResource(id = R.string.lyrics_provider)
                                                    LyricsProvider.LRCLIB -> stringResource(id = R.string.lyrics_provider_lrc)
                                                    LyricsProvider.YOUTUBE -> stringResource(id = R.string.lyrics_provider_youtube)
                                                    LyricsProvider.SPOTIFY -> stringResource(id = R.string.spotify_lyrics_provider)
                                                    LyricsProvider.OFFLINE -> stringResource(id = R.string.offline_mode)
                                                    else -> {
                                                        ""
                                                    }
                                                },
                                            style = typo.bodySmall,
                                            textAlign = TextAlign.End,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        AnimatedVisibility(visible = screenDataState.songInfoData != null) {
                            ElevatedCard(
                                onClick = {
                                    val song = sharedViewModel.nowPlayingState.value?.songEntity
                                    if (song != null && song.artistId?.firstOrNull() != null) {
                                        navController.navigateSafe(
                                            R.id.action_global_artistFragment,
                                            bundleOf(
                                                "channelId" to song.artistId.firstOrNull(),
                                            ),
                                        )
                                    } else {
                                        navController.navigateSafe(
                                            R.id.action_global_artistFragment,
                                            bundleOf(
                                                "channelId" to screenDataState.songInfoData?.authorId,
                                            ),
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors =
                                    CardDefaults.elevatedCardColors().copy(
                                        containerColor = startColor.value,
                                    ),
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(250.dp),
                                ) {
                                    val thumb = screenDataState.songInfoData?.authorThumbnail
                                    AsyncImage(
                                        model =
                                            ImageRequest
                                                .Builder(LocalContext.current)
                                                .data(thumb)
                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                .diskCacheKey(thumb)
                                                .crossfade(550)
                                                .build(),
                                        placeholder = painterResource(R.drawable.holder_video),
                                        error = painterResource(R.drawable.holder_video),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .alpha(0.8f)
                                                .clip(
                                                    RoundedCornerShape(8.dp),
                                                ),
                                    )
                                    Box(
                                        modifier =
                                            Modifier
                                                .padding(15.dp)
                                                .fillMaxSize(),
                                    ) {
                                        Column(Modifier.align(Alignment.TopStart)) {
                                            Spacer(modifier = Modifier.height(5.dp))
                                            Text(
                                                text = stringResource(id = R.string.artists),
                                                style = typo.labelMedium,
                                                color = Color.White,
                                            )
                                        }
                                        Column(Modifier.align(Alignment.BottomStart)) {
                                            Text(
                                                text = screenDataState.songInfoData?.author ?: "",
                                                style = typo.labelMedium,
                                                color = Color.White,
                                            )
                                            Spacer(modifier = Modifier.height(5.dp))
                                            Text(
                                                text = screenDataState.songInfoData?.subscribers ?: "",
                                                style = typo.bodySmall,
                                                textAlign = TextAlign.End,
                                            )
                                            Spacer(modifier = Modifier.height(5.dp))
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        AnimatedVisibility(visible = screenDataState.songInfoData != null) {
                            ElevatedCard(
                                onClick = {},
                                shape = RoundedCornerShape(8.dp),
                                colors =
                                    CardDefaults.elevatedCardColors().copy(
                                        containerColor = startColor.value,
                                    ),
                            ) {
                                Column(
                                    Modifier
                                        .padding(15.dp)
                                        .fillMaxWidth(),
                                ) {
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = stringResource(id = R.string.published_at, screenDataState.songInfoData?.uploadDate ?: ""),
                                        style = typo.labelSmall,
                                        color = Color.White,
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text =
                                            stringResource(
                                                id = R.string.view_count,
                                                "%,d".format(screenDataState.songInfoData?.viewCount),
                                            ),
                                        style = typo.labelMedium,
                                        color = Color.White,
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text =
                                            stringResource(
                                                id = R.string.like_and_dislike,
                                                screenDataState.songInfoData?.like ?: 0,
                                                screenDataState.songInfoData?.dislike ?: 0,
                                            ),
                                        style = typo.bodyMedium,
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = stringResource(id = R.string.description),
                                        style = typo.labelSmall,
                                        color = Color.White,
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    DescriptionView(
                                        text = screenDataState.songInfoData?.description ?: "",
                                        onTimeClicked = { raw ->
                                            val timestamp = parseTimestampToMilliseconds(raw)
                                            if (timestamp != 0.0 && timestamp < timelineState.total) {
                                                sharedViewModel.onUIEvent(
                                                    UIEvent.UpdateProgress(
                                                        ((timestamp * 100) / timelineState.total).toFloat(),
                                                    ),
                                                )
                                            }
                                        },
                                        onURLClicked = { url ->
                                            uriHandler.openUri(
                                                url,
                                            )
                                        },
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Spacer(
                            modifier =
                                Modifier.height(
                                    with(localDensity) { WindowInsets.systemBars.getBottom(localDensity).toDp() },
                                ),
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = shouldShowToolbar,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            ElevatedCard(
                elevation = CardDefaults.elevatedCardElevation(10.dp),
                shape = RectangleShape,
                colors =
                    CardDefaults.elevatedCardColors(
                        containerColor =
                            startColor.value
                                .copy(
                                    red = startColor.value.red - 0.05f,
                                    green = startColor.value.green - 0.05f,
                                    blue = startColor.value.blue - 0.05f,
                                ),
                    ),
                modifier =
                    Modifier
                        .clipToBounds()
                        .wrapContentHeight()
                        .fillMaxWidth(),
            ) {
                Box(
                    modifier =
                        Modifier.padding(
                            top = with(localDensity) { WindowInsets.statusBars.getTop(localDensity).toDp() },
                        ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .padding(
                                    vertical = 8.dp,
                                    horizontal = 15.dp,
                                ).fillMaxWidth(),
                    ) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Box(modifier = Modifier.weight(1F)) {
                            Column(
                                Modifier
                                    .wrapContentHeight(),
                            ) {
                                Text(
                                    text = screenDataState.nowPlayingTitle,
                                    style = typo.bodyMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(
                                                align = Alignment.CenterVertically,
                                            ).basicMarquee(
                                                iterations = Int.MAX_VALUE,
                                                animationMode = MarqueeAnimationMode.Immediately,
                                            ).focusable(),
                                )
                                LazyRow(verticalAlignment = Alignment.CenterVertically) {
                                    item {
                                        androidx.compose.animation.AnimatedVisibility(visible = screenDataState.isExplicit) {
                                            ExplicitBadge(
                                                modifier =
                                                    Modifier
                                                        .size(20.dp)
                                                        .padding(end = 4.dp)
                                                        .weight(1f),
                                            )
                                        }
                                    }
                                    item {
                                        Text(
                                            text = screenDataState.artistName,
                                            style = typo.bodySmall,
                                            maxLines = 1,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight(
                                                        align = Alignment.CenterVertically,
                                                    ).basicMarquee(
                                                        iterations = Int.MAX_VALUE,
                                                        animationMode = MarqueeAnimationMode.Immediately,
                                                    ).focusable(),
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        HeartCheckBox(checked = controllerState.isLiked, size = 30) {
                            sharedViewModel.onUIEvent(UIEvent.ToggleLike)
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        Crossfade(targetState = timelineState.loading, label = "") {
                            if (it) {
                                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.LightGray,
                                        strokeWidth = 3.dp,
                                    )
                                }
                            } else {
                                PlayPauseButton(isPlaying = controllerState.isPlaying, modifier = Modifier.size(48.dp)) {
                                    sharedViewModel.onUIEvent(UIEvent.PlayPause)
                                }
                            }
                        }
                    }
                    Box(
                        modifier =
                            Modifier
                                .wrapContentSize(Alignment.Center)
                                .align(Alignment.BottomCenter),
                    ) {
                        LinearProgressIndicator(
                            progress = { timelineState.current.toFloat() / timelineState.total },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(
                                        color = Color.Transparent,
                                        shape = RoundedCornerShape(4.dp),
                                    ),
                            color = Color.White,
                            trackColor = Color.Gray.copy(alpha = 0.4f),
                            strokeCap = StrokeCap.Round,
                            drawStopIndicator = {},
                        )
                    }
                }
            }
        }
    }
}