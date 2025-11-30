package com.mahbub.realtimevoicetranslate_kmp.core.platform

import com.mahbub.realtimevoicetranslate_kmp.dat.AndroidTTSProvider
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TTSProvider

actual fun getTTSProvider(): TTSProvider {
    return AndroidTTSProvider()
}