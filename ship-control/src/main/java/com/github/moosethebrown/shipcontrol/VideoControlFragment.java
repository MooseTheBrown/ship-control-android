package com.github.moosethebrown.shipcontrol;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.ui.PlayerView;

public class VideoControlFragment extends Fragment {

    private static final String LOG_TAG = "VideoControlFragment";

    private ExoPlayer player = null;

    private VideoSettingsProvider settingsProvider = null;

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

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onStart() {
        super.onStart();

        Log.d(LOG_TAG, "onStart()");

        LoadControl loadControl = new DefaultLoadControl.Builder().
                setBufferDurationsMs(10, 20, 10, 0).
                build();
        player = new ExoPlayer.Builder(getContext()).
                setLoadControl(loadControl).
                build();
        PlayerView playerView = getView().findViewById(R.id.player_view);
        playerView.setPlayer(player);
        MediaItem mediaItem = new MediaItem.Builder().
                setUri(settingsProvider.getVideoStreamUri()).
                build();
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
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

        player.release();
        player = null;
    }
}