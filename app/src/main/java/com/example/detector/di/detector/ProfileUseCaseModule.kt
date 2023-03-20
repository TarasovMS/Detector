package com.example.detector.di.detector

import com.example.detector.domain.DetectorRepository
import com.example.detector.domain.DetectorUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@[Module InstallIn(SingletonComponent::class)]
class ProfileUseCaseModule {

    @Provides
    fun provideProfileUseCase(
        detectorRepository: DetectorRepository,
    ) = DetectorUseCase(
        detectorRepository = detectorRepository,
    )
}
