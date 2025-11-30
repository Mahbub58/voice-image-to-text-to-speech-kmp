package com.mahbub.realtimevoicetranslate_kmp

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.setActivityProvider
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadKoinModules(
            module {
                single<Activity> { this@MainActivity }
            }
        )
        setActivityProvider { this }

        setContent {
            App()
        }
    }
}