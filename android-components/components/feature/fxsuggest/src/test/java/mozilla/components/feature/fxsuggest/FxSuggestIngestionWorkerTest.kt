/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.fxsuggest

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.await
import androidx.work.testing.TestListenableWorkerBuilder
import java.io.IOException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import mozilla.appservices.suggest.SuggestApiException
import mozilla.appservices.suggest.SuggestStore
import mozilla.components.support.test.any
import mozilla.components.support.test.mock
import mozilla.components.support.test.robolectric.testContext
import mozilla.components.support.test.whenever
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class FxSuggestIngestionWorkerTest {
    private lateinit var suggestStore: SuggestStore

    @Before
    fun setUp() {
        suggestStore = mock()
        GlobalFxSuggestDependencyProvider.suggestStore = suggestStore
    }

    @After
    fun tearDown() {
        GlobalFxSuggestDependencyProvider.suggestStore = null
    }

    @Test
    fun workSucceeds() = runTest {
        doNothing().`when`(suggestStore).ingest(any())

        val worker = TestListenableWorkerBuilder<FxSuggestIngestionWorker>(testContext).build()

        val result = worker.startWork().await()

        verify(suggestStore).ingest(any())
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun workShouldRetry() = runTest {
        whenever(suggestStore.ingest(any())).then {
            throw SuggestApiException.Other("Mercury in retrograde")
        }

        val worker = TestListenableWorkerBuilder<FxSuggestIngestionWorker>(testContext).build()

        val result = worker.startWork().await()

        verify(suggestStore).ingest(any())
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun workShouldRethrow() {
        whenever(suggestStore.ingest(any())).then {
            throw IOException()
        }

        val worker = TestListenableWorkerBuilder<FxSuggestIngestionWorker>(testContext).build()

        assertThrows(IOException::class.java) {
            runTest {
                worker.startWork().await()
            }
        }
        verify(suggestStore).ingest(any())
    }
}