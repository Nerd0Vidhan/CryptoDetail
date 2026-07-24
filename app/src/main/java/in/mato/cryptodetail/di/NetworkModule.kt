package `in`.mato.cryptodetail.di

import `in`.mato.cryptodetail.BuildConfig
import `in`.mato.cryptodetail.data.remote.CryptoApiService
import `in`.mato.cryptodetail.data.repository.CryptoRepositoryImpl
import `in`.mato.cryptodetail.domain.repository.CryptoRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCryptoRepository(implementation: CryptoRepositoryImpl): CryptoRepository
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BaseUrl = BuildConfig.BASE_URL

    @Provides
    @Singleton
    fun provideAuthInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("x-cg-demo-api-key", BuildConfig.API_KEY)
            .build()
        chain.proceed(request)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BaseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideCryptoApiService(retrofit: Retrofit): CryptoApiService = retrofit.create(CryptoApiService::class.java)
}
