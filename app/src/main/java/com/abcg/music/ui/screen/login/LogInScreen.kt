package com.abcg.music.ui.screen.login

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abcg.music.viewModel.LogInViewModel
import com.abcg.music.viewModel.SettingsViewModel
import com.abcg.music.viewModel.SharedViewModel
import org.koin.compose.koinInject

@Composable
fun LogInScreen(
    logInViewModel: LogInViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
) {
}