package com.bookshare.app.di

import android.content.Context
import androidx.room.Room
import com.bookshare.app.data.local.BookShareDatabase
import com.bookshare.app.data.local.dao.BookDao
import com.bookshare.app.data.local.dao.UserDao
import com.bookshare.app.data.local.dao.LoanRequestDao
import com.bookshare.app.data.remote.api.OpenLibraryApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BookShareDatabase =
        Room.databaseBuilder(context, BookShareDatabase::class.java, "bookshare_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideBookDao(db: BookShareDatabase): BookDao = db.bookDao()

    @Provides
    fun provideUserDao(db: BookShareDatabase): UserDao = db.userDao()

    @Provides
    fun provideLoanRequestDao(db: BookShareDatabase): LoanRequestDao = db.loanRequestDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://openlibrary.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    @Provides
    @Singleton
    fun provideOpenLibraryApi(retrofit: Retrofit): OpenLibraryApi =
        retrofit.create(OpenLibraryApi::class.java)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}
