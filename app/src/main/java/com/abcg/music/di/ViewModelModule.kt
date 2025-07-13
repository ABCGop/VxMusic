package com.abcg.music.di

import androidx.media3.common.util.UnstableApi
import com.abcg.music.viewModel.AlbumViewModel
import com.abcg.music.viewModel.ArtistViewModel
import com.abcg.music.viewModel.HomeViewModel
import com.abcg.music.viewModel.LibraryDynamicPlaylistViewModel
import com.abcg.music.viewModel.LibraryViewModel
import com.abcg.music.viewModel.LogInViewModel
import com.abcg.music.viewModel.MusixmatchViewModel
import com.abcg.music.viewModel.NowPlayingBottomSheetViewModel
import com.abcg.music.viewModel.PlaylistViewModel
import com.abcg.music.viewModel.SearchViewModel
import com.abcg.music.viewModel.SettingsViewModel
import com.abcg.music.viewModel.SharedViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@UnstableApi
val viewModelModule =
    module {
        single {
            SharedViewModel(
                androidApplication()
            )
        }
        single {
            SearchViewModel(
                application = androidApplication()
            )
        }
        viewModel {
            NowPlayingBottomSheetViewModel(
                application = androidApplication(),
            )
        }
        viewModel {
            LibraryViewModel(
                application = androidApplication(),
            )
        }
        viewModel {
            LibraryDynamicPlaylistViewModel(
                application = androidApplication(),
            )
        }
        viewModel {
            AlbumViewModel(
                application = androidApplication(),
            )
        }
        viewModel {
            HomeViewModel(
                application = androidApplication(),
            )
        }
        viewModel {
            SettingsViewModel(
                application = androidApplication(),
            )
        }
        viewModel {
            ArtistViewModel(
                application = androidApplication(),
            )
        }
        viewModel {
            PlaylistViewModel(
                application = androidApplication(),
            )
        }
        viewModel {
            LogInViewModel(
                application = androidApplication(),
            )
        }
        viewModel {
            MusixmatchViewModel(
                application = androidApplication(),
            )
        }
    }