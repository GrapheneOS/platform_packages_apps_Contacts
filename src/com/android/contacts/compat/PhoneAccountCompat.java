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
 * limitations under the License
 */
package com.android.contacts.compat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.telecom.PhoneAccount;

import androidx.annotation.Nullable;

/**
 * Compatiblity class for {@link android.telecom.PhoneAccount}
 */
public class PhoneAccountCompat {

    /**
     * Gets the {@link Icon} associated with the given {@link PhoneAccount}
     *
     * @param phoneAccount the PhoneAccount from which to retrieve the Icon
     * @return the Icon, or null
     */
    @Nullable
    private static Icon getIcon(@Nullable PhoneAccount phoneAccount) {
        if (phoneAccount == null) {
            return null;
        }

        return phoneAccount.getIcon();
    }

    /**
     * Builds and returns an icon {@code Drawable} to represent this {@code PhoneAccount} in a user
     * interface.
     *
     * @param phoneAccount the PhoneAccount from which to build the icon.
     * @param context A {@code Context} to use for loading Drawables.
     *
     * @return An icon for this PhoneAccount, or null
     */
    @Nullable
    public static Drawable createIconDrawable(@Nullable PhoneAccount phoneAccount,
            @Nullable Context context) {
        if (phoneAccount == null || context == null) {
            return null;
        }

        Icon accountIcon = getIcon(phoneAccount);
        if (accountIcon == null) {
            return null;
        }
        return accountIcon.loadDrawable(context);
    }
}
