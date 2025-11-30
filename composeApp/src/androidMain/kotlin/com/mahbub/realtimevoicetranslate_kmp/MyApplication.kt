package com.mahbub.realtimevoicetranslate_kmp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.mahbub.realtimevoicetranslate_kmp.core.platform.di.platformModule
import com.mahbub.realtimevoicetranslate_kmp.core.platform.di.sharedModule
import com.mahbub.realtimevoicetranslate_kmp.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.lang.ref.WeakReference

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@MyApplication)
            modules(
                module {
                    single { applicationContext }
                },
                platformModule,
                sharedModule
            )
        }
    }
}
