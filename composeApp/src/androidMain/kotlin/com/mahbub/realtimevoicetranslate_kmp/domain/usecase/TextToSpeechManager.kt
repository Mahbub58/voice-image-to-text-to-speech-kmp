package com.mahbub.realtimevoicetranslate_kmp.domain.usecase

import android.app.Activity

private var _activityProvider: () -> Activity? = {
    null
}

val activityProvider: () -> Activity?
    get() = _activityProvider

fun setActivityProvider(provider: () -> Activity?) {
    _activityProvider = provider
}