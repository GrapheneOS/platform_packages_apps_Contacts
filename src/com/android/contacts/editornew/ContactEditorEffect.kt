package com.android.contacts.editornew

import android.os.Bundle
import com.android.contacts.model.RawContactDelta

internal sealed interface ContactEditorEffect {
    data class Save(val updatedPhotos: Bundle, val rawContactDelta: RawContactDelta) :
        ContactEditorEffect
}
