package com.abcg.music.ui.fragment.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.findNavController
import com.abcg.music.ui.screen.player.NowPlayingScreen
import com.abcg.music.ui.theme.AppTheme
import com.abcg.music.viewModel.SharedViewModel

@UnstableApi
class NowPlayingFragment : Fragment() {
    val viewModel by activityViewModel<SharedViewModel>()
    private lateinit var composeView: ComposeView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).also {
            composeView = it
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    Scaffold { paddingValues ->
                        NowPlayingScreen(sharedViewModel = viewModel, navController = findNavController())
                    }
                }
            }
        }
    }

    fun connectArtists(artists: List<String>): String {
        val stringBuilder = StringBuilder()

        for ((index, artist) in artists.withIndex()) {
            stringBuilder.append(artist)

            if (index < artists.size - 1) {
                stringBuilder.append(", ")
            }
        }

        return stringBuilder.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        arguments?.putString("type", null)
        arguments?.putString("videoId", null)
    }
}