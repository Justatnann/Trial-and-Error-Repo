package com.example.myapplication

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackendManagerDI {
    @Provides
    @Singleton
    fun createBackendManager(@ApplicationContext context: Context): BackendDiscoveryManager {
        return BackendDiscoveryManager(context)
    }

}

@Module
@InstallIn(SingletonComponent::class)
abstract class DnsRepoModule {
    @Binds
    abstract fun bindDnsRepo(
        impl: DnsRepoImpl
    ): DnsRepository

}