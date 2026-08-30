package com.android.contacts.ui.contactdetails

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

internal class ContactDetailsLaunchers(
    val editor: ActivityResultLauncher<Intent>,
    val directoryCopy: ActivityResultLauncher<Intent>,
    val joinTarget: ActivityResultLauncher<Intent>,
    val ringtone: ActivityResultLauncher<Intent>,
)
