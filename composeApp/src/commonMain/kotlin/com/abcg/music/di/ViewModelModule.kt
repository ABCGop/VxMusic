package com.abcg.music.di

import com.abcg.music.viewModel.AlbumViewModel
import com.abcg.music.viewModel.AnalyticsViewModel
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
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        single {
            SharedViewModel(
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
                get(),
                get(),
            )
        }
        viewModel {
            NowPlayingBottomSheetViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LibraryViewModel(
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
                get(),
                get(),
            )
        }
        viewModel {
            AlbumViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            HomeViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            SettingsViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ArtistViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            PlaylistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LogInViewModel(
                get(),
            )
        }
        viewModel {
            PodcastViewModel(
                get(),
            )
        }
        viewModel {
            MoreAlbumsViewModel(
                get(),
            )
        }
        viewModel {
            RecentlySongsViewModel(
                get(),
            )
        }
        viewModel {
            LocalPlaylistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            NotificationViewModel(
                get(),
            )
        }
        viewModel {
            MoodViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            AnalyticsViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
    }