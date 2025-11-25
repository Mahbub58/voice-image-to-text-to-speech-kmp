package com.mahbub.realtimevoicetranslate_kmp.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahbub.realtimevoicetranslate_kmp.data.ListeningStatus
import com.mahbub.realtimevoicetranslate_kmp.data.TranscriptState
import com.mahbub.realtimevoicetranslate_kmp.presentation.component.BottomBar
import com.mahbub.realtimevoicetranslate_kmp.presentation.component.LanguageSelectionDialog
import com.mahbub.realtimevoicetranslate_kmp.presentation.component.PermissionNeedDialog
import com.mahbub.realtimevoicetranslate_kmp.presentation.component.VoiceAnimation
import com.mahbub.realtimevoicetranslate_kmp.presentation.component.TopBar
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel


interface PermissionDialogEvents {
    fun onDismissRequest()
    fun onClickGoToSettings()
}


@Composable
fun SpeechToTextScreen(onBack: () -> Unit) {
    val viewModel = koinViewModel<SpeechToTextViewModel>()
    val transcriptState = viewModel.transcriptState.collectAsState()
    val uiEvent = viewModel.uiEvent.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiEvent.value) {
        uiEvent.value?.let { event ->
            when (event) {
                is SpeechToTextViewModel.UiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                    viewModel.onUiEventHandled()
                }
            }
        }
    }

        SpeechToTextContent(
            snackbarHostState = snackbarHostState,
            transcriptState = transcriptState.value,
            onLanguageSelected = viewModel::onLanguageSelected,
            onClickMic = viewModel::onClickMic,
            onClickCopy = viewModel::onClickCopy,
            onBack = onBack,

            permissionDialogEvents = object : PermissionDialogEvents {
                override fun onDismissRequest() {
                    viewModel.onDismissRequest()
                }

                override fun onClickGoToSettings() {
                    viewModel.openAppSettings()
                }
            }
        )
    }


@Composable
fun SpeechToTextContent(
    snackbarHostState: SnackbarHostState,
    transcriptState: TranscriptState,
    permissionDialogEvents: PermissionDialogEvents,
    onLanguageSelected: (String) -> Unit,
    onClickMic: () -> Unit,
    onClickCopy: () -> Unit,
    onBack: () -> Unit,
) {
    var showLanguageDialog by remember { mutableStateOf(false) }

    var selectedLanguage by remember {
        mutableStateOf(
            transcriptState.selectedLanguage
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            supportedLanguages = transcriptState.supportedLanguages,
            onSelected = {
                onLanguageSelected(it)
                selectedLanguage = it
                showLanguageDialog = false
            },
            onDismissRequest = {
                showLanguageDialog = false
            },
            selectedLanguage = selectedLanguage
        )
    }

    if (transcriptState.showPermissionNeedDialog) {
        PermissionNeedDialog(
            onDismissRequest = {permissionDialogEvents.onDismissRequest()},
            onClickGoToSettings = {permissionDialogEvents.onClickGoToSettings()}
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopBar(title = "Speech to Text", onBack = onBack) },
        bottomBar = {
            BottomBar(
                onClickShowLanguages = {
                    showLanguageDialog = true
                },
                onClickMic = {
                    onClickMic()
                },
                onClickCopy = {
                    onClickCopy()
                },
                isListening = transcriptState.listeningStatus == ListeningStatus.LISTENING
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    color = MaterialTheme.colorScheme.background
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            val (result, resultTextColor) = when {
                transcriptState.error.isError -> {
                    "ERROR: ${transcriptState.error.message}" to MaterialTheme.colorScheme.error
                }

                transcriptState.transcript != null -> {
                    transcriptState.transcript to MaterialTheme.colorScheme.onBackground
                }

                else -> "" to MaterialTheme.colorScheme.onBackground
            }

            val scrollState = rememberScrollState()

            LaunchedEffect(result) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(
                        vertical = 5.dp,
                        horizontal = 10.dp
                    )
                    .verticalScroll(scrollState)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (result.isBlank()) "Start to Listen" else result,
                    style = TextStyle(
                        fontSize = 28.sp,
                        letterSpacing = TextUnit(
                            1.5f, TextUnitType.Sp
                        ),
                        textAlign = TextAlign.Center,
                        color = resultTextColor,
                        fontFamily = FontFamily.Serif,
                    )
                )
            }

            VoiceAnimation(transcriptState.listeningStatus == ListeningStatus.LISTENING)

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

}
