package com.android.contacts.di.photo

import com.android.contacts.data.photo.repository.ContactPhotoRepository
import com.android.contacts.data.photo.repository.ContactPhotoRepositoryImpl
import com.android.contacts.domain.photo.usecase.AttachPhotoToContact
import com.android.contacts.domain.photo.usecase.AttachPhotoToContactImpl
import com.android.contacts.domain.photo.usecase.GetPhotoCropSize
import com.android.contacts.domain.photo.usecase.GetPhotoCropSizeImpl
import com.android.contacts.domain.photo.usecase.ResolveAttachPhotoTarget
import com.android.contacts.domain.photo.usecase.ResolveAttachPhotoTargetImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PhotoBindsModule {

    @Binds
    abstract fun bindContactPhotoRepository(
        impl: ContactPhotoRepositoryImpl,
    ): ContactPhotoRepository

    @Binds
    @Reusable
    abstract fun bindAttachPhotoToContact(
        impl: AttachPhotoToContactImpl,
    ): AttachPhotoToContact

    @Binds
    @Reusable
    abstract fun bindGetPhotoCropSize(
        impl: GetPhotoCropSizeImpl,
    ): GetPhotoCropSize

    @Binds
    @Reusable
    abstract fun bindResolveAttachPhotoTarget(
        impl: ResolveAttachPhotoTargetImpl,
    ): ResolveAttachPhotoTarget
}
