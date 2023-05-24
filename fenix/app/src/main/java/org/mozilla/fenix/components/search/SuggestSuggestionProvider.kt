/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components.search

import android.content.Context
import android.graphics.BitmapFactory
import java.util.UUID
import mozilla.appservices.suggest.Suggestion
import mozilla.components.concept.awesomebar.AwesomeBar
import mozilla.components.feature.session.SessionUseCases
import org.mozilla.fenix.R
import org.mozilla.fenix.ext.settings

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
        return this.mapNotNull { result ->
            if ((result.isSponsored && !context.settings().shouldShowSponsoredSuggestions) ||
                    (!result.isSponsored && !context.settings().shouldShowNonSponsoredSuggestions)) {
                null
            } else {
                AwesomeBar.Suggestion(
                    provider = this@SuggestSuggestionProvider,
                    icon = result.icon?.let {
                        val byteArray = it.toUByteArray().asByteArray()
                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    },
                    title = "${result.fullKeyword} — ${result.title}",
                    description = if (result.isSponsored) context.getString(R.string.sponsored_suggestion_description) else null,
                    onSuggestionClicked = {
                        loadUrlUseCase.invoke(result.url)
                    }
                )
            }
        }
    }
}
