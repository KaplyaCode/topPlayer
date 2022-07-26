package com.example.topplayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bullhead.equalizer.EqualizerFragment;
import com.bullhead.equalizer.Settings;
import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {
    ArrayList<VideoFiles> mVideoFiles = new ArrayList<>();
    PlayerView playerView;
    SimpleExoPlayer player;
    int position;
    String videoName;
    TextView title;
    private ControlsMode controlsMode;
    public enum ControlsMode{
        LOCK, FULLSCREEN;
    }
    ImageView videoBack, lock, unlock, scaling, videoList;
    VideoAdapter videoAdapter;
    RelativeLayout root;
    ConcatenatingMediaSource concatenatingMediaSource;
    ImageView nextButton, prevButton;

    //horizontal icons variables

    private ArrayList<IconModel> iconModelArrayList = new ArrayList<>();
    PlaybackIconsAdapter playbackIconsAdapter;
    RecyclerView recyclerViewIcons;
    boolean expand = false;
    PlaybackParameters parameters;
    float speed;
    DialogProperties dialogProperties;
    FilePickerDialog filePickerDialog;
    Uri uriSubtitle;
    FrameLayout eqContainer;

    //horizontal icons variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();

        setContentView(R.layout.activity_video_player);
        getSupportActionBar().hide();
        playerView = findViewById(R.id.exoplayer_view);
        position = getIntent().getIntExtra("position", 1);
        videoName = getIntent().getStringExtra("video_name");
        mVideoFiles = getIntent().getExtras().getParcelableArrayList("videoArrayList");

        screenOrientationCheck();

        nextButton = findViewById(R.id.exo_next);
        prevButton = findViewById(R.id.exo_prev);
        title = findViewById(R.id.video_title);
        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
        scaling = findViewById(R.id.scaling);
        root = findViewById(R.id.root_layout);
        videoList = findViewById(R.id.video_list);
        recyclerViewIcons = findViewById(R.id.recycler_view_icons);
        eqContainer = findViewById(R.id.eqFrame);

        title.setText(videoName);

        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        videoList.setOnClickListener(this);
        scaling.setOnClickListener(firstListener);
        dialogProperties = new DialogProperties();
        filePickerDialog = new FilePickerDialog(VideoPlayerActivity.this);
        filePickerDialog.setTitle("Select a subtitles file");
        filePickerDialog.setPositiveBtnName("Ok");
        filePickerDialog.setNegativeBtnName("Cancel");

        iconModelArrayList.add(new IconModel(R.drawable.ic_right));
        iconModelArrayList.add(new IconModel(R.drawable.ic_rotate));
        iconModelArrayList.add(new IconModel(R.drawable.ic_speed));
        iconModelArrayList.add(new IconModel(R.drawable.ic_equalizer));

        playbackIconsAdapter = new PlaybackIconsAdapter(iconModelArrayList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, true);
        recyclerViewIcons.setLayoutManager(layoutManager);
        recyclerViewIcons.setAdapter(playbackIconsAdapter);
        playbackIconsAdapter.notifyDataSetChanged();
        playbackIconsAdapter.setOnItemClickListener(new PlaybackIconsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if(position==0){
                    if (expand){
                        iconModelArrayList.clear();
                        iconModelArrayList.add(new IconModel(R.drawable.ic_right));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_rotate));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_speed));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_equalizer));
                        playbackIconsAdapter.notifyDataSetChanged();
                        expand = false;
                    }else{
                        if (iconModelArrayList.size() == 4){
                            iconModelArrayList.add(new IconModel(R.drawable.ic_subtitles));
                        }
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_left));
                        playbackIconsAdapter.notifyDataSetChanged();
                        expand = true;
                    }
                }
                if(position==1){
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        playbackIconsAdapter.notifyDataSetChanged();
                    }else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        playbackIconsAdapter.notifyDataSetChanged();
                    }
                }
                if(position==2){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(VideoPlayerActivity.this);
                    alertDialog.setTitle("Speed").setPositiveButton("Ok", null);
                    String[] items = {"0.5x","1x","1.5x","1.75x","2x"};
                    int checkedItem = -1;
                    alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0:
                                    speed = 0.5f;
                                    break;
                                case 1:
                                    speed = 1f;
                                    break;
                                case 2:
                                    speed = 1.5f;
                                    break;
                                case 3:
                                    speed = 1.75f;
                                    break;
                                case 4:
                                    speed = 2f;
                                    break;
                                default:
                                    speed = 1f;
                                    break;
                            }
                            parameters = new PlaybackParameters(speed);
                            player.setPlaybackParameters(parameters);
                        }
                    });
                    AlertDialog alert = alertDialog.create();
                    alert.show();
                }
                if(position==3){
                    if (eqContainer.getVisibility() == View.GONE){
                        eqContainer.setVisibility(View.VISIBLE);
                    }
                    final int sessionId = player.getAudioSessionId();
                    Settings.isEditing = false;
                    EqualizerFragment equalizerFragment = EqualizerFragment.newBuilder()
                            .setAccentColor(Color.parseColor("#1A78F2"))
                            .setAudioSessionId(sessionId).build();
                    getSupportFragmentManager().beginTransaction().replace(R.id.eqFrame, equalizerFragment).commit();
                    playbackIconsAdapter.notifyDataSetChanged();
                }
                if(position==4){
                    dialogProperties.selection_mode = DialogConfigs.SINGLE_MODE;
                    dialogProperties.extensions = new String[]{".srt"};
                    dialogProperties.root = new File("/mnt/sdcard");
                    filePickerDialog.setProperties(dialogProperties);
                    filePickerDialog.show();
                    filePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
                        @Override
                        public void onSelectedFilePaths(String[] files) {
                            for(String path: files){
                                File file = new File(path);
                                uriSubtitle = Uri.parse(file.getAbsolutePath().toString());
                            }
                            playVideoSubtitles(uriSubtitle);
                        }
                    });
                }
            }
        });

        playVideo();
    }

    private void playVideo() {
        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                this, Util.getUserAgent(this, "app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();

        for (int i = 0; i < mVideoFiles.size(); i++){
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.
                    Factory(dataSourceFactory).createMediaSource(Uri.parse(String.valueOf(uri)));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }

        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, C.TIME_UNSET);
        playError();
    }

    private void playVideoSubtitles(Uri subtitle) {
        long oldPosition = player.getCurrentPosition();
        player.stop();

        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                this, Util.getUserAgent(this, "app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();

        for (int i = 0; i < mVideoFiles.size(); i++){
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.
                    Factory(dataSourceFactory).createMediaSource(Uri.parse(String.valueOf(uri)));
            Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, Format.NO_VALUE, "app");
            MediaSource subtitlesSource = new SingleSampleMediaSource.Factory(dataSourceFactory).setTreatLoadErrorsAsEndOfStream(true)
                    .createMediaSource(Uri.parse(String.valueOf(subtitle)), textFormat, C.TIME_UNSET);
            MergingMediaSource mergingMediaSource = new MergingMediaSource(mediaSource,subtitlesSource);

            concatenatingMediaSource.addMediaSource(mergingMediaSource);
        }

        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, oldPosition);
        playError();
    }

    private void screenOrientationCheck(){
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            Bitmap bitmap;
            String path = mVideoFiles.get(position).getPath();
            Uri uri = Uri.parse(path);
            retriever.setDataSource(this, uri);
            bitmap = retriever.getFrameAtTime();
            int videoWidth = bitmap.getWidth();
            int videoHeight = bitmap.getHeight();
            if (videoWidth>videoHeight){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }else{
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }catch (Exception e){
            Log.e("MediaMetadataRetriever", "screenOrientation");
        }
    }

    private void playError() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, "Video playing error", Toast.LENGTH_SHORT).show();
            }
        });
        player.setPlayWhenReady(true);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.eqFrame);
        if (eqContainer.getVisibility() == View.GONE){
            super.onBackPressed();
        }else{
            if (fragment.isVisible() && eqContainer.getVisibility() == View.VISIBLE){
                eqContainer.setVisibility(View.GONE);
            }else{
                if (player != null){
                    player.release();
                }
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    private void setFullScreen(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.video_back:
                if (player != null){
                    player.release();
                }
                finish();
                break;
            case R.id.video_list:
                VideoPlaylist videoPlaylist = new VideoPlaylist(mVideoFiles, videoAdapter);
                videoPlaylist.show(getSupportFragmentManager(), videoPlaylist.getTag());
                break;
            case R.id.lock:
                controlsMode = ControlsMode.FULLSCREEN;
                root.setVisibility(View.VISIBLE);
                lock.setVisibility(View.INVISIBLE);
                break;
            case R.id.unlock:
                controlsMode = ControlsMode.LOCK;
                root.setVisibility(View.INVISIBLE);
                lock.setVisibility(View.VISIBLE);
                break;
            case R.id.exo_next:
                try {
                    player.stop();
                    position++;
                    playVideo();
                }catch (Exception e){
                    finish();
                }
                break;
            case R.id.exo_prev:
                try {
                    player.stop();
                    position--;
                    playVideo();
                }catch (Exception e){
                    finish();
                }
                break;
        }
    }

    View.OnClickListener firstListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.ic_fit);

            scaling.setOnClickListener(secondListener);
        }
    };

    View.OnClickListener secondListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.ic_zoom);

            scaling.setOnClickListener(thirdListener);
        }
    };

    View.OnClickListener thirdListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
             playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
             player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
             scaling.setImageResource(R.drawable.ic_fullscreen);
             scaling.setOnClickListener(firstListener);
        }
    };
}