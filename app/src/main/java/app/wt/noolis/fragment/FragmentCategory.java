package app.wt.noolis.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import app.wt.noolis.ActivityCategoryDetails;
import app.wt.noolis.R;
import app.wt.noolis.adapter.ListAdapterCategory;
import app.wt.noolis.data.DatabaseManager;
import app.wt.noolis.model.Category;

public class FragmentCategory extends Fragment {

    private RecyclerView recyclerView;
    private ListAdapterCategory mAdapter;
    private View view;
    private LinearLayout lyt_not_found;
    private DatabaseManager db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_category, null);

        //connect db
        db = new DatabaseManager(getActivity());

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        lyt_not_found = (LinearLayout) view.findViewById(R.id.lyt_not_found);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // specify an adapter (see also next example)
        mAdapter = new ListAdapterCategory(getActivity(), db.getAllCategory());
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ListAdapterCategory.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Category obj, int position) {
                Intent i = new Intent(getActivity(), ActivityCategoryDetails.class);
                i.putExtra(ActivityCategoryDetails.EXTRA_OBJCT, obj);
                startActivity(i);
            }
        });

        if (mAdapter.getItemCount() == 0) {
            lyt_not_found.setVisibility(View.VISIBLE);
        } else {
            lyt_not_found.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter = new ListAdapterCategory(getActivity(), db.getAllCategory());
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ListAdapterCategory.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Category obj, int position) {
                Intent i = new Intent(getActivity(), ActivityCategoryDetails.class);
                i.putExtra(ActivityCategoryDetails.EXTRA_OBJCT, obj);
                startActivity(i);
            }
        });
    }
}
