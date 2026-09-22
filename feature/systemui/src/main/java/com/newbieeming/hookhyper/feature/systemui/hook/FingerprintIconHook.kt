package com.newbieeming.hookhyper.feature.systemui.hook

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.hook.HookContext
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.core.ui.component.SettingsPreferenceGroup
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.model.FingerprintIconStyle
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef
import dalvik.system.BaseDexClassLoader
import java.io.File

/**
 * 替换屏下指纹图标。
 *
 * LSPosed / libxposed API 102 不再支持宿主资源注入，因此不再改写资源 ID，
 * 而是在 [MiuiGxzwFrameAnimation.decodeBitmap] 解码位图时直接换成模块内图标。
 */
@HookModule(packageName = SystemUiFeatureEntry.PACKAGE_NAME)
@SuppressLint("DiscouragedPrivateApi")
class FingerprintIconHook :
    SubHooker,
    FeatureHook<SystemUiHookDef> {

    override val def = SystemUiHookDef.REPLACE_FINGERPRINT_ICON

    private var appContext: Context? = null
    private var moduleResources: Resources? = null
    private val bitmapCache = HashMap<Int, Bitmap?>()

    @Composable
    override fun Content() {
        val repository = LocalPreferencesRepository.current
        var enabled by remember { mutableStateOf(repository.getBoolean(preferenceKey)) }
        var style by remember {
            mutableStateOf(FingerprintIconStyle.fromId(repository.getString(FingerprintIconStyle.PREFERENCE_KEY)))
        }
        SettingsPreferenceGroup(index = 0, count = 2) {
            HookSwitchPreference(
                preferenceKey = preferenceKey,
                title = stringResource(R.string.systemui_replace_fingerprint_icon_title),
                summary = stringResource(R.string.systemui_replace_fingerprint_icon_summary),
                onCheckedChange = { enabled = it },
            )
            AnimatedVisibility(
                visible = enabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                FingerprintStyleSelector(
                    selected = style,
                    onSelect = {
                        style = it
                        repository.putString(FingerprintIconStyle.PREFERENCE_KEY, it.id)
                    },
                )
            }
        }
    }

    override fun HookContext.onHook() {
        val preferences = prefs(PreferenceKeys.FILE_NAME)
        val style =
            FingerprintIconStyle.fromId(preferences.getString(FingerprintIconStyle.PREFERENCE_KEY))

        hookSafely("Application.attach") {
            val attach = Application::class.java.getDeclaredMethod("attach", Context::class.java)
            xposed.hook(attach).intercept { chain ->
                val context = chain.args[0] as Context
                if (context.packageName == packageName) bindContext(context)
                chain.proceed()
            }
        }

        hookSafely("MiuiGxzwFrameAnimation.decodeBitmap") {
            val method = FRAME_ANIMATION_CLASS.toClass().resolve()
                .firstMethod {
                    name = DECODE_BITMAP_METHOD
                    parameterCount = 2
                }.self
            xposed.hook(method).intercept { chain ->
                val original = chain.proceed() as? Bitmap
                val resId = (chain.args[0] as? Number)?.toInt()
                if (resId == null) {
                    original
                } else {
                    decodeReplacement(style, resId) ?: original
                }
            }
        }
    }

    private fun bindContext(context: Context) {
        appContext = context.applicationContext ?: context
        moduleResources = resolveModuleResources(appContext ?: context)
    }

    private fun decodeReplacement(style: FingerprintIconStyle, hostResId: Int): Bitmap? {
        ensureBound()
        bitmapCache[hostResId]?.let { return it }
        val hostRes = appContext?.resources ?: return null
        val resName =
            runCatching { hostRes.getResourceEntryName(hostResId) }.getOrNull() ?: return null
        val moduleResId = style.replacementFor(resName) ?: return null
        val moduleRes = moduleResources ?: return null
        val bitmap = runCatching {
            BitmapFactory.decodeResource(moduleRes, moduleResId)
        }.getOrNull()
        if (bitmap != null) {
            Log.d(TAG, "Replaced $resName with ${style.id}")
        }
        bitmapCache[hostResId] = bitmap
        return bitmap
    }

    private fun ensureBound() {
        if (appContext != null && moduleResources != null) return
        val context = currentApplication() ?: return
        bindContext(context)
    }

    private fun resolveModuleResources(context: Context): Resources? = runCatching {
        context.createPackageContext(MODULE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY).resources
    }.recoverCatching {
        context.packageManager.getResourcesForApplication(MODULE_PACKAGE)
    }.recoverCatching {
        // LSPosed 加载的模块 ClassLoader 可能没有 APK 路径，仅作兜底。
        val apkPath = moduleApkPath() ?: return@recoverCatching null
        resourcesFromApkPath(context, apkPath)
    }.getOrNull()

    private fun resourcesFromApkPath(context: Context, apkPath: String): Resources? = runCatching {
        val assets = AssetManager::class.java.getDeclaredConstructor().newInstance()
        val addAssetPath = AssetManager::class.java
            .getDeclaredMethod("addAssetPath", String::class.java)
        val cookie = addAssetPath.invoke(assets, apkPath) as? Int ?: 0
        if (cookie == 0) return null
        Resources(assets, context.resources.displayMetrics, context.resources.configuration)
    }.getOrNull()

    private fun moduleApkPath(): String? = runCatching {
        val classLoader = FingerprintIconHook::class.java.classLoader as? BaseDexClassLoader
            ?: return null
        val pathList = BaseDexClassLoader::class.java
            .getDeclaredField("pathList")
            .apply { isAccessible = true }
            .get(classLoader)
        val elements = pathList.javaClass
            .getDeclaredField("dexElements")
            .apply { isAccessible = true }
            .get(pathList) as? Array<*> ?: return null
        elements.firstNotNullOfOrNull { element ->
            if (element == null) return@firstNotNullOfOrNull null
            runCatching {
                val path = element.javaClass
                    .getDeclaredField("path")
                    .apply { isAccessible = true }
                    .get(element) as? File
                path?.absolutePath?.takeIf { it.endsWith(".apk") }
            }.getOrNull()
        }
    }.getOrNull()

    @SuppressLint("PrivateApi")
    private fun currentApplication(): Context? = runCatching {
        Class.forName("android.app.ActivityThread")
            .getMethod("currentApplication")
            .invoke(null) as? Context
    }.getOrNull()

    private companion object {
        private const val TAG = "FingerprintIconHook"
        private const val MODULE_PACKAGE = "com.newbieeming.hookhyper"
        private const val FRAME_ANIMATION_CLASS =
            "com.miui.keyguard.biometrics.fod.MiuiGxzwFrameAnimation"
        private const val DECODE_BITMAP_METHOD = "decodeBitmap"
    }
}

@Composable
private fun FingerprintStyleSelector(
    selected: FingerprintIconStyle,
    onSelect: (FingerprintIconStyle) -> Unit,
) {
    val darkTheme = isSystemInDarkTheme()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.systemui_fingerprint_style_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FingerprintStyleOption(
                    style = FingerprintIconStyle.CIRCLE,
                    selected = selected == FingerprintIconStyle.CIRCLE,
                    darkTheme = darkTheme,
                    onClick = { onSelect(FingerprintIconStyle.CIRCLE) },
                )
                FingerprintStyleOption(
                    style = FingerprintIconStyle.VANILLA,
                    selected = selected == FingerprintIconStyle.VANILLA,
                    darkTheme = darkTheme,
                    onClick = { onSelect(FingerprintIconStyle.VANILLA) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.FingerprintStyleOption(
    style: FingerprintIconStyle,
    selected: Boolean,
    darkTheme: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colorScheme.primaryContainer else colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                painter = painterResource(if (darkTheme) style.normalResId else style.lightResId),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = stringResource(style.labelResId()),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

private fun FingerprintIconStyle.labelResId(): Int = when (this) {
    FingerprintIconStyle.CIRCLE -> R.string.systemui_fingerprint_style_circle
    FingerprintIconStyle.VANILLA -> R.string.systemui_fingerprint_style_vanilla
}
