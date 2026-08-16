package com.newbieeming.hookhyper.ui.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.newbieeming.hookhyper.R
import com.newbieeming.hookhyper.ui.component.AdaptiveText

@Composable
fun MissingFeatureScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().clickable(onClick = onBack),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AdaptiveText(stringResource(R.string.missing_feature))
    }
}
