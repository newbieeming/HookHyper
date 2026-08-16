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

        val rootCheck = runAsRoot("id")
        if (rootCheck !is RootCommandResult.Success || !rootCheck.output.contains("uid=0")) {
            return@withContext RestartAppResult.RootRequired
        }

        val command = if (packageName == SYSTEM_UI_PACKAGE) {
            "killall $packageName"
        } else {
            "am force-stop $packageName; sleep 1; " +
                "monkey -p $packageName -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1"
        }

        when (val result = runAsRoot(command)) {
            is RootCommandResult.Success -> RestartAppResult.Success
            is RootCommandResult.Error -> RestartAppResult.Failure(result.reason)
        }
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
