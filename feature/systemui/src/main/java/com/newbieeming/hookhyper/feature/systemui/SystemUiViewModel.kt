package com.newbieeming.hookhyper.feature.systemui

import android.content.Context
import com.newbieeming.hookhyper.core.data.HookPreferencesRepository
import com.newbieeming.hookhyper.core.data.RootAppRestarter
import com.newbieeming.hookhyper.core.ui.feature.FeatureViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SystemUiViewModel @Inject constructor(
    @ApplicationContext context: Context,
    preferences: HookPreferencesRepository,
    appRestarter: RootAppRestarter,
) : FeatureViewModel(context, preferences, appRestarter) {

    override val packageName = SystemUiFeatureEntry.PACKAGE_NAME
    override val restartSuccessMessage = context.getString(R.string.restart_systemui_success)
}
