package com.wedev.mygel;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoTFragment extends Fragment {
   ImageView immaginep;
    VideoView videoView;
   View  view;
    public VideoTFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_t, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        immaginep = view.findViewById(R.id.immaginep);
        videoView = view.findViewById(R.id.videov);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) videoView.getLayoutParams();
        params.width = metrics.widthPixels;
        params.height = metrics.heightPixels;
        params.leftMargin = 0;
        videoView.setLayoutParams(params);
        immaginep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {// finding videoview by its id


                videoView.setVisibility(View.VISIBLE);

                videoView.setZOrderOnTop(true);

                // Uri object to refer the
                // resource from the videoUrl
                Uri uri = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");

                // sets the resource from the
                // videoUrl to the videoView
                videoView.setVideoURI(uri);

                // creating object of
                // media controller class
                MediaController mediaController = new MediaController(getContext());

                // sets the anchor view
                // anchor view for the videoView
                mediaController.setAnchorView(videoView);

                // sets the media player to the videoView
                mediaController.setMediaPlayer(videoView);

                // sets the media controller to the videoView
                videoView.setMediaController(mediaController);

                // starts the video
                videoView.start();

            }
        });
    }
}