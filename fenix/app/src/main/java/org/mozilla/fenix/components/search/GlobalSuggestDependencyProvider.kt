/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components.search

import mozilla.appservices.suggest.SuggestStore

object GlobalSuggestDependencyProvider {
    const val DEFAULT_SUGGEST_DATABASE_NAME = "suggest.sqlite"

    internal var suggestStore: SuggestStore? = null

    fun initializeSuggestStore(databasePath: String) {
        this.suggestStore = SuggestStore(databasePath)
    }

    fun requireSuggestStore(): SuggestStore {
        return requireNotNull(suggestStore) {
            "`GlobalSuggestDependencyProvider.initializeSuggestStore` must be called before accessing `suggestStore`"
        }
    }
}
