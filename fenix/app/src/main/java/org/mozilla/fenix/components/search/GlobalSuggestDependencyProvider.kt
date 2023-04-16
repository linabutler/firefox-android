/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components.search

import mozilla.appservices.suggest.SuggestionProvider

object GlobalSuggestDependencyProvider {
    const val DEFAULT_SUGGESTION_PROVIDER_DATABASE_NAME = "suggest.sqlite"

    internal var suggestionProvider: SuggestionProvider? = null

    fun initializeSuggestionProvider(databasePath: String) {
        this.suggestionProvider = SuggestionProvider(databasePath)
    }

    fun requireSuggestionProvider(): SuggestionProvider {
        return requireNotNull(suggestionProvider) {
            "`GlobalSuggestDependencyProvider.initialize` must be called before accessing `suggestionProvider`"
        }
    }
}
