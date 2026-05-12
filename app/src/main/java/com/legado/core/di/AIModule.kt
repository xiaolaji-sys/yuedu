package com.legado.core.di

import com.legado.ai.DirectoryGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideDirectoryGenerator(): DirectoryGenerator {
        return DirectoryGenerator()
    }
}