/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.fxsuggest

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mozilla.appservices.suggest.SuggestIngestionConstraints
import mozilla.appservices.suggest.SuggestApiException
import mozilla.components.support.base.log.logger.Logger

/**
 * A [CoroutineWorker] that downloads and persists new Firefox Suggest search suggestions.
 *
 * @param context The Android application context.
 * @param params Parameters for this worker's internal state.
 */
internal class FxSuggestIngestionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val logger = Logger("FxSuggestIngestionWorker")

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val store = GlobalFxSuggestDependencyProvider.requireSuggestStore()
            try {
                logger.info("Ingesting new Firefox Suggest suggestions")
                store.ingest(SuggestIngestionConstraints())
                Result.success()
            } catch (suggestError: SuggestApiException) {
                logger.error("Failed to ingest new Firefox Suggest suggestions", suggestError)
                Result.retry()
            }
        }
    }

    internal companion object {
        const val WORK_TAG = "mozilla.components.feature.fxsuggest.ingest.work.tag"
    }
}
