package com.darach.openlibrarybooks.core.common.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for checking network connectivity status.
 *
 * Provides both synchronous checks and reactive Flow-based connectivity monitoring.
 * Injected as a singleton via Hilt for consistent connectivity state across the app.
 */
@Singleton
class NetworkConnectivity @Inject constructor(@ApplicationContext private val context: Context) {

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Checks if the device currently has an active internet connection.
     *
     * @return true if connected to a network with internet capability, false otherwise
     */
    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Checks if the device is connected via WiFi.
     *
     * @return true if connected via WiFi, false otherwise
     */
    fun isConnectedViaWifi(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Checks if the device is connected via cellular/mobile data.
     *
     * @return true if connected via cellular, false otherwise
     */
    fun isConnectedViaCellular(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    /**
     * Returns a Flow that emits connectivity status changes.
     *
     * Emits true when connected, false when disconnected.
     * The Flow is hot and will continue emitting as network status changes.
     *
     * @return Flow<Boolean> representing connectivity status
     */
    fun observeConnectivity(): Flow<Boolean> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            private val networks = mutableSetOf<Network>()

            override fun onAvailable(network: Network) {
                networks.add(network)
                trySend(true)
            }

            override fun onLost(network: Network) {
                networks.remove(network)
                trySend(networks.isNotEmpty())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET,
                ) && networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED,
                )
                trySend(hasInternet && networks.isNotEmpty())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)

        // Send initial state
        trySend(isConnected())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged()
}
