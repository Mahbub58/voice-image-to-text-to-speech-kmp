package com.mahbub.realtimevoicetranslate_kmp.presentation.screen.textrecognition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mahbub.realtimevoicetranslate_kmp.presentation.component.PermissionNeedDialog
import com.mahbub.realtimevoicetranslate_kmp.presentation.component.TopBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TextRecognitionScreen(onBack: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel = koinViewModel<TextRecognitionViewModel>()
    val state = viewModel.state.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.requestPermission()
    }

    Scaffold(
        topBar = { TopBar(title = "Text Recognition", onBack = onBack) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Recognized Text", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = state.recognizedText.ifBlank { "No text recognized" }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.size(16.dp))
            Button(onClick = { viewModel.pickImageAndRecognize() }, enabled = !state.isProcessing) { Text("Pick Image") }
        }
    }

    if (state.showPermissionNeedDialog) {
        PermissionNeedDialog(
            onDismissRequest = { viewModel.dismissPermissionDialog() },
            onClickGoToSettings = { viewModel.openAppSettings() }
        )
    }
}
