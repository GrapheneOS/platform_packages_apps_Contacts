package com.android.contacts.domain.photo.usecase

import android.net.Uri
import android.util.Log
import com.android.contacts.data.photo.repository.ContactPhotoRepository
import com.android.contacts.domain.photo.model.AttachPhotoResult
import com.android.contacts.domain.photo.model.AttachPhotoTarget
import javax.inject.Inject

internal fun interface AttachPhotoToContact {
    suspend operator fun invoke(
        contactUri: Uri,
        target: AttachPhotoTarget.Ready,
        photoUri: Uri,
    ): AttachPhotoResult
}

internal class AttachPhotoToContactImpl @Inject constructor(
    private val contactPhotoRepository: ContactPhotoRepository,
) : AttachPhotoToContact {

    override suspend operator fun invoke(
        contactUri: Uri,
        target: AttachPhotoTarget.Ready,
        photoUri: Uri,
    ): AttachPhotoResult {
        val thumbnail = contactPhotoRepository.buildThumbnail(photoUri) ?: run {
            Log.w(TAG, "Could not create scaled and compressed bitmap")
            return AttachPhotoResult.INVALID_PHOTO
        }

        val isSaved = when (target) {
            is AttachPhotoTarget.Ready.ExistingRawContact ->
                contactPhotoRepository.savePhotoToRawContact(
                    contactUri = contactUri,
                    rawContactId = target.rawContactId,
                    thumbnail = thumbnail,
                    photoUri = photoUri,
                )
            is AttachPhotoTarget.Ready.NewRawContact ->
                contactPhotoRepository.savePhotoToNewRawContact(
                    contactUri = contactUri,
                    account = target.account,
                    thumbnail = thumbnail,
                    photoUri = photoUri,
                )
        }
        return when {
            isSaved -> AttachPhotoResult.SUCCESS
            else -> AttachPhotoResult.SAVE_FAILED
        }
    }

    private companion object {
        const val TAG = "AttachPhotoToContact"
    }
}
