/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.contacts.drawer;

import android.app.Activity;
import android.app.Fragment;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.android.contacts.R;
import com.android.contacts.activities.PeopleActivity.ContactsView;
import com.android.contactsbind.ObjectFactory;

public class DrawerFragment extends Fragment {

    private static final String KEY_CONTACTS_VIEW = "contactsView";

    private WelcomeContentObserver mObserver;
    private ListView mDrawerListView;
    private DrawerAdapter mDrawerAdapter;
    private ContactsView mCurrentContactsView;
    private DrawerFragmentListener mListener;
    // Transparent scrim drawn at the top of the drawer fragment.
    private ScrimDrawable mScrimDrawable;

    private final class WelcomeContentObserver extends ContentObserver {
        private WelcomeContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            mDrawerAdapter.notifyDataSetChanged();
        }
    }

    public DrawerFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DrawerFragmentListener) {
            mListener = (DrawerFragmentListener) activity;
        } else {
            throw new IllegalArgumentException(
                    "Activity must implement " + DrawerFragmentListener.class.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View contentView = inflater.inflate(R.layout.drawer_fragment, null);
        mDrawerListView = (ListView) contentView.findViewById(R.id.list);
        mDrawerAdapter = new DrawerAdapter(getActivity());
        mDrawerAdapter.setSelectedContactsView(mCurrentContactsView);
        mDrawerListView.setAdapter(mDrawerAdapter);
        mDrawerListView.setOnItemClickListener(mOnDrawerItemClickListener);

        if (savedInstanceState != null) {
            final ContactsView contactsView =
                    ContactsView.values()[savedInstanceState.getInt(KEY_CONTACTS_VIEW)];
            setNavigationItemChecked(contactsView);
        } else {
            setNavigationItemChecked(ContactsView.ALL_CONTACTS);
        }

        final FrameLayout root = (FrameLayout) contentView.findViewById(R.id.drawer_fragment_root);
        root.setFitsSystemWindows(true);
        root.setOnApplyWindowInsetsListener(new WindowInsetsListener());
        root.setForegroundGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL);

        mScrimDrawable = new ScrimDrawable();
        root.setForeground(mScrimDrawable);

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // todo double check on the new Handler() thing
        final Uri uri = ObjectFactory.getWelcomeUri();
        if (uri != null) {
            mObserver = new WelcomeContentObserver(new Handler());
            getActivity().getContentResolver().registerContentObserver(uri, false, mObserver);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CONTACTS_VIEW, mCurrentContactsView.ordinal());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mObserver != null) {
            getActivity().getContentResolver().unregisterContentObserver(mObserver);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private final OnItemClickListener mOnDrawerItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            if (mListener == null) {
                return;
            }
            final int viewId = v.getId();
            if (viewId == R.id.nav_all_contacts) {
                mListener.onContactsViewSelected(ContactsView.ALL_CONTACTS);
                setNavigationItemChecked(ContactsView.ALL_CONTACTS);
            } else if (viewId == R.id.nav_assistant) {
                mListener.onContactsViewSelected(ContactsView.ASSISTANT);
                setNavigationItemChecked(ContactsView.ASSISTANT);
            } else if (viewId == R.id.nav_accounts) {
                mListener.onOpenAccountsFilter();
            } else if (viewId == R.id.nav_groups) {
                mListener.onOpenGroups();
            } else if (viewId == R.id.nav_settings) {
                mListener.onOpenSettings();
            } else if (viewId == R.id.nav_help) {
                mListener.onLaunchHelpFeedback();
            } else {
                return;
            }
            mListener.onDrawerItemClicked();
        }
    };

    public void setNavigationItemChecked(ContactsView contactsView) {
        mCurrentContactsView = contactsView;
        if (mDrawerAdapter != null) {
            mDrawerAdapter.setSelectedContactsView(contactsView);
        }
    }

    public void setIsAccountSwitcherVisible(boolean isVisible) {
        mDrawerAdapter.setIsAccountSwitcherVisible(isVisible);
    }

    private void applyTopInset(int insetTop) {
        // set height of the scrim
        mScrimDrawable.setIntrinsicHeight(insetTop);
        mDrawerListView.setPadding(mDrawerListView.getPaddingLeft(),
                insetTop, mDrawerListView.getPaddingRight(),
                mDrawerListView.getPaddingBottom());
    }

    public interface DrawerFragmentListener {
        void onDrawerItemClicked();
        void onContactsViewSelected(ContactsView mode);
        void onOpenAccountsFilter();
        void onOpenGroups();
        void onOpenSettings();
        void onLaunchHelpFeedback();
    }

    private class WindowInsetsListener implements View.OnApplyWindowInsetsListener {
        @Override
        public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
            final int insetTop = insets.getSystemWindowInsetTop();
            applyTopInset(insetTop);
            return insets;
        }
    }
}
