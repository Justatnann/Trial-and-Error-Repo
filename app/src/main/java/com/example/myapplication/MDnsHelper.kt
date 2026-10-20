package com.example.myapplication

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


class BackendDiscoveryManager(val context: Context) {

    val nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    private val serviceType = "_backend._tcp."

    private var baseUrl: String = ""
    private val scope = CoroutineScope(Dispatchers.Main)
    private var timeoutJob: Job? = null
    private var backendFound = false
    private var discoveryActive = false

    var onBackendFound: ((String) -> Unit)? = null
    var onDiscoveryTimeout: (() -> Unit)? = null


    val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(
            serviceInfo: NsdServiceInfo?,
            errorCode: Int
        ) {
            Log.e("ResolveListener", "Resolve Listener Failed: $serviceInfo | $errorCode")

        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
            Log.d("ResolveListener", "Resolve Listener Success: $serviceInfo")
            serviceInfo?.let {
                val host = it.host?.hostAddress ?: return
                Log.d("ResolveListener", "Host Address: ${host}")
                Log.d("ResolveListener", "Host Port: ${it.port}")
                baseUrl = "https://${host}:${it.port}"
                Log.d("ResolveListener", "Backend URL: $baseUrl")
                backendFound = true
                timeoutJob?.cancel()
                stopDiscovery()
                onBackendFound?.invoke(baseUrl)
            }

        }

    }
    val listener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(serviceType: String?) {
            Log.d("BackendDiscovery", "Starting Discovery: $serviceType")
        }

        override fun onDiscoveryStopped(serviceType: String?) {
            Log.d("BackendDiscovery", "Discovery Stopped: $serviceType")
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
            Log.d("BackendDiscovery", "APP: onServiceFound called")

            serviceInfo?.let {
                try {
                    Log.d("BackendDiscovery", "APP: Calling resolveService")

                    nsdManager.resolveService(it, resolveListener)
                    Log.d("BackendDiscovery", "APP: resolveService returned")
                } catch (e: Exception) {
                    Log.e("BackendDiscovery", "APP: resolveService threw an exception", e)
                }
            }
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
            Log.d("BackendDiscovery", "Discovered Service Lost: $serviceInfo")

        }

        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.d("BackendDiscovery", "Starting Discovery Failed: $errorCode | $serviceType")
            discoveryActive = false
            timeoutJob?.cancel()
            onDiscoveryTimeout?.invoke()
        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.d("BackendDiscovery", "Stopping Discovery Failed: $errorCode | $serviceType")
            discoveryActive = false
        }

    }


    fun startDiscovery() {
        if (discoveryActive) return
        discoveryActive = true
        backendFound = false
        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(30_000)
            if (!backendFound) {
                stopDiscovery(); onDiscoveryTimeout?.invoke()
            }
        }
    }

    // Why the old Stop triggered Error:
    //t=0   Backend resolved
    //      onServiceResolved calls stopDiscovery()
    //        isDiscovering is true -> nsdManager.stopServiceDiscovery(listener)   (stop requested)
    //
    //t=1   onBackendFound is invoked -> repo does trySend + close()
    //      close() triggers awaitClose { ... stopDiscovery() }
    //        isDiscovering is STILL true (onDiscoveryStopped hasn't arrived yet)
    //        -> nsdManager.stopServiceDiscovery(listener) AGAIN   (crash)
    //
    //t=2   NSD finally calls onDiscoveryStopped -> isDiscovering = false   (too late)

    fun stopDiscovery() {
        timeoutJob?.cancel()
        if (!discoveryActive) return
        discoveryActive = false
        try {
            nsdManager.stopServiceDiscovery(listener)
        } catch (e: IllegalArgumentException) {
        }
    }

}

