package com.android.contacts.di.debug

import javax.inject.Qualifier

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class SeedTestContactsCount

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class SeedTestGroupsCount
