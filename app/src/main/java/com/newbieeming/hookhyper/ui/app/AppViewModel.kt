package com.newbieeming.hookhyper.ui.app

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.data.HookPreferencesRepository
import com.newbieeming.hookhyper.core.data.ModuleStatusProvider
import com.newbieeming.hookhyper.core.ui.feature.FeatureEntry
import com.newbieeming.hookhyper.core.ui.mvi.MviViewModel
import com.newbieeming.hookhyper.core.ui.theme.ThemeColor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferences: HookPreferencesRepository,
    private val moduleStatusProvider: ModuleStatusProvider,
    featureSet: Set<@JvmSuppressWildcards FeatureEntry>,
) : MviViewModel<AppState, AppIntent, AppEffect>(
    AppState(
        predictiveBackEnabled = preferences.getBoolean(PreferenceKeys.PREDICTIVE_BACK_ENABLED),
        moduleStatus = moduleStatusProvider.current(),
        themeColor = ThemeColor.fromId(preferences.getString(PreferenceKeys.THEME_COLOR)),
    ),
) {
    val features: List<FeatureEntry> = featureSet.sortedBy(FeatureEntry::targetPackageName)
    private val featureEntries = featureSet.associateBy(FeatureEntry::targetPackageName)

    init {
        viewModelScope.launch {
            moduleStatusProvider.status.collect { status ->
                reduce { copy(moduleStatus = status) }
            }
        }
    }

    fun feature(targetPackageName: String): FeatureEntry? = featureEntries[targetPackageName]

    override fun onIntent(intent: AppIntent) {
        when (intent) {
            is AppIntent.SetThemeColor -> {
                preferences.putString(PreferenceKeys.THEME_COLOR, intent.color.id)
                reduce { copy(themeColor = intent.color) }
            }
            is AppIntent.SetPredictiveBackEnabled -> {
                preferences.putBoolean(PreferenceKeys.PREDICTIVE_BACK_ENABLED, intent.enabled)
                reduce { copy(predictiveBackEnabled = intent.enabled) }
            }
            AppIntent.RefreshModuleStatus -> reduce { copy(moduleStatus = moduleStatusProvider.current()) }
        }
    }
}
