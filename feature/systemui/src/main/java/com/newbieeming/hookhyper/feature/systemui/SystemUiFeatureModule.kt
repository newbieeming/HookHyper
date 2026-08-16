package com.newbieeming.hookhyper.feature.systemui

import com.newbieeming.hookhyper.core.ui.feature.FeatureEntry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class SystemUiFeatureModule {
    @Binds
    @IntoSet
    abstract fun bindFeatureEntry(entry: SystemUiFeatureEntry): FeatureEntry
}
