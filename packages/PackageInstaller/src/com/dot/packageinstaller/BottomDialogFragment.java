package com.dot.packageinstaller;

import android.app.DialogFragment;
import android.view.Gravity;
import android.view.ViewGroup;

public class BottomDialogFragment extends DialogFragment {

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_background_inset, null));
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
