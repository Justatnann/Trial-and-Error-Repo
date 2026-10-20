package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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



    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        Button(
            enabled = !state.isLoading,
            onClick = {
                if (!state.isLoading) {
                    viewmodel.startDiscovery()
                }
            }
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),

                    )

                state.success.isNotEmpty() -> Text("Restart")
                else -> Text("Start")

            }
        }
    }
}