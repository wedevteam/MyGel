package com.wedev.mygel;

import androidx.appcompat.app.AppCompatActivity;


import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        // finding videoview by its id
        VideoView videoView = findViewById(R.id.videoView);
        videoView.setZOrderOnTop(true);
        // Uri object to refer the
        // resource from the videoUrl
        Uri uri = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");

        // sets the resource from the
        // videoUrl to the videoView
        videoView.setVideoURI(uri);

        // creating object of
        // media controller class
        MediaController mediaController = new MediaController(this);

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
}