package com.activaplan.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.activaplan.app.data.local.UserDataStore
import com.activaplan.app.data.local.createDataStore
import com.activaplan.app.data.repository.ComposeResourceExerciseDataSource
import com.activaplan.app.data.repository.ExerciseDataSource
import com.activaplan.app.data.repository.ExerciseRepository
import com.activaplan.app.data.repository.UserDataRepository
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

fun interface DataStorePathProvider {
    fun path(): String
}

/** Modul spesifik platform; menyediakan path file DataStore. */
expect fun platformModule(): Module

val commonModule = module {
    single { Json { ignoreUnknownKeys = true; encodeDefaults = true } }
    single<DataStore<Preferences>> { createDataStore(get<DataStorePathProvider>()) }
    single<ExerciseDataSource> { ComposeResourceExerciseDataSource() }
    single { ExerciseRepository(get(), get()) }
    single { UserDataStore(get(), get()) }
    single { UserDataRepository(get(), get()) }
}

fun appModules(): List<Module> = listOf(platformModule(), commonModule)

/** Aman dipanggil berulang; hanya memulai Koin sekali. */
fun initKoin(config: KoinAppDeclaration? = null) {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    startKoin {
        config?.invoke(this)
        modules(appModules())
    }
}
