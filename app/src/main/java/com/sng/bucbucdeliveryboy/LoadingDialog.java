package com.sng.bucbucdeliveryboy;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

public
class LoadingDialog {
    public static LoadingDialog loadingProgress=null;
    Dialog dialog;

    ProgressBar mProgressBar;

    public static LoadingDialog getInstance(){
        if (loadingProgress==null){
            loadingProgress=new LoadingDialog();
        }
        return loadingProgress;
    }

    public void ShowProgress(Context context, String message, boolean cancelable){
        dialog=new Dialog(context,R.style.MyTheme);
        // no tile for the dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading_dialog);
        mProgressBar = (ProgressBar) dialog.findViewById(R.id.progressbar1);
        //  mProgressBar.getIndeterminateDrawable().setColorFilter(context.getResources()
        // .getColor(R.color.material_blue_gray_500), PorterDuff.Mode.SRC_IN);
        TextView progressText = (TextView) dialog.findViewById(R.id.loadingTV);
        progressText.setText("" + message);
        progressText.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        // you can change or add this line according to your need
        mProgressBar.setIndeterminate(true);
        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(cancelable);
        dialog.show();
    }

    public void hideProgress() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}

