package com.android.contacts.domain.photo.usecase

import com.android.contacts.data.photo.repository.ContactPhotoRepository
import javax.inject.Inject

internal fun interface GetPhotoCropSize {
    suspend operator fun invoke(): Int
}

internal class GetPhotoCropSizeImpl @Inject constructor(
    private val contactPhotoRepository: ContactPhotoRepository,
) : GetPhotoCropSize {
    override suspend operator fun invoke(): Int {
        return contactPhotoRepository.getMaxPhotoDimension() ?: DEFAULT_PHOTO_DIM
    }

    private companion object {
        const val DEFAULT_PHOTO_DIM = 720
    }
}
