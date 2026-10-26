package com.activaplan.app.di

import android.content.Context
import com.activaplan.app.data.local.DATASTORE_FILE_NAME
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single {
        val context = get<Context>()
        DataStorePathProvider { context.filesDir.resolve(DATASTORE_FILE_NAME).absolutePath }
    }
}
