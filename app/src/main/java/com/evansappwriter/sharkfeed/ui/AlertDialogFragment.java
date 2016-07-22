package com.evansappwriter.sharkfeed.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;

import com.evansappwriter.sharkfeed.util.Keys;
import com.evansappwriter.sharkfeed.util.Utils;


public class AlertDialogFragment extends DialogFragment {
    private static final String TAG = "AlertDialogFragment";

    private Activity mActivity;

    /**
     * The activity or fragment that creates an instance of this dialog fragment must implement
     * this interface in order to receive event callbacks. Each method passes the DialogFragment in
     * case the host needs to query it.
     */
    public interface OnDialogDoneListener {
        void onDone(String tag, boolean cancelled, CharSequence message);
    }

    /**
     * Use this instance of the interface to deliver action events
     */
    private OnDialogDoneListener mListener;

    public AlertDialogFragment() {

    }

    public static AlertDialogFragment newInstance(int dialogId, Bundle args) {
        if (args == null) {
            args = new Bundle();
        }
        args.putInt(Keys.DIALOG_ID_KEY, dialogId);
        AlertDialogFragment frag = new AlertDialogFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Activity act) {
        if (mListener == null) {
            mListener = (OnDialogDoneListener) getTargetFragment();
            // if coming from activity
            if (mListener == null) {
                //mListener = (OnDialogDoneListener) getActivity();
            }
        }

        mActivity = act;

        super.onAttach(act);
    }

    public void setListener(OnDialogDoneListener l) {
        mListener = l;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mActivity == null) {
            Utils.printLogInfo(TAG, "Activity is null");
            return null;
        }

        int dialogId = getArguments().getInt(Keys.DIALOG_ID_KEY);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        switch (dialogId) {
            case Keys.DIALOG_GENERAL_LOADING:
                ProgressDialog progressDialog = new ProgressDialog(mActivity);
                if (!getArguments().containsKey(Keys.DIALOG_MESSAGE_KEY)) {
                    //progressDialog.setMessage(mActivity.getResources().getString(R.string.general_loading_message));
                } else {
                    progressDialog.setMessage(getArguments().getString(Keys.DIALOG_MESSAGE_KEY));
                }
                progressDialog.setCancelable(false);
                progressDialog.show();
                return progressDialog;
            case Keys.DIALOG_GENERAL_MESSAGE:
            case Keys.DIALOG_GENERAL_ERROR:
            default:
                String title = getArguments().getString(Keys.DIALOG_TITLE_KEY);
                if (!TextUtils.isEmpty(title)) {
                    builder.setTitle(title);
                }
                String message = getArguments().getString(Keys.DIALOG_MESSAGE_KEY);
                if (!TextUtils.isEmpty(message)) {
                    builder.setMessage(message);
                }
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
                return builder.create();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        final int dialogId = getArguments().getInt(Keys.DIALOG_ID_KEY);
        if (mActivity == null) {
            return;
        }
        switch (dialogId) {
            case Keys.DIALOG_GENERAL_ERROR:
                mActivity.finish();
                break;
            case Keys.DIALOG_GENERAL_MESSAGE:
            default:
        }
    }
}
