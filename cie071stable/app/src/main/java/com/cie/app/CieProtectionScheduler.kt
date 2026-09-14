package com.cie.app

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Keeps the local company/identity caches fresh without requiring the UI process
 * to stay alive. Call screening itself is still invoked by Android through
 * CallScreeningService when CIE holds ROLE_CALL_SCREENING.
 */
class CieProtectionSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val repo = StableRepository(applicationContext)
        val identities = IdentityNetworkStore(applicationContext)

        val companySync = runCatching { repo.sync() }.isSuccess
        val identitySync = runCatching { identities.sync() }.isSuccess

        return if (companySync || identitySync) Result.success() else Result.retry()
    }
}

object CieProtectionScheduler {
    private const val PERIODIC_NAME = "cie-protection-periodic-sync"
    private const val IMMEDIATE_NAME = "cie-protection-immediate-sync"

    fun ensure(context: Context, requestImmediate: Boolean = false) {
        val network = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodic = PeriodicWorkRequestBuilder<CieProtectionSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(network)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodic
        )

        if (requestImmediate) {
            val immediate = OneTimeWorkRequestBuilder<CieProtectionSyncWorker>()
                .setConstraints(network)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_NAME,
                ExistingWorkPolicy.REPLACE,
                immediate
            )
        }
    }
}

class CieApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CieProtectionScheduler.ensure(this, requestImmediate = true)
    }
}

class CieBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            CieProtectionScheduler.ensure(context, requestImmediate = true)
        }
    }
}
