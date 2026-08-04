/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.contacts.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.android.contacts.R;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;

import java.util.List;

/**
 * Utility class for selecting an Account for importing contact(s)
 */
public class AccountSelectionUtil {
    // TODO: maybe useful for EditContactActivity.java...
    private static final String LOG_TAG = "AccountSelectionUtil";

    public static Uri mPath;

    public static abstract class AccountSelectedListener
            implements DialogInterface.OnClickListener {

        final private Activity mActivity;
        final private int mResId;
        final private int mSubscriptionId;

        final protected List<AccountWithDataSet> mAccountList;

        public AccountSelectedListener(Activity activity, List<AccountWithDataSet> accountList,
                int resId, int subscriptionId) {
            if (accountList == null || accountList.size() == 0) {
                Log.e(LOG_TAG, "The size of Account list is 0.");
            }
            mActivity = activity;
            mAccountList = accountList;
            mResId = resId;
            mSubscriptionId = subscriptionId;
        }

        public AccountSelectedListener(Activity activity, List<AccountWithDataSet> accountList,
                int resId) {
            // Subscription id is only needed for importing from SIM card. We can safely ignore
            // its value for SD card importing.
            this(activity, accountList, resId, /* subscriptionId = */ -1);
        }

        public abstract void onClick(DialogInterface dialog, int which);
    }

    /**
     * When OnClickListener or OnCancelListener is null, uses a default listener.
     * The default OnCancelListener just closes itself with {@link Dialog#dismiss()}.
     */
    public static Dialog getSelectAccountDialog(Activity activity, int resId,
            @NonNull DialogInterface.OnClickListener onClickListener,
            DialogInterface.OnCancelListener onCancelListener) {
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(activity);
        final List<AccountWithDataSet> writableAccountList =
                accountTypes.blockForWritableAccounts();

        Log.i(LOG_TAG, "The number of available accounts: " + writableAccountList.size());

        // Assume accountList.size() > 1

        // Wrap our context to inflate list items using correct theme
        final Context dialogContext = new ContextThemeWrapper(
                activity, android.R.style.Theme_Light);
        final LayoutInflater dialogInflater = (LayoutInflater)dialogContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ArrayAdapter<AccountWithDataSet> accountAdapter =
            new ArrayAdapter<AccountWithDataSet>(
                    activity, R.layout.account_selector_list_item_condensed, writableAccountList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = dialogInflater.inflate(
                            R.layout.account_selector_list_item_condensed,
                            parent, false);
                }

                final TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
                final TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
                final ImageView icon = (ImageView) convertView.findViewById(android.R.id.icon);

                final AccountWithDataSet account = this.getItem(position);
                final AccountType accountType = accountTypes.getAccountType(
                        account.type, account.dataSet);
                final Context context = getContext();

                text1.setText(accountType.getDisplayLabel(context));
                text2.setText(account.name);
                icon.setImageDrawable(accountType.getDisplayIcon(getContext()));

                return convertView;
            }
        };

        if (onCancelListener == null) {
            onCancelListener = new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                }
            };
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final TextView title = (TextView) View.inflate(activity, R.layout.dialog_title, null);
        title.setText(R.string.dialog_new_contact_account);
        builder.setCustomTitle(title);
        builder.setSingleChoiceItems(accountAdapter, 0, onClickListener);
        builder.setOnCancelListener(onCancelListener);
        final AlertDialog result = builder.create();
        return result;
    }
}
