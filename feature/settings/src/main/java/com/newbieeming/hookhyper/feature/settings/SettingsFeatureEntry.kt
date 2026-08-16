package com.newbieeming.hookhyper.feature.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.newbieeming.hookhyper.core.model.FeatureMetadata
import com.newbieeming.hookhyper.core.ui.feature.FeatureEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SettingsFeatureEntry @Inject constructor(
    @param:ApplicationContext context: Context,
) : FeatureEntry {
    override val metadata = FeatureMetadata(
        id = ID,
        packageName = PACKAGE_NAME,
        fallbackName = context.getString(R.string.feature_settings_name),
        description = context.getString(R.string.feature_settings_description),
    )

    @Composable
    override fun Content(onBack: () -> Unit, modifier: Modifier) {
        SettingsFeatureScreen(onBack = onBack, modifier = modifier)
    }

    companion object {
        const val ID = "settings"
        const val PACKAGE_NAME = "com.android.settings"
    }
}
