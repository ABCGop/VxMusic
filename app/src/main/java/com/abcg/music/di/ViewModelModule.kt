package com.abcg.music.di

import com.abcg.music.viewModel.AlbumViewModel
import com.abcg.music.viewModel.ArtistViewModel
import com.abcg.music.viewModel.HomeViewModel
import com.abcg.music.viewModel.LibraryDynamicPlaylistViewModel
import com.abcg.music.viewModel.LibraryViewModel
import com.abcg.music.viewModel.LocalPlaylistViewModel
import com.abcg.music.viewModel.LogInViewModel
import com.abcg.music.viewModel.MoodViewModel
import com.abcg.music.viewModel.MoreAlbumsViewModel
import com.abcg.music.viewModel.NotificationViewModel
import com.abcg.music.viewModel.NowPlayingBottomSheetViewModel
import com.abcg.music.viewModel.PlaylistViewModel
import com.abcg.music.viewModel.PodcastViewModel
import com.abcg.music.viewModel.RecentlySongsViewModel
import com.abcg.music.viewModel.SearchViewModel
import com.abcg.music.viewModel.SettingsViewModel
import com.abcg.music.viewModel.SharedViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        single {
            SharedViewModel(
                androidApplication(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        single {
            SearchViewModel(
                application = androidApplication(),
                get(),
                get(),
            )
        }
        viewModel {
            NowPlayingBottomSheetViewModel(
                application = androidApplication(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LibraryViewModel(
                application = androidApplication(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LibraryDynamicPlaylistViewModel(
                application = androidApplication(),
                get(),
                get(),
            )
        }
        viewModel {
            AlbumViewModel(
                application = androidApplication(),
                get(),
                get(),
            )
        }
        viewModel {
            HomeViewModel(
                application = androidApplication(),
                get(),
                get(),
            )
        }
        viewModel {
            SettingsViewModel(
                application = androidApplication(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ArtistViewModel(
                application = androidApplication(),
                get(),
                get(),
            )
        }
        viewModel {
            PlaylistViewModel(
                application = androidApplication(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LogInViewModel(
                application = androidApplication(),
                get(),
            )
        }
        viewModel {
            PodcastViewModel(
                application = androidApplication(),
                get(),
            )
        }
        viewModel {
            MoreAlbumsViewModel(
                application = androidApplication(),
                get(),
            )
        }
        viewModel {
            RecentlySongsViewModel(
                application = androidApplication(),
                get(),
            )
        }
        viewModel {
            LocalPlaylistViewModel(
                androidApplication(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            NotificationViewModel(
                androidApplication(),
                get(),
            )
        }
        viewModel {
            MoodViewModel(
                androidApplication(),
                get(),
                get(),
            )
        }
    }
