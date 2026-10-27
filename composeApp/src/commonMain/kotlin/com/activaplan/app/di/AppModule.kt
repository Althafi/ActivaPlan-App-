package com.activaplan.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.activaplan.app.data.local.UserDataStore
import com.activaplan.app.data.local.createDataStore
import com.activaplan.app.data.repository.ComposeResourceExerciseDataSource
import com.activaplan.app.data.repository.ExerciseDataSource
import com.activaplan.app.data.repository.ExerciseRepository
import com.activaplan.app.data.repository.UserDataRepository
import com.activaplan.app.domain.time.LocalClock
import com.activaplan.app.domain.time.SystemLocalClock
import com.activaplan.app.domain.usecase.AddExerciseToRoutineUseCase
import com.activaplan.app.domain.usecase.CreateRoutineFromTemplateUseCase
import com.activaplan.app.domain.usecase.DeleteRoutineUseCase
import com.activaplan.app.domain.usecase.EstimateRoutineMinutesUseCase
import com.activaplan.app.domain.usecase.FilterExercisesUseCase
import com.activaplan.app.domain.usecase.GetExerciseDetailUseCase
import com.activaplan.app.domain.usecase.GetTemplateDetailUseCase
import com.activaplan.app.domain.usecase.LoadRoutineDraftUseCase
import com.activaplan.app.domain.usecase.ObserveHomeUseCase
import com.activaplan.app.domain.usecase.ObserveRoutinesUseCase
import com.activaplan.app.domain.usecase.SaveRoutineUseCase
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
    single<LocalClock> { SystemLocalClock }
    factory { ObserveHomeUseCase(get(), get(), get()) }
    factory { FilterExercisesUseCase() }
    factory { GetExerciseDetailUseCase(get()) }
    factory { AddExerciseToRoutineUseCase(get()) }
    factory { EstimateRoutineMinutesUseCase() }
    factory { ObserveRoutinesUseCase(get(), get(), get()) }
    factory { LoadRoutineDraftUseCase(get(), get()) }
    factory { SaveRoutineUseCase(get()) }
    factory { DeleteRoutineUseCase(get()) }
    factory { CreateRoutineFromTemplateUseCase(get(), get()) }
    factory { GetTemplateDetailUseCase(get(), get()) }
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
