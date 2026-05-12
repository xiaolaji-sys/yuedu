package com.legado.core.di

import com.legado.data.network.api.BookSourceApi
import com.legado.data.network.api.impl.BookSourceApiImpl
import com.legado.data.repository.BookRepository
import com.legado.data.source.BookSourceManager
import com.legado.data.source.rules.RuleEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BookSourceModule {

    @Provides
    @Singleton
    fun provideBookSourceManager(
        bookSourceApi: BookSourceApi,
        ruleEngine: RuleEngine
    ): BookSourceManager {
        return BookSourceManager(ruleEngine)
    }

    @Provides
    @Singleton
    fun provideRuleEngine(): RuleEngine {
        return RuleEngine()
    }

    @Provides
    @Singleton
    fun provideBookSourceApi(): BookSourceApi {
        return BookSourceApiImpl()
    }
}