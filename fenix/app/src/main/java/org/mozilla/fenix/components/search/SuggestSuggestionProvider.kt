/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components.search

import android.content.Context
import android.graphics.BitmapFactory
import java.util.UUID
import mozilla.appservices.suggest.Suggestion
import mozilla.appservices.suggest.SuggestionQuery
import mozilla.components.concept.awesomebar.AwesomeBar
import mozilla.components.feature.session.SessionUseCases
import org.mozilla.fenix.R
import org.mozilla.fenix.ext.settings

data class SuggestSuggestionDetails(
    val title: String,
    val url: String,
    val fullKeyword: String,
    val isSponsored: Boolean,
    val icon: List<UByte>?,
)

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

        val suggestions = GlobalSuggestDependencyProvider.requireSuggestStore().query(SuggestionQuery(
            keyword = text,
            includeSponsored = context.settings().shouldShowSponsoredSuggestions,
            includeNonSponsored = context.settings().shouldShowNonSponsoredSuggestions,
        ))
        return suggestions.into()
    }

    override fun onInputCancelled() {
        GlobalSuggestDependencyProvider.requireSuggestStore().interrupt()
    }

    private suspend fun List<Suggestion>.into(): List<AwesomeBar.Suggestion> {
        return this.map { suggestion ->
            val details = when (suggestion) {
                is Suggestion.Amp -> SuggestSuggestionDetails(
                    title = suggestion.title,
                    url = suggestion.url,
                    fullKeyword = suggestion.fullKeyword,
                    isSponsored = true,
                    icon = suggestion.icon,
                )
                is Suggestion.Wikipedia -> SuggestSuggestionDetails(
                    title = suggestion.title,
                    url = suggestion.url,
                    fullKeyword = suggestion.fullKeyword,
                    isSponsored = false,
                    icon = suggestion.icon,
                )
            }
            AwesomeBar.Suggestion(
                provider = this@SuggestSuggestionProvider,
                icon = details.icon?.let {
                    val byteArray = it.toUByteArray().asByteArray()
                    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                },
                title = "${details.fullKeyword} — ${details.title}",
                description = if (details.isSponsored) context.getString(R.string.sponsored_suggestion_description) else null,
                onSuggestionClicked = {
                    loadUrlUseCase.invoke(details.url)
                }
            )
        }
    }
}
