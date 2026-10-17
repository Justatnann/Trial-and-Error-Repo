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

    var onBackendFound: ((String) -> Unit)? = null
    var onDiscoveryTimeout: (() -> Unit)? = null
    var isDiscovering = false


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
                Log.d("ResolveListener", "Host Address: ${it.host.hostAddress}")
                Log.d("ResolveListener", "Host Port: ${it.port}")
                baseUrl = "https://${it.host.hostAddress}:${it.port}"
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
            isDiscovering = true
        }

        override fun onDiscoveryStopped(serviceType: String?) {
            Log.d("BackendDiscovery", "Discovery Stopped: $serviceType")
            isDiscovering = false
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
            Log.d("BackendDiscovery", "Found services: $serviceInfo")
            serviceInfo?.let { nsdManager.resolveService(it, resolveListener) }

        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
            Log.d("BackendDiscovery", "Discovered Service Lost: $serviceInfo")

        }

        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.d("BackendDiscovery", "Starting Discovery Failed: $errorCode | $serviceType")
            isDiscovering = false
        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.d("BackendDiscovery", "Stopping Discovery Failed: $errorCode | $serviceType")
            isDiscovering = true
        }

    }


    fun startDiscovery() {
        if (!isDiscovering) {
            backendFound = false
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
            timeoutJob = scope.launch {
                delay(10_000L.milliseconds)
                if (!backendFound) {

                    stopDiscovery()
                    onDiscoveryTimeout?.invoke()
                }
            }
        }
    }

    fun stopDiscovery() {
        if (isDiscovering) {
            nsdManager.stopServiceDiscovery(listener)
        }
    }

}

