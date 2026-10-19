package com.example.myapplication

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

interface DnsRepository {
    fun getDns(): Flow<DnsResult>
}

class DnsRepoImpl @Inject constructor(private val dnsManager: BackendDiscoveryManager) :
    DnsRepository {
    override fun getDns() = callbackFlow {

        dnsManager.onBackendFound = { url ->
            trySend(DnsResult.Found(url))
            close()
        }

        dnsManager.onDiscoveryTimeout = {
            trySend(DnsResult.TimedOut)
            close()
        }

        dnsManager.startDiscovery()
        awaitClose {
            dnsManager.onBackendFound = null
            dnsManager.onDiscoveryTimeout = null
            dnsManager.stopDiscovery()
        }
    }

}

sealed interface DnsResult {
    data class Found(val url: String) : DnsResult
    data object TimedOut : DnsResult
}