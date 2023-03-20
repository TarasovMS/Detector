package com.example.detector.di.detector

import com.example.detector.data.repository.DetectorRepositoryImpl
import com.example.detector.domain.DetectorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@[Module InstallIn(SingletonComponent::class)]
interface DetectorRepositoryModule {

    @Binds
    fun bindDetectorRepository(repository: DetectorRepositoryImpl): DetectorRepository

}
