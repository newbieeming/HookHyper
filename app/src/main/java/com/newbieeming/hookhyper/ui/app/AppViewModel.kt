package com.newbieeming.hookhyper.ui.app

import com.newbieeming.hookhyper.core.data.HookPreferencesRepository
import com.newbieeming.hookhyper.core.data.ModuleStatusProvider
import com.newbieeming.hookhyper.core.model.FeatureMetadata
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
        moduleStatus = moduleStatusProvider.current(),
        features = featureSet.map(FeatureEntry::metadata).sortedBy(FeatureMetadata::fallbackName),
    ),
) {
    private val featureEntries = featureSet.associateBy { it.metadata.id }

    fun feature(id: String): FeatureEntry? = featureEntries[id]

    override fun onIntent(intent: AppIntent) {
        when (intent) {
            is AppIntent.SelectUiStyle -> {
                preferences.setUiStyle(intent.style)
                reduce { copy(uiStyle = intent.style) }
                sendEffect(AppEffect.UiStyleChanged)
            }
            AppIntent.RefreshModuleStatus -> reduce { copy(moduleStatus = moduleStatusProvider.current()) }
        }
    }
}
