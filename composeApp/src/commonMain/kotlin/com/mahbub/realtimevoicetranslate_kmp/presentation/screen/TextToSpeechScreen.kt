package com.mahbub.realtimevoicetranslate_kmp.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mahbub.realtimevoicetranslate_kmp.presentation.component.TopBar
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun TextToSpeechScreen(onBack: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val textState = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val viewModel = koinViewModel<TextToSpeechViewModel>()
    val ttsState = viewModel.state
    LaunchedEffect(ttsState.collectAsState().value) { /* trigger recompositions */ }

    Scaffold(
        topBar = { TopBar(title = "Text to Speech", onBack = onBack) },
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
            Text(text = "Text to speak", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.size(8.dp))
            OutlinedTextField(
                value = textState.value,
                onValueChange = { textState.value = it },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 6
            )
            Spacer(modifier = Modifier.size(16.dp))
            IconButton(onClick = { viewModel.speak(textState.value) }) {
                Icon(imageVector = Icons.Filled.VolumeUp, contentDescription = null)
            }

            Spacer(modifier = Modifier.size(12.dp))
            val highlighted: AnnotatedString = buildAnnotatedString {
                val text = ttsState.value.text
                if (text.isEmpty()) append("") else {
                    val start = ttsState.value.highlightStart
                    val end = ttsState.value.highlightEnd
                    if (start in 0..text.length && end in 0..text.length && start < end) {
                        append(text.substring(0, start))
                        withStyle(SpanStyle(background = MaterialTheme.colorScheme.secondaryContainer)) {
                            append(text.substring(start, end))
                        }
                        append(text.substring(end))
                    } else {
                        append(text)
                    }
                }
            }
            if (ttsState.value.text.isNotBlank()) {
                Text(text = highlighted, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.size(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { viewModel.speak(textState.value) }) { Text("Play") }
                Button(onClick = { viewModel.pause() }, enabled = ttsState.value.isPlaying) { Text("Pause") }
                Button(onClick = { viewModel.resume() }, enabled = ttsState.value.isPaused) { Text("Resume") }
                Button(onClick = { viewModel.stop() }, enabled = ttsState.value.isPlaying || ttsState.value.isPaused) { Text("Stop") }
            }
        }
    }
}
