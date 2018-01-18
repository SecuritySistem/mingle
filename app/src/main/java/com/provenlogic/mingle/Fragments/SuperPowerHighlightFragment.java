package com.provenlogic.mingle.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.provenlogic.mingle.R;


/**
 * Created by Anurag on 4/10/2017.
 */

public class SuperPowerHighlightFragment extends Fragment{

    public SuperPowerHighlightFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.super_power_highlight_fragment , container, false);
        return view;
    }

}
