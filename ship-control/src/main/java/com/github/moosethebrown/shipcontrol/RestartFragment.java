package com.github.moosethebrown.shipcontrol;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class RestartFragment extends DialogFragment {

    private Listener listener = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.restartDialogMessage);
        builder.setPositiveButton(R.string.restartDialogRestartNow, (DialogInterface d, int id) -> {
            if (listener != null) {
                listener.onRestartNow();
            }
        });
        builder.setNegativeButton(R.string.restartDialogLater, (DialogInterface d, int id) -> {
            // just log, nothing more to do here
            Log.i(MainActivity.LOG_TAG, "RestartDialog: user refused to restart now");
        });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Listener) {
            listener = (Listener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Listener");
        }
    }

    public interface Listener {
        void onRestartNow();
    }
}
