package com.jchanghong.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.LinearLayout
import com.jchanghong.ActivityNoteEdit
import com.jchanghong.R
import com.jchanghong.adapter.ListAdapterNote
import com.jchanghong.data.DatabaseManager
import com.jchanghong.model.Note

class FragmentNote : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var mAdapter: ListAdapterNote
    private var mview: View? = null
    private var searchView: SearchView? = null
    private var lyt_not_found: LinearLayout? = null
    private var db: DatabaseManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mview = inflater.inflate(R.layout.fragment_note, null)
        db = DatabaseManager(activity)

        // activate fragment menu
        setHasOptionsMenu(true)

        recyclerView = mview!!.findViewById<View>(R.id.recyclerView) as RecyclerView
        lyt_not_found = mview!!.findViewById<View>(R.id.lyt_not_found) as LinearLayout

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()

        // specify an adapter (see also next example)
        displayData(db!!.allNotes)
        return mview
    }

    override fun onResume() {
        // specify an adapter (see also next example)
        displayData(db!!.allNotes)
        super.onResume()
    }

    private fun displayData(items: List<Note>) {
        mAdapter = ListAdapterNote(activity, items)
        recyclerView.adapter = mAdapter
        mAdapter.setOnItemClickListener(object :ListAdapterNote.OnItemClickListener{
            override fun onItemClick(view: View, model: Note) {
                val intent = Intent(activity, ActivityNoteEdit::class.java)
                intent.putExtra(ActivityNoteEdit.EXTRA_OBJCT, model)
                activity.startActivity(intent)
            }

        })
        if (mAdapter.itemCount == 0) {
            lyt_not_found!!.visibility = View.VISIBLE
        } else {
            lyt_not_found!!.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_note, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView!!.isIconified = false
        searchView!!.queryHint = "Search note..."
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                try {
                    mAdapter.filter.filter(s)
                } catch (e: Exception) {
                }

                return true
            }
        })
        // Detect SearchView icon clicks
        searchView!!.setOnSearchClickListener { setItemsVisibility(menu, searchItem, false) }

        // Detect SearchView close
        searchView!!.setOnCloseListener {
            setItemsVisibility(menu, searchItem, true)
            false
        }
        searchView!!.onActionViewCollapsed()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun setItemsVisibility(menu: Menu, exception: MenuItem, visible: Boolean) {
        (0..menu.size() - 1)
                .map { menu.getItem(it) }
                .filter { it !== exception }
                .forEach { it.isVisible = visible }
    }
}
