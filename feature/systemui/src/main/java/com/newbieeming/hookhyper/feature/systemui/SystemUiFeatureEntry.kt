package com.newbieeming.hookhyper.feature.systemui

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.newbieeming.hookhyper.core.ui.component.HookContent
import com.newbieeming.hookhyper.core.ui.feature.FeatureEntryImpl
import com.newbieeming.hookhyper.core.ui.feature.FeatureViewModel
import com.newbieeming.hookhyper.feature.systemui.hook.HookRegistry
import javax.inject.Inject

class SystemUiFeatureEntry @Inject constructor() : FeatureEntryImpl() {
    override val targetPackageName = PACKAGE_NAME
    override val hooks: List<HookContent> = HookRegistry.modules.filterIsInstance<HookContent>()

    @Composable
    override fun provideViewModel(): FeatureViewModel = hiltViewModel<SystemUiViewModel>()

    companion object {
        const val PACKAGE_NAME = "com.android.systemui"
    }
}
