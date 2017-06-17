package gr.tsagi.jekyllforandroid.app.adapters

import android.content.Context
import android.database.Cursor
import android.support.v4.widget.CursorAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import app.wt.noolis.R
import gr.tsagi.jekyllforandroid.app.fragments.PostsListFragment
import gr.tsagi.jekyllforandroid.app.utils.Utility

/**
\* Created with IntelliJ IDEA.
\* User: tsagi
\* Date: 8/9/14
\* Time: 9:15
\*/

/**
 * [PostListAdapter] exposes a list of weather forecasts
 * from a [Cursor] to a [android.widget.ListView].
 */
class PostListAdapter(context: Context, c: Cursor, flags: Int) : CursorAdapter(context, c, flags) {

    lateinit internal var utility: Utility

    /**
     * Cache of the children views for a forecast list item.
     */
    class ViewHolder(view: View) {
        val titleView: TextView = view.findViewById<View>(R.id.ptitle) as TextView
        val dateView: TextView

        init {
            dateView = view.findViewById<View>(R.id.pdate) as TextView
        }
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        // Choose the layout type
        val layoutId = R.layout.post_list_item

        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)

        val viewHolder = ViewHolder(view)
        view.tag = viewHolder

        return view
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val viewHolder = view.tag as ViewHolder

        utility = Utility(context)

        // Read post title from cursor
        val title = cursor.getString(PostsListFragment.COL_POST_TITLE)
        // Find TextView and set weather forecast on it
        viewHolder.titleView.text = title

        // Read date from cursor
        val dateString = cursor.getString(PostsListFragment.COL_POST_DATE)

        // Find TextView and set formatted date on it
        if (dateString != "0")
            viewHolder.dateView.text = utility.getFriendlyDayString(dateString)
        else
            viewHolder.dateView.text = ""
    }
}
