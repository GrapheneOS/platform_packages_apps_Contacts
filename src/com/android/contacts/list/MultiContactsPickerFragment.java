package com.android.contacts.list;

import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.contacts.R;

public class MultiContactsPickerFragment extends
        MultiSelectContactsListFragment<DefaultContactListAdapter> {

    public static final String TAG = MultiContactsPickerFragment.class.getSimpleName();

    public MultiContactsPickerFragment() {
        setPhotoLoaderEnabled(true);
        setSectionHeaderDisplayEnabled(true);
        setHasOptionsMenu(true);
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.contact_list_content, null);
    }

    @Override
    protected DefaultContactListAdapter createListAdapter() {
        var adapter = new DefaultContactListAdapter(getActivity());
        adapter.setSectionHeaderDisplayEnabled(true);
        adapter.setDisplayPhotos(true);
        return adapter;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.items_multi_select, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final MenuItem item = menu.findItem(R.id.menu_send);
        item.setVisible(getAdapter().hasSelectedItems());
        item.getActionView().setOnClickListener(v -> onOptionsItemSelected(item));

        updateMenuItemSendForPickerIntent(item);;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_send) {
            setResultAndFinish(getSelectedContactIdsArray(),
                    Contacts.CONTENT_URI, Contacts.CONTENT_ITEM_TYPE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean onItemLongClick(int position, long id) {
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        displayCheckBoxes(true);
    }
}
