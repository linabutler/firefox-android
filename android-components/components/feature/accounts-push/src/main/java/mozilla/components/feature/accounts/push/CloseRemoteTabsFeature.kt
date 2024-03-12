/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.accounts.push

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import mozilla.components.concept.sync.AccountEvent
import mozilla.components.concept.sync.AccountEventsObserver
import mozilla.components.concept.sync.Device
import mozilla.components.concept.sync.DeviceCommandIncoming
import mozilla.components.service.fxa.manager.FxaAccountManager

class CloseRemoteTabsFeature(
    accountManager: FxaAccountManager,
    owner: LifecycleOwner = ProcessLifecycleOwner.get(),
    autoPause: Boolean = false,
    onTabsClosed: (Device?, List<String>) -> Unit,
) {
    init {
        val observer = TabsClosedEventsObserver(onTabsClosed)

        // Observe the account for all account events, although we'll ignore
        // non send-tab command events.
        accountManager.registerForAccountEvents(observer, owner, autoPause)
    }
}

internal class TabsClosedEventsObserver(
    private val onTabsClosed: (Device?, List<String>) -> Unit,
) : AccountEventsObserver {
    override fun onEvents(events: List<AccountEvent>) {
        events.asSequence()
            .filterIsInstance<AccountEvent.DeviceCommandIncoming>()
            .map { it.command }
            .filterIsInstance<DeviceCommandIncoming.TabsClosed>()
            .forEach { command ->
                onTabsClosed(command.from, command.urls)
            }
    }
}