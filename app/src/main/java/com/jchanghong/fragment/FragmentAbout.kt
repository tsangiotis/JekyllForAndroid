package com.jchanghong.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.jchanghong.R

class FragmentAbout : Fragment() {

    lateinit internal var parent_view: View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        parent_view = inflater.inflate(R.layout.fragment_about, null)
        prepareAds()
        return parent_view
    }

    private fun prepareAds() {
        //        AdView mAdView = (AdView) parent_view.findViewById(R.id.ad_view);
        //        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        // Start loading the ad in the background.
        //        mAdView.loadAd(adRequest);
    }
}
