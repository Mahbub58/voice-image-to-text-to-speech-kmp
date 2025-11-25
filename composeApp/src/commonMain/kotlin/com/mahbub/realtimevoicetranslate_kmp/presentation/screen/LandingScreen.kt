package com.mahbub.realtimevoicetranslate_kmp.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardVoice
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LandingScreen(
    onClickSpeechToText: () -> Unit,
    onClickTextToSpeech: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Real-time Voice Translate", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.size(24.dp))
        Button(onClick = onClickSpeechToText) {
            Icon(imageVector = Icons.Rounded.KeyboardVoice, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Speech to Text")
        }
        Spacer(modifier = Modifier.size(16.dp))
        Button(onClick = onClickTextToSpeech) {
            Icon(imageVector = Icons.Rounded.RecordVoiceOver, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Text to Speech")
        }
    }
}

