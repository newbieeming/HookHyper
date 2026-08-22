package com.newbieeming.hookhyper.core.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface RestartAppResult {
    data object Success : RestartAppResult
    data object RootRequired : RestartAppResult
    data class Failure(val reason: String) : RestartAppResult
}

@Singleton
class RootAppRestarter @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    suspend fun restart(packageName: String): RestartAppResult = withContext(Dispatchers.IO) {
        if (!PACKAGE_NAME.matches(packageName)) {
            return@withContext RestartAppResult.Failure(context.getString(R.string.restart_invalid_package))
        }

        if (!checkRootWithRetry()) {
            return@withContext RestartAppResult.RootRequired
        }

        val command = if (packageName == SYSTEM_UI_PACKAGE) {
            "killall $packageName"
        } else {
            val component = context.packageManager
                .getLaunchIntentForPackage(packageName)?.component?.flattenToString()
                ?: return@withContext RestartAppResult.Failure(
                    context.getString(R.string.restart_invalid_package)
                )
            "am force-stop $packageName; sleep 1; am start -n $component"
        }

        when (val result = runAsRoot(command)) {
            is RootCommandResult.Success -> RestartAppResult.Success
            is RootCommandResult.Error -> RestartAppResult.Failure(result.reason)
        }
    }

    /**
     * 检测 root 权限，支持重试。
     * 首次调用 su 时如果 Magisk 弹出授权弹窗，su 会阻塞等待用户操作；
     * 用户授权后 su 可能已超时退出，此时重试即可成功。
     */
    private fun checkRootWithRetry(maxRetries: Int = 3): Boolean {
        repeat(maxRetries) {
            when (val result = runAsRoot("id")) {
                is RootCommandResult.Success -> {
                    if (result.output.contains("uid=0")) return true
                }
                is RootCommandResult.Error -> { /* 继续重试 */ }
            }
        }
        return false
    }

    private fun runAsRoot(command: String): RootCommandResult = try {
        val process = ProcessBuilder("su", "-c", command)
            .redirectErrorStream(true)
            .start()
        if (!process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroy()
            RootCommandResult.Error(context.getString(R.string.restart_command_timeout))
        } else {
            val output = process.inputStream.bufferedReader().use { it.readText().trim() }
            if (process.exitValue() == 0) {
                RootCommandResult.Success(output)
            } else {
                RootCommandResult.Error(output.ifBlank {
                    context.getString(R.string.restart_root_command_failed)
                })
            }
        }
    } catch (_: Exception) {
        RootCommandResult.Error(context.getString(R.string.restart_root_command_unavailable))
    }

    private sealed interface RootCommandResult {
        data class Success(val output: String) : RootCommandResult
        data class Error(val reason: String) : RootCommandResult
    }

    private companion object {
        val PACKAGE_NAME = Regex("[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z][A-Za-z0-9_]*)+")
        const val SYSTEM_UI_PACKAGE = "com.android.systemui"
        const val COMMAND_TIMEOUT_SECONDS = 30L
    }
}
