package com.github.moosethebrown.shipcontrol;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import com.alexvas.rtsp.widget.RtspSurfaceView;

public class VideoControlFragment extends Fragment {

    private static final String LOG_TAG = "VideoControlFragment";

    private VideoSettingsProvider settingsProvider = null;
    private RtspSurfaceView rtspSurfaceView=  null;

    public interface VideoSettingsProvider {
        String getVideoStreamUri();
    }

    public VideoControlFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_control, container, false);

        rtspSurfaceView = (RtspSurfaceView)view.findViewById(R.id.player_view);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VideoControlFragment.VideoSettingsProvider) {
            settingsProvider = (VideoControlFragment.VideoSettingsProvider) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement VideoControlFragment.VideoSettingsProvider");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        settingsProvider = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(LOG_TAG, "onStart()");

        Uri uri = Uri.parse(settingsProvider.getVideoStreamUri());
        rtspSurfaceView.init(uri, null, null, null);
        rtspSurfaceView.start(true, false, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(LOG_TAG, "onStop()");
        rtspSurfaceView.stop();
    }
}