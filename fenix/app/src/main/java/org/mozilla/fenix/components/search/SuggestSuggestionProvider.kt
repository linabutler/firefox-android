/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components.search

import android.content.Context
import java.util.UUID
import mozilla.appservices.suggest.Suggestion
import mozilla.components.concept.awesomebar.AwesomeBar
import mozilla.components.feature.session.SessionUseCases

class SuggestSuggestionProvider(
    private val context: Context,
    private val loadUrlUseCase: SessionUseCases.LoadUrlUseCase,
    private val suggestionsHeader: String? = null,
) : AwesomeBar.SuggestionProvider {
    override val id: String = UUID.randomUUID().toString()

    override fun groupTitle(): String? {
        return suggestionsHeader
    }

    override suspend fun onInputChanged(text: String): List<AwesomeBar.Suggestion> {
        if (text.isEmpty()) {
            return emptyList()
        }

        val suggestions = GlobalSuggestDependencyProvider.requireSuggestionProvider().query(text)
        return suggestions.into()
    }

    override fun onInputCancelled() {
        GlobalSuggestDependencyProvider.requireSuggestionProvider().interrupt()
    }

    private suspend fun List<Suggestion>.into(): List<AwesomeBar.Suggestion> {
        return this.map { result ->
            AwesomeBar.Suggestion(
                provider = this@SuggestSuggestionProvider,
                icon = null,
                title = "${result.fullKeyword} — ${result.title}",
                description = null,
                onSuggestionClicked = {
                    loadUrlUseCase.invoke(result.url)
                },
            )
        }
    }
}
