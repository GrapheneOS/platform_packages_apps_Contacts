/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.contacts.group;

import android.net.Uri;
import android.provider.ContactsContract.Groups;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Group utility methods.
 */
public final class GroupUtil {

    public final static String ALL_GROUPS_SELECTION = Groups.DELETED + "=0";

    public final static String DEFAULT_SELECTION = ALL_GROUPS_SELECTION + " AND "
            + Groups.AUTO_ADD + "=0 AND " + Groups.FAVORITES + "=0";

    public static final String ACTION_ADD_TO_GROUP = "addToGroup";
    public static final String ACTION_CREATE_GROUP = "createGroup";
    public static final String ACTION_REMOVE_FROM_GROUP = "removeFromGroup";
    public static final String ACTION_UPDATE_GROUP = "updateGroup";

    // System IDs of FFC groups in Google accounts
    private static final Set<String> FFC_GROUPS =
            new HashSet(Arrays.asList("Friends", "Family", "Coworkers"));

    private GroupUtil() {
    }

    public static long[] convertLongSetToLongArray(Set<Long> set) {
        final Long[] contactIds = set.toArray(new Long[set.size()]);
        final long[] result = new long[contactIds.length];
        for (int i = 0; i < contactIds.length; i++) {
            result[i] = contactIds[i];
        }
        return result;
    }

    public static boolean isSystemIdFFC(String systemId) {
        return systemId != null && !TextUtils.isEmpty(systemId) && FFC_GROUPS.contains(systemId);
    }

    /**
     * Returns true the URI is a group URI.
     */
    public static boolean isGroupUri(Uri uri) {
        return  uri != null && uri.toString().startsWith(Groups.CONTENT_URI.toString());
    }

    /**
     * Sort groups alphabetically and in a localized way.
     */
    public static String getGroupsSortOrder() {
        return Groups.TITLE + " COLLATE LOCALIZED ASC";
    }
}
