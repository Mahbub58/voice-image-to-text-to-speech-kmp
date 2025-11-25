package com.mahbub.realtimevoicetranslate_kmp.di


import com.mahbub.realtimevoicetranslate_kmp.core.platform.di.platformModule
import com.mahbub.realtimevoicetranslate_kmp.core.platform.di.sharedModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(platformModule, sharedModule)
    }
}
