package app.wt.noolis.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.wt.noolis.R

class FragmentAbout : Fragment() {

    lateinit internal var parent_view: View
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        parent_view = inflater!!.inflate(R.layout.fragment_about, null)
        prepareAds()
        return parent_view
    }

    private fun prepareAds() {}
}
