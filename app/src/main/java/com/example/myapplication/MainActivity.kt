package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.theme.MyApplicationTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Welcome(innerPadding = innerPadding)
                }
            }
        }
    }
}

@Composable
fun Welcome(innerPadding: PaddingValues) {
    val viewmodel: DnsViewmodel = hiltViewModel()
    val state by viewmodel.state.collectAsStateWithLifecycle()



    Column() {
        Text("Get Started")

        Spacer(Modifier.height(8.dp))
        Text(
            text = when {
                state.isLoading -> ""
                state.success.isNotEmpty() -> "Backend Founded: ${state.success}"
                state.isTimeout -> "Backend Not Found"
                else -> "Ready to Search"

            }
        )
        Spacer(Modifier.height(8.dp))

        Button(onClick = {
            viewmodel.startDiscovery()
        }) {
            Text("Start")
        }
    }
}