package com.example.topplayer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder>
{
    private ArrayList<VideoFiles> videoFiles;
    private ArrayList<String> folderPath;
    private Context context;

    public VideoListAdapter(ArrayList<VideoFiles> videoFiles, ArrayList<String> folderPath, Context context) {
        this.videoFiles = videoFiles;
        this.folderPath = folderPath;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.foulder_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int indexPath = folderPath.get(position).lastIndexOf("/");
        String nameOfFolder = folderPath.get(position).substring(indexPath+1);

        holder.folder_name.setText(nameOfFolder);
        holder.folder_path.setText(folderPath.get(position));
        holder.countOfFiles.setText(countOfFiles(folderPath.get(position)) + " videos");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoListActivity.class);
                intent.putExtra("folderName", nameOfFolder);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return folderPath.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView folder_name, folder_path, countOfFiles;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            folder_name = itemView.findViewById(R.id.folderName);
            folder_path = itemView.findViewById(R.id.folderPath);
            countOfFiles = itemView.findViewById(R.id.countOfFiles);
        }
    }

    int countOfFiles(String folder_name){
        int files_count = 0;
        for (VideoFiles videoFiles : videoFiles){
            if (videoFiles.getPath().substring(0, videoFiles.getPath().lastIndexOf("/")).endsWith(folder_name)){
                files_count++;
            }
        }
        return files_count;
    }
}
