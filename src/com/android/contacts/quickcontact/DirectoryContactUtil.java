/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts.quickcontact;

import android.provider.ContactsContract.Directory;

import com.android.contacts.model.Contact;

/**
 * Utility class to support adding directory contacts.
 *
 * This class is coupled with the contact details screen, but is left out of it to avoid
 * ballooning the size of the file.
 */
public class DirectoryContactUtil {

    public static boolean isDirectoryContact(Contact contactData) {
        // Not a directory contact? Nothing to fix here
        if (contactData == null || !contactData.isDirectoryEntry()) return false;

        // No export support? Too bad
        return contactData.getDirectoryExportSupport() != Directory.EXPORT_SUPPORT_NONE;
    }
}
