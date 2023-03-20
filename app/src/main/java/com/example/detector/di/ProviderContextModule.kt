package com.example.detector.di

import com.example.detector.common.contextProvider.ResourceProviderContext
import com.example.detector.common.contextProvider.ResourceProviderContextImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@[Module InstallIn(SingletonComponent::class)]
interface ProviderContextModule {

    @Binds
    fun bindResourceProviderContext(
        ResourceProviderContext: ResourceProviderContextImpl,
    ): ResourceProviderContext

}
