package com.example.somnath.mymusic;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;

import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;


public class MyMusicService extends Service {

    static public final int STOPED = -1, PAUSED = 0, PLAYING = 1;
    private MediaPlayer mediaPlayer;
    private ArrayList<Song> tracklist;
    private int status;
    private int currentTrackPosition;
    private BroadcastReceiver receiver;
    private boolean taken;
    public boolean complete = false;
    private IBinder playerBinder;
    private Context context;
    private static final int NOTIF_ID = 1234;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private RemoteViews mRemoteviews;
    private Notification mNotification;

    public class LocalBinder extends Binder {
        public MyMusicService getService() {
            return MyMusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        mediaPlayer = new MediaPlayer();
        tracklist = new ArrayList<Song>();
        setStatus(STOPED);
        playerBinder = new LocalBinder();

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                complete = false;
            }
        });
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                // if (currentTrackPosition == tracklist.size()-1) {
                //    playTrack(0);

                // } else
                {
                    nextTrack();
                    updateNotification();
                    complete = true;
                }
            }
        });
        restoreTracklist();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("play");
        filter.addAction("pause");
        filter.addAction("next");
        filter.addAction("previous");
        filter.addAction("clear");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //do something based on the intent's action


                switch (intent.getAction()) {
                    case "play":
                        play();
                        updateNotification();
                        break;
                    case "pause":
                        play();
                        updateNotification();
                        break;
                    case "next":
                        nextTrack();
                        updateNotification();
                        break;
                    case "previous":
                        prevTrack();
                        updateNotification();
                        break;
                    case "clear":
                        pause();
                        mNotificationManager.cancel(NOTIF_ID);
                        stopForeground(true);
                        break;
                }


            }
        };
        registerReceiver(receiver, filter);


        return playerBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    public boolean updateOnCompleteview() {
        return true;
    }

    public void take() {
        taken = true;
    }

    private void untake() {
        synchronized (this) {
            taken = false;
            notifyAll();
        }
    }


    public boolean isTaken() {
        return taken;
    }

    private void setStatus(int s) {
        status = s;
    }

    public int getStatus() {
        return status;
    }

    public ArrayList<Song> getTracklist() {
        return tracklist;
    }

    public Song getTrack(int pos) {
        return tracklist.get(pos);
    }

    public void addTrack(Song track) {
        tracklist.add(track);
        untake();
    }

    public void addTrack(long id, String name) {
        tracklist.add(new Song(id, name));
        untake();
    }

    public String getCurrentTrack() {
        if (currentTrackPosition < 0) {
            return null;
        } else {
            return tracklist.get(currentTrackPosition).getTitle();
        }
    }

    public void setCurrentTrackPosition(int position) {
        currentTrackPosition = position;

    }

    public int getCurrentTrackPosition() {
        return currentTrackPosition;
    }


    public void removeTrack(int pos) {
        if (pos == currentTrackPosition) {
            stop();
        }
        if (pos < currentTrackPosition) {
            currentTrackPosition--;
        }
        tracklist.remove(pos);
        untake();
    }

    public void clearTracklist() {
        if (status > STOPED) {
            stop();
        }
        tracklist.clear();
        untake();
    }

    //play using position in list using song_id
    public void playTrack(int pos) {
        if (status > STOPED) {
            stop();
        }
        Song song = tracklist.get(pos);
        long id = song.getId();

        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Toast.makeText(context, "Error Setting Data Source", Toast.LENGTH_SHORT).show();
        }
        try {
            mediaPlayer.prepare();
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error Setting Data Source", e);

        }
        mediaPlayer.start();

        currentTrackPosition = pos;

        DbNowplaying dbOpenHelper = new DbNowplaying(getApplicationContext());
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        ContentValues c = new ContentValues();
        c.put(DbNowplaying.CURR_SONG, currentTrackPosition);




        db.update(DbNowplaying.TABLE2, c, DbNowplaying.CURR_ID + "=" + 1, null);
        dbOpenHelper.close();


        setStatus(PLAYING);
        untake();
    }


    //play according to status
    public void play() {
        switch (status) {
            case STOPED:
                if (!tracklist.isEmpty()) {
                    playTrack(currentTrackPosition);
                }
                break;
            case PLAYING:
                mediaPlayer.pause();
                setStatus(PAUSED);
                break;
            case PAUSED:
                mediaPlayer.start();
                setStatus(PLAYING);
                break;
        }
        untake();
    }

    public void pause() {
        mediaPlayer.pause();
        setStatus(PAUSED);
        untake();
    }

    public void stop() {
        mediaPlayer.stop();
        mediaPlayer.reset();
        currentTrackPosition = -1;
        setStatus(STOPED);
        untake();
    }

    public void nextTrack() {
        if (currentTrackPosition < tracklist.size() - 1) {
            playTrack(currentTrackPosition + 1);
        }
    }

    public void prevTrack() {
        if (currentTrackPosition > 0) {
            playTrack(currentTrackPosition - 1);
        }
    }

    public int getCurrentTrackProgress() {
        if (status > STOPED) {
            return mediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    public int getCurrentTrackDuration() {
        if (status > STOPED) {
            return mediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    public String getTrackDurationFromList() {
        return tracklist.get(getCurrentTrackPosition()).getDduration();
    }

    public void seekTrack(int p) {
        if (status > STOPED) {
            mediaPlayer.seekTo(p);
            untake();
        }
    }

    public MediaPlayer mediaPlayer() {
        return mediaPlayer;
    }

    public void storeTracklist(ArrayList<Song> tracklist) {
        DbNowplaying dbOpenHelper = new DbNowplaying(getApplicationContext());
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.onUpgrade(db, 1, 1);
        for (int i = 0; i < tracklist.size(); i++) {
            ContentValues c = new ContentValues();
            c.put(DbNowplaying.ID_SONG, tracklist.get(i).getId());
            c.put(DbNowplaying.KEY_FILE, tracklist.get(i).getTitle());
            c.put(DbNowplaying.IMAGE, tracklist.get(i).getImg_Id());
            c.put(DbNowplaying.KEY_ARTIST, tracklist.get(i).getArtist());
            c.put(DbNowplaying.KEY_ALBUMS, tracklist.get(i).getAlbums());
            c.put(DbNowplaying.KEY_GENRES, tracklist.get(i).getGenres());

            db.insert(DbNowplaying.TABLE_NAME, null, c);
        }
        currentTrackPosition = 0;
        ContentValues currsonsg = new ContentValues();
        currsonsg.put(DbNowplaying.CURR_ID, 1);
        currsonsg.put(DbNowplaying.CURR_SONG, currentTrackPosition);
        db.insert(DbNowplaying.TABLE2, null, currsonsg);

        dbOpenHelper.close();
        restoreTracklist();

    }

    public void restoreTracklist() {
        DbNowplaying dbOpenHelper = new DbNowplaying(getApplicationContext());
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor c = db.query(DbNowplaying.TABLE_NAME, null, null, null, null, null, null);
        tracklist.clear();
        while (c.moveToNext()) {
            tracklist.add(new Song(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5)));
        }
        dbOpenHelper.close();
        currentTrackPosition = getCurrentTrackPosition();
        c.close();

    }

    public void CustomNotification() {
        // Using mRemoteviews to bind custom layouts into Notification


        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mRemoteviews = new RemoteViews(getPackageName(), R.layout.notification_view);
        // Open NotificationView Class on Notification Click
        Intent intent = new Intent(this, MainActivity.class);

        // Open NotificationView.java Activity
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(this)
                // Set Icon
                .setSmallIcon(R.drawable.ic_play_circle_outline_black_24dp)
                .setContentTitle("Now Playing")                // Set Ticker Message
                // Dismiss Notification
                .setAutoCancel(true)
                // Set PendingIntent into Notification
                .setContentIntent(pIntent)
                // Set mRemoteviews into Notification
                .setContent(mRemoteviews);


        Intent previousclick = new Intent("previous");
        Intent nextclick = new Intent("next");
        Intent play = new Intent("play");
        Intent pause = new Intent("pause");
        Intent clear = new Intent("clear");


        PendingIntent pre = PendingIntent.getBroadcast(this, 0, previousclick, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent next = PendingIntent.getBroadcast(this, 0, nextclick, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent playPause = PendingIntent.getBroadcast(this, 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent Pause = PendingIntent.getBroadcast(this, 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent Clear = PendingIntent.getBroadcast(this, 0, clear, PendingIntent.FLAG_UPDATE_CURRENT);


        mRemoteviews.setOnClickPendingIntent(R.id.previous_not, pre);
        mRemoteviews.setOnClickPendingIntent(R.id.playPause_not, playPause);
        mRemoteviews.setOnClickPendingIntent(R.id.next_not, next);
        mRemoteviews.setOnClickPendingIntent(R.id.pause_noti, Pause);
        mRemoteviews.setOnClickPendingIntent(R.id.clear_not, Clear);


        mRemoteviews.setViewVisibility(R.id.pause_noti, View.GONE);


        startForeground(NOTIF_ID, mBuilder.build());

    }

    // use this method to update the Notification's UI
    public void updateNotification() {
        int api = Build.VERSION.SDK_INT;

        if (getTracklist().size() > 0) {


            mRemoteviews.setTextViewText(R.id.notification_title, getTracklist().get(getCurrentTrackPosition()).getTitle());
            String image = getTracklist().get(getCurrentTrackPosition()).getImg_Id();
            Bitmap bm = BitmapFactory.decodeFile(image);
            mRemoteviews.setImageViewBitmap(R.id.image_noti, bm);

        }

        if (getStatus() == 1) {
            mRemoteviews.setViewVisibility(R.id.pause_noti, View.VISIBLE);
            mRemoteviews.setViewVisibility(R.id.playPause_not, View.GONE);
        } else {
            mRemoteviews.setViewVisibility(R.id.pause_noti, View.GONE);
            mRemoteviews.setViewVisibility(R.id.playPause_not, View.VISIBLE);
        }

        if (api < Build.VERSION_CODES.HONEYCOMB) {
            mNotificationManager.notify(NOTIF_ID, mNotification);
        } else if (api >= Build.VERSION_CODES.HONEYCOMB) {
            mNotificationManager.notify(NOTIF_ID, mBuilder.build());
        }


    }

    @Override
    @TargetApi(24)
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        stopForeground(STOP_FOREGROUND_REMOVE);

    }

}