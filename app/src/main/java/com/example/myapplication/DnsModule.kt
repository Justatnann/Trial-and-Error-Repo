package com.example.myapplication

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DnsModule {
    @Provides
    @Singleton
    fun createBackendManager(@ApplicationContext context: Context): BackendDiscoveryManager {
        return BackendDiscoveryManager(context)
    }
}