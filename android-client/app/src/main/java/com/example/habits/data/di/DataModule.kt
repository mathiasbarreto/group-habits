package com.example.habits.data.di

import android.util.Log
import com.example.habits.data.di.interceptor.HeaderInterceptor
import com.example.habits.data.repository.LoginRepository
import com.example.habits.data.repository.LoginRepositoryImpl
import com.example.habits.data.services.UserService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object DataModule {

    private const val BASE_URL =
        "https://group-habits.herokuapp.com/api/v1/"
    private const val OK_HTTP = "Ok Http"

    fun load() {
        loadKoinModules(postsModule() + networkModule())
    }

    private fun postsModule(): Module {
        return module {
            single<LoginRepository> { LoginRepositoryImpl(service = get()) }
        }
    }

    private fun networkModule(): Module {
        return module {

            single {
                createOkHttpClient()
            }

            single {
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            }

            single {
                createService<UserService>(get(), get())
            }
        }
    }

    private fun createOkHttpClient(): OkHttpClient {

        val interceptor = HttpLoggingInterceptor {
            Log.e(OK_HTTP, it)
        }
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(interceptor)
            .build()
    }

    private inline fun <reified T> createService(
        client: OkHttpClient,
        factory: Moshi,
    ): T {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(factory))
            .build()
            .create(T::class.java)
    }
}
