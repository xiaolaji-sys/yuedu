@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    abstract fun bindBookSourceApi(bookSourceApiImpl: BookSourceApiImpl): BookSourceApi

    @Binds
    abstract fun bindChapterApi(chapterApiImpl: ChapterApiImpl): ChapterApi

    companion object {
        @Provides
        @Singleton
        fun provideLoggingInterceptor(): HttpLoggingInterceptor {
            return HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        }

        @Provides
        @Singleton
        fun provideCache(): Cache {
            val cacheDir = File(System.getProperty("java.io.tmpdir"), "okhttp_cache")
            return Cache(cacheDir, 10L * 1024 * 1024) // 10MB cache
        }
    }
}