/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.fxsuggest

import android.content.res.Resources
import java.util.UUID
import mozilla.appservices.suggest.Suggestion
import mozilla.appservices.suggest.SuggestionQuery
import mozilla.components.concept.awesomebar.AwesomeBar
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.support.ktx.kotlin.toBitmap

/**
 * An [AwesomeBar.SuggestionProvider] that returns Firefox Suggest search suggestions.
 */
class FxSuggestSuggestionProvider(
    private val resources: Resources,
    private val loadUrlUseCase: SessionUseCases.LoadUrlUseCase,
    private val includeSponsoredSuggestions: Boolean,
    private val includeNonSponsoredSuggestions: Boolean,
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

        val suggestions = GlobalFxSuggestDependencyProvider.requireSuggestStore().query(SuggestionQuery(
            keyword = text,
            includeSponsored = includeSponsoredSuggestions,
            includeNonSponsored = includeNonSponsoredSuggestions,
        ))
        return suggestions.into()
    }

    override fun onInputCancelled() {
        GlobalFxSuggestDependencyProvider.requireSuggestStore().interrupt()
    }

    private suspend fun List<Suggestion>.into(): List<AwesomeBar.Suggestion> {
        return this.map { suggestion ->
            val details = when (suggestion) {
                is Suggestion.Amp -> SuggestionDetails(
                    title = suggestion.title,
                    url = suggestion.url,
                    fullKeyword = suggestion.fullKeyword,
                    isSponsored = true,
                    icon = suggestion.icon,
                )
                is Suggestion.Wikipedia -> SuggestionDetails(
                    title = suggestion.title,
                    url = suggestion.url,
                    fullKeyword = suggestion.fullKeyword,
                    isSponsored = false,
                    icon = suggestion.icon,
                )
            }
            AwesomeBar.Suggestion(
                provider = this@FxSuggestSuggestionProvider,
                icon = details.icon?.let {
                    it.toUByteArray().asByteArray().toBitmap()
                },
                title = "${details.fullKeyword} — ${details.title}",
                description = if (details.isSponsored) {
                    resources.getString(R.string.sponsored_suggestion_description)
                } else {
                    null
                },
                onSuggestionClicked = {
                    loadUrlUseCase.invoke(details.url)
                }
            )
        }
    }
}

internal data class SuggestionDetails(
    val title: String,
    val url: String,
    val fullKeyword: String,
    val isSponsored: Boolean,
    val icon: List<UByte>?,
)
