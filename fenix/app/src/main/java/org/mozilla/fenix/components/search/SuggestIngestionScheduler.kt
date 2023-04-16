/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components.search

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.base.worker.Frequency
import java.util.concurrent.TimeUnit

class SuggestIngestionScheduler(
    private val context: Context,
    private val frequency: Frequency = Frequency(repeatInterval = 1, repeatIntervalTimeUnit = TimeUnit.DAYS)
) {
    private val logger = Logger("SuggestIngestionScheduler")

    fun schedulePeriodicIngestion() {
        logger.info("Scheduling periodic ingestion for new Firefox Suggest suggestions")
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SuggestIngestionWorker.WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            createPeriodicIngestionWorkerRequest()
        )
    }

    fun stopPeriodicIngestion() {
        logger.info("Canceling periodic ingestion for new Firefox Suggest suggestions")
        WorkManager.getInstance(context).cancelAllWorkByTag(SuggestIngestionWorker.WORK_TAG)
    }

    internal fun createPeriodicIngestionWorkerRequest(): PeriodicWorkRequest {
        val constraints = getWorkerConstrains()
        return PeriodicWorkRequestBuilder<SuggestIngestionWorker>(
            this.frequency.repeatInterval,
            this.frequency.repeatIntervalTimeUnit,
        ).apply {
            setConstraints(constraints)
            addTag(SuggestIngestionWorker.WORK_TAG)
        }.build()
    }

    internal fun getWorkerConstrains() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresBatteryNotLow(true)
        .setRequiresStorageNotLow(true)
        .build()
}
