package com.jchanghong.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import com.jchanghong.ActivityNoteEdit;
import com.jchanghong.R;
import com.jchanghong.adapter.ListAdapterNote;
import com.jchanghong.data.DatabaseManager;
import com.jchanghong.model.Note;

public class FragmentFavorites extends Fragment {
    private RecyclerView recyclerView;
    private ListAdapterNote mAdapter;
    private View view;
    private SearchView searchView;
    private LinearLayout lyt_not_found;
    private DatabaseManager db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favorites, null);

        //connect db
        db = new DatabaseManager(getActivity());

        // activate fragment menu
        setHasOptionsMenu(true);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        lyt_not_found = (LinearLayout) view.findViewById(R.id.lyt_not_found);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // specify an adapter (see also next example)
        displayData(db.getAllFavNote());
        return view;
    }

    @Override
    public void onResume() {
        displayData(db.getAllFavNote());
        super.onResume();
    }

    private void displayData(List<Note> items) {
        mAdapter = new ListAdapterNote(getActivity(), items);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ListAdapterNote.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Note model) {
                Intent intent = new Intent(getActivity(), ActivityNoteEdit.class);
                intent.putExtra(ActivityNoteEdit.EXTRA_OBJCT, model);
                getActivity().startActivity(intent);
            }
        });
        if (mAdapter.getItemCount() == 0) {
            lyt_not_found.setVisibility(View.VISIBLE);
        } else {
            lyt_not_found.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_note, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setIconified(false);
        searchView.setQueryHint("Search note...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                try {
                    mAdapter.getFilter().filter(s);
                } catch (Exception e) {
                }
                return true;
            }
        });
        // Detect SearchView icon clicks
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setItemsVisibility(menu, searchItem, false);
            }
        });

        // Detect SearchView close
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                setItemsVisibility(menu, searchItem, true);
                return false;
            }
        });
        searchView.onActionViewCollapsed();
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setItemsVisibility(Menu menu, MenuItem exception, boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception) item.setVisible(visible);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
