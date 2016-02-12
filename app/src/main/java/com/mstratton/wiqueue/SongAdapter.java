package com.mstratton.wiqueue;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class SongAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<SongObject> songs;

    private TextView name;
    private TextView artist;
    private TextView album;
    private TextView time;

    public SongAdapter(Context context, ArrayList<SongObject> inSongs) {
        this.context = context;
        this.songs = inSongs;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = (View) inflater.inflate(R.layout.playlist_list_item, null);
        }

        name = (TextView)convertView.findViewById(R.id.name);
        artist = (TextView)convertView.findViewById(R.id.artist);
        album = (TextView)convertView.findViewById(R.id.album);
        time = (TextView)convertView.findViewById(R.id.time);

        name.setText(songs.get(position).getName());
        artist.setText(songs.get(position).getArtist());
        album.setText(songs.get(position).getAlbum());
        time.setText(songs.get(position).getTime());

        return convertView;
    }
}