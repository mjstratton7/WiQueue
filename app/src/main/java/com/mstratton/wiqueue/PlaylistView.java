package com.mstratton.wiqueue;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PlaylistView extends AppCompatActivity {
    private static final String TAG = "PlaylistView";

    // UI
    private Toolbar toolBar;
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabPlay;

    private ArrayList<SongObject> allSongs;
    private ListView songList;
    private SongAdapter songsAdapter;

    private ArrayList<SongObject> playlistSongs;
    private ListView playList;
    private SongAdapter playlistAdapter;

    private MenuItem menuEdit;
    private boolean editPlaylist = false;
    private boolean addPlaylist = false;
    private TextView emptyHint;

    // Server Related
    private Network server;
    private boolean isHost;
    private String name;
    private String servername;
    private String password;

    // Android Media Store Related
    final String track_id = MediaStore.Audio.Media._ID;
    final String track_no = MediaStore.Audio.Media.TRACK;
    final String track_name = MediaStore.Audio.Media.TITLE;
    final String artist = MediaStore.Audio.Media.ARTIST;
    final String duration = MediaStore.Audio.Media.DURATION;
    final String album = MediaStore.Audio.Media.ALBUM;
    final String composer = MediaStore.Audio.Media.COMPOSER;
    final String year = MediaStore.Audio.Media.YEAR;
    final String path = MediaStore.Audio.Media.DATA;
    final String date_added = MediaStore.Audio.Media.DATE_ADDED;
    Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        // Determine if Hosting or Joining Server
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("type").contains("host")) {
                isHost = true;
                servername = extras.getString("name");
                password = extras.getString("password");

            } else if (extras.getString("type").contains("user")) {
                isHost = false;
                servername = extras.getString("servername");
                name = extras.getString("name");
                password = extras.getString("password");
            }
            if (!extras.getString("name").isEmpty()) {
                name = extras.getString("name");
            } else {
                name = "WiQueue";
            }
        }

        initUI();

        server = new Network(this);

        // Fill allSongs array with data from Host
        if (isHost) {
            // Get Music Local on Device, by accesing the android mediastore to populate list.
            // A Bit of help from below as this is TOTALLY NOT DOCUMENTED, THANKS GOOGLE. Mainly didn't know
            // column info and the exact way to query the service.
            // http://stackoverflow.com/questions/13568798/list-all-music-in-mediastore-with-the-paths

            //Some audio may be explicitly marked as not being music, prevent those from being shown
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

            // Create a projection of the data we want
            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA
            };

            // Create a cursor similiar to a sql database (as it is one)
            Cursor cursor = this.managedQuery(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    null);

            // Read each row from the cursor until we have all allSongs
            while(cursor.moveToNext()) {
                String id = cursor.getString(0);
                String title = cursor.getString(1);
                String artist = cursor.getString(2);
                String album =  cursor.getString(3);
                String duration = cursor.getString(4);
                String path = cursor.getString(5);

                allSongs.add(new SongObject(id, title, artist, album, duration, path));
            }

            // With All Songs Loaded, Start Server

            // Define thread for server
            final Thread serverThread = new Thread() {
                @Override
                public void run() {

                    server.host(servername, password);

                }
            };

            // Start the server thread
            serverThread.start();

        } else {
            // Not Host, Get Song Data from Host

            // Connect to Server
            // Define thread for server
            final Thread serverThread = new Thread() {
                @Override
                public void run() {

                    server.connect(servername, name, password);

                }
            };

            // Start the server thread
            serverThread.start();

        }

        // Array adapter, copy allSongs into songview with custom layout.
        songsAdapter = new SongAdapter(this, allSongs);
        songList.setAdapter(songsAdapter);

        songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View view, int myItemInt, long mylng) {

                // Trying to add a song, get info
                String selectedName = ((TextView) view.findViewById(R.id.name)).getText().toString();
                String selectedArtist = ((TextView) view.findViewById(R.id.artist)).getText().toString();

                // Find the song object with that info
                for (SongObject s : allSongs) {
                    if (s.getName().equals(selectedName) && s.getArtist().equals(selectedArtist)) {
                        SongObject selectedSong = s;

                        // Add that song object to the playlist
                        playlistSongs.add(selectedSong);
                    }
                }

                // Notify of Song Added
                Snackbar.make(view, "Song Added.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

        // Array adapter, copy allSongs into songview with custom layout.
        playlistAdapter = new SongAdapter(this, playlistSongs);
        playList.setAdapter(playlistAdapter);

        playList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View view, int myItemInt, long mylng) {

                if (editPlaylist) {
                    // Selected to delete

                    // Get background color of view, to see if already selected
                    int color = Color.TRANSPARENT;
                    Drawable background = view.getBackground();
                    if (background instanceof ColorDrawable)
                        color = ((ColorDrawable) background).getColor();

                    // Background check, if red switch to white, vice versa
                    if (color != 0x7ADC1F1F) {
                        view.setBackgroundColor(0x7ADC1F1F);
                    } else {
                        view.setBackgroundColor(Color.WHITE);
                    }

                } else {
                    // Trying to play song, get info
                    String selectedName = ((TextView) view.findViewById(R.id.name)).getText().toString();
                    String selectedArtist = ((TextView) view.findViewById(R.id.artist)).getText().toString();

                    // Find the song object with that info
                    for (SongObject s : playlistSongs) {
                        if (s.getName().equals(selectedName) && s.getArtist().equals(selectedArtist)) {
                            SongObject selectedSong = s;

                            // Play The Song by launching the user default music app
                            // and attaching the filepath and audio type to the intent.
                            Intent intent = new Intent();
                            intent.setAction(android.content.Intent.ACTION_VIEW);

                            File file = new File(selectedSong.getPath());

                            intent.setDataAndType(Uri.fromFile(file), "audio/*");
                            startActivity(intent);

                        }
                    }
                }
            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!addPlaylist) {
                    addPlaylist = true;

                    // Switch to All Songs View
                    playList.setVisibility(View.GONE);
                    songList.setVisibility(View.VISIBLE);

                    emptyHint.setVisibility(View.GONE);

                    fabAdd.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_check));

                    if (isHost)
                        fabPlay.setVisibility(View.GONE);

                } else if (addPlaylist) {
                    addPlaylist = false;

                    // Switch to Playlist View and Update Data
                    playlistAdapter.notifyDataSetChanged();
                    playList.setVisibility(View.VISIBLE);
                    songList.setVisibility(View.GONE);

                    // Playlist still empty, show hint
                    if (playlistSongs.size() == 0) {
                        emptyHint.setVisibility(View.VISIBLE);
                    }

                    fabAdd.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_add));

                    if (isHost)
                        fabPlay.setVisibility(View.VISIBLE);

                }
            }
        });

        fabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Notiy Playing Song
                Snackbar.make(view, "Playing Song.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                // Play The Song by launching the user default music app
                // and attaching the filepath and audio type to the intent.
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                File file = new File(playlistSongs.get(0).getPath());
                intent.setDataAndType(Uri.fromFile(file), "audio/*");
                startActivity(intent);

            }
        });
    }

    private void initUI() {
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        fabAdd = (FloatingActionButton) findViewById(R.id.fabadd);
        fabPlay = (FloatingActionButton) findViewById(R.id.fabplay);

        if (isHost) {
            getSupportActionBar().setTitle(name + " - Hosting");
            fabPlay.setVisibility(View.VISIBLE);
        } else {
            getSupportActionBar().setTitle(name + " - User");
            fabPlay.setVisibility(View.GONE);
        }

        allSongs = new ArrayList<SongObject>();
        songList = (ListView) findViewById(R.id.songlist);

        playlistSongs = new ArrayList<SongObject>();
        playList = (ListView) findViewById(R.id.playlist);

        emptyHint = (TextView) findViewById(R.id.empty_hint);
    }

    // Create Toolbar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (isHost) {
            getMenuInflater().inflate(R.menu.menu_playlistview, menu);

            // Required to Find Button in Instance of Menu
            menuEdit = (MenuItem) menu.findItem(R.id.edit);

        }

        return true;

    }

    // Toolbar Menu events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Edit button Action
        if (id == R.id.edit) {
            if (!editPlaylist) {
                editPlaylist = true;
                menuEdit.setIcon(R.drawable.ic_menu_save);

                if (isHost) {
                    fabPlay.setVisibility(View.GONE);
                }

            } else if (editPlaylist) {
                editPlaylist = false;
                menuEdit.setIcon(R.drawable.ic_menu_edit);

                for (int i = 0; i < playList.getCount(); i++) {
                    View view = playList.getChildAt(i);

                    // Get background color of view, to see if set for delete
                    int color = Color.TRANSPARENT;
                    Drawable background = view.getBackground();
                    if (background instanceof ColorDrawable)
                        color = ((ColorDrawable) background).getColor();

                    if (color == 0x7ADC1F1F) {
                        // Set for delete, get info
                        String selectedName = ((TextView) view.findViewById(R.id.name)).getText().toString();
                        String selectedArtist = ((TextView) view.findViewById(R.id.artist)).getText().toString();

                        // Find the song object with that info
                        for (SongObject s : playlistSongs) {
                            if (s.getName().equals(selectedName) && s.getArtist().equals(selectedArtist)) {
                                SongObject selectedSong = s;

                                // Remove selected song
                                playlistSongs.remove(selectedSong);

                            }
                        }
                    }
                }

                if (isHost) {
                    fabPlay.setVisibility(View.VISIBLE);
                }

                // Refresh data afterwards
                playlistAdapter.notifyDataSetChanged();

            }
        }

        return super.onOptionsItemSelected(item);
    }

}
