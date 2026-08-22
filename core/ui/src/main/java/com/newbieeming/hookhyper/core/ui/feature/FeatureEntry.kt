package com.newbieeming.hookhyper.core.ui.feature

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.newbieeming.hookhyper.core.common.FeatureMetadata

interface FeatureEntry {
    val metadata: FeatureMetadata

    @Composable
    fun Content(
        onBack: () -> Unit,
        modifier: Modifier,
    )
}
