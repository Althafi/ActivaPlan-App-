package com.activaplan.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.activaplan.app.di.DataStorePathProvider
import okio.Path.Companion.toPath

internal const val DATASTORE_FILE_NAME = "activaplan.preferences_pb"

fun createDataStore(pathProvider: DataStorePathProvider): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { pathProvider.path().toPath() })
