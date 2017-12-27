package com.example.laptop.rideuser;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Laptop on 12/23/2017.
 */

public class BottomSheetRiderFragment extends BottomSheetDialogFragment {
    String mTag;

    public static BottomSheetRiderFragment newInstance(String tag)
    {

        BottomSheetRiderFragment f = new BottomSheetRiderFragment();
        Bundle args = new Bundle();
        args.putString("TAG",tag);
        f.setArguments(args);
        return f;
    }

    //ctrl+O

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = getArguments().getString("TAG");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.bottom_sheet_rider,container,false);
        return view;

    }
}
