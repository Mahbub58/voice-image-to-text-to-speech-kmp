package com.mahbub.realtimevoicetranslate_kmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.mahbub.realtimevoicetranslate_kmp.presentation.navigation.AppNavigation
import org.jetbrains.compose.ui.tooling.preview.Preview

import realtimevoicetranslate_kmp.composeapp.generated.resources.Res
import realtimevoicetranslate_kmp.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {

    MaterialTheme {
        AppNavigation()
    }
}
