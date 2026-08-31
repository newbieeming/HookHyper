package com.newbieeming.hookhyper.ui.app

import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.data.HookPreferencesRepository
import com.newbieeming.hookhyper.core.data.ModuleStatusProvider
import com.newbieeming.hookhyper.core.ui.feature.FeatureEntry
import com.newbieeming.hookhyper.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferences: HookPreferencesRepository,
    private val moduleStatusProvider: ModuleStatusProvider,
    featureSet: Set<@JvmSuppressWildcards FeatureEntry>,
) : MviViewModel<AppState, AppIntent, AppEffect>(
    AppState(
        uiStyle = preferences.getUiStyle(),
        predictiveBackEnabled = preferences.getBoolean(PreferenceKeys.PREDICTIVE_BACK_ENABLED),
        moduleStatus = moduleStatusProvider.current(),
    ),
) {
    val features: List<FeatureEntry> = featureSet.sortedBy(FeatureEntry::targetPackageName)
    private val featureEntries = featureSet.associateBy(FeatureEntry::targetPackageName)

    fun feature(targetPackageName: String): FeatureEntry? = featureEntries[targetPackageName]

    override fun onIntent(intent: AppIntent) {
        when (intent) {
            is AppIntent.SelectUiStyle -> {
                preferences.setUiStyle(intent.style)
                reduce { copy(uiStyle = intent.style) }
                sendEffect(AppEffect.UiStyleChanged)
            }
            is AppIntent.SetPredictiveBackEnabled -> {
                preferences.putBoolean(PreferenceKeys.PREDICTIVE_BACK_ENABLED, intent.enabled)
                reduce { copy(predictiveBackEnabled = intent.enabled) }
            }
            AppIntent.RefreshModuleStatus -> reduce { copy(moduleStatus = moduleStatusProvider.current()) }
        }
    }
}
