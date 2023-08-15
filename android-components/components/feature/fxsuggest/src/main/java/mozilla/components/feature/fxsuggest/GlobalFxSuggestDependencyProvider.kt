/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.fxsuggest

import mozilla.appservices.suggest.SuggestStore

/**
 * Provides global access to the dependencies needed to access Firefox Suggest search suggestions.
 */
object GlobalFxSuggestDependencyProvider {
    /**
     * The default Suggest database file name.
     *
     * Your application should pass this constant to [android.content.Context.getDatabasePath],
     * then pass the resulting absolute path to
     * [GlobalFxSuggestDependencyProvider.openSuggestStore].
     */
    const val DEFAULT_SUGGEST_DATABASE_NAME = "suggest.sqlite"

    internal var suggestStore: SuggestStore? = null

    /**
     * Opens a Suggest store that persists suggestions, icons, and metadata in a database at the
     * given path.
     *
     * Your application's [onCreate][android.app.Application.onCreate] method should call this
     * method once.
     *
     * @param databasePath The absolute path of the Suggest database.
     */
    fun openSuggestStore(databasePath: String) {
        this.initializeSuggestStore(SuggestStore(databasePath))
    }

    private fun initializeSuggestStore(suggestStore: SuggestStore) {
        this.suggestStore = suggestStore
    }

    internal fun requireSuggestStore(): SuggestStore {
        return requireNotNull(suggestStore) {
            "`GlobalSuggestDependencyProvider.openSuggestStore` must be called before accessing `suggestStore`"
        }
    }
}
