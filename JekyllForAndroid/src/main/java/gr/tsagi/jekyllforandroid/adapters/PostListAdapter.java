package gr.tsagi.jekyllforandroid.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import gr.tsagi.jekyllforandroid.R;
import gr.tsagi.jekyllforandroid.fragments.PostsListFragment;
import gr.tsagi.jekyllforandroid.utils.Utility;

/**
 * Created by tsagi on 8/9/14.
 */

/**
 * {@link PostListAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class PostListAdapter extends CursorAdapter {

    Utility utility;

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final TextView titleView;
        public final TextView dateView;

        public ViewHolder(View view) {
            titleView = (TextView) view.findViewById(R.id.ptitle);
            dateView = (TextView) view.findViewById(R.id.pdate);
        }
    }

    public PostListAdapter (Context context, Cursor c, int flags) {
        super(context, c, flags);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int layoutId = R.layout.post_list_item;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        utility = new Utility(context);

        // Read post title from cursor
        String title = cursor.getString(PostsListFragment.COL_POST_TITLE);
        // Find TextView and set weather forecast on it
        viewHolder.titleView.setText(title);

        // Read date from cursor
        String dateString = cursor.getString(PostsListFragment.COL_POST_DATE);

        // Find TextView and set formatted date on it
        if (!dateString.equals("0"))
            viewHolder.dateView.setText(utility.getFriendlyDayString(dateString));
        else
            viewHolder.dateView.setText("");
    }
}
