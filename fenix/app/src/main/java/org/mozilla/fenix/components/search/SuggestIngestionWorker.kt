/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components.search

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mozilla.appservices.suggest.IngestLimits
import mozilla.appservices.suggest.SuggestApiException
import mozilla.components.support.base.log.logger.Logger

internal class SuggestIngestionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val logger = Logger("SuggestIngestionWorker")

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val provider = GlobalSuggestDependencyProvider.requireSuggestionProvider()
            try {
                logger.info("Ingesting new Firefox Suggest suggestions")
                provider.ingest(IngestLimits(records = null))
                Result.success()
            } catch (suggestError: SuggestApiException) {
                logger.error("Failed to ingest new Firefox Suggest suggestions", suggestError)
                Result.retry()
            }
        }
    }

    internal companion object {
        const val WORK_TAG = "org.mozilla.fenix.components.search.suggest.ingest.work.tag"
    }
}
