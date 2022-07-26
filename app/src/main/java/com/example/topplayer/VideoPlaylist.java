package com.example.topplayer;

import static com.example.topplayer.VideoListActivity.MY_PREF;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class VideoPlaylist extends BottomSheetDialogFragment {

    ArrayList<VideoFiles> arrayList = new ArrayList<>();
    VideoAdapter videoAdapter;
    BottomSheetDialog bottomSheetDialog;
    RecyclerView recyclerView;
    TextView folder;
    String sortOrder;

    public VideoPlaylist(ArrayList<VideoFiles> arrayList, VideoAdapter videoAdapter) {
        this.arrayList = arrayList;
        this.videoAdapter = videoAdapter;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.playlist_bs_layout, null);
        bottomSheetDialog.setContentView(view);

        recyclerView = view.findViewById(R.id.playlist_rv);
        folder = view.findViewById(R.id.playlist_folder_name);

        SharedPreferences preferences = this.getActivity().getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);
        String folderName = preferences.getString("playlist_folder_name", "error");
        folder.setText(folderName);

        arrayList = fetchMedia(folderName);
        videoAdapter = new VideoAdapter(arrayList, getContext(), 1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(videoAdapter);
        videoAdapter.notifyDataSetChanged();

        return bottomSheetDialog;
    }

    private ArrayList<VideoFiles> fetchMedia(String folderName) {
        ArrayList<VideoFiles> videos = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        sortOrder = MediaStore.MediaColumns.DISPLAY_NAME+" ASC";
        String selection = MediaStore.Video.Media.DATA+" like?";
        String[] selectionArg = new String[]{"%" + folderName + "%"};
        Cursor cursor = getContext().getContentResolver().query(uri, null, selection, selectionArg, sortOrder);
        if (cursor != null && cursor.moveToNext()){
            do {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                @SuppressLint("Range") String display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                @SuppressLint("Range") String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                @SuppressLint("Range") String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                @SuppressLint("Range") String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                VideoFiles videoFiles = new VideoFiles(id, title, display_name, size, duration,path, dateAdded);

                videos.add(videoFiles);
            }while (cursor.moveToNext());
        }
        return videos;
    }
}
