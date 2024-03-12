/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.accounts.push

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.plus
import mozilla.components.concept.sync.Device
import mozilla.components.concept.sync.DeviceCapability
import mozilla.components.concept.sync.DeviceCommandOutgoing
import mozilla.components.concept.sync.DeviceConstellation
import mozilla.components.service.fxa.manager.FxaAccountManager
import kotlin.coroutines.CoroutineContext

class CloseRemoteTabsUseCase(
    private val accountManager: FxaAccountManager,
    coroutineContext: CoroutineContext = Dispatchers.IO,
) {
    private var job: Job = SupervisorJob()
    private val scope = CoroutineScope(coroutineContext) + job

    operator fun invoke(deviceId: String, url: String) =
        scope.async { send(deviceId, url) }

    private suspend fun send(deviceId: String, url: String): Boolean {
        filterCloseRemoteTabDevices(accountManager) { constellation, devices ->
            val device = devices.firstOrNull {
                it.id == deviceId
            }
            device?.let {
                return constellation.sendCommandToDevice(
                    device.id,
                    DeviceCommandOutgoing.CloseRemoteTabs(listOf(url)),
                )
            }
        }

        return false
    }
}

@VisibleForTesting
internal inline fun filterCloseRemoteTabDevices(
    accountManager: FxaAccountManager,
    block: (DeviceConstellation, Collection<Device>) -> Unit,
) {
    val constellation = accountManager.authenticatedAccount()?.deviceConstellation() ?: return

    constellation.state()?.let { state ->
        state.otherDevices.filter {
            it.capabilities.contains(DeviceCapability.CLOSE_REMOTE_TABS)
        }.let { devices ->
            block(constellation, devices)
        }
    }
}
