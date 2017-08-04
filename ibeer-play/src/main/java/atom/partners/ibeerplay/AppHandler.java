package atom.partners.ibeerplay;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.id;

public class AppHandler extends FileDownloader.Download implements Handler.Callback, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {
    public final MainActivity ma;
    public static AppHandler H;
    private Toast toast;
    private List<Playlist> playlists = new ArrayList();
    private List<Uri> playlist = new ArrayList();
    private int current = 0;
    final SharedPreferences prefs;
    public final static String host = "ibeer-v2.eu-central-1.elasticbeanstalk.com";
    public final static String url = "http://" + host + "/Admin/DevicePlaylist?deviceId=";
    protected static Handler handler;
    final static int Update = 1;
    private int count;
    int playerId;
    private final static int PlayerId = -1;
   // private final static int PlayerId = 52;

    AppHandler(MainActivity ma) {
        H = this;
        this.ma = ma;
        file = new File(ma.getApplicationContext().getFilesDir(), "playlist.json");
        prefs = ma.getSharedPreferences("AppHandler", Context.MODE_PRIVATE);
        handler = new Handler(this);
        playerId = prefs.getInt("playerId", PlayerId);
        // playerId = 1;
        if (playerId > 0) {
            ready(true); // load playlist from file
            start();
        } else{
            new GetPlayerId();

        }

            }



    void setDeviceId(String id) {
        Editor e = prefs.edit();
        e.putString("deviceId", id);
        e.commit();
    }

    public String getDeviceId() {
        String s = prefs.getString("deviceId", null);
        if (s == null) new GetPlayerId();
        return s;
    }

    void start() {
            switch (NetUtil.getNetType()) {
                case NetUtil.TypeNo:
                    showToast("No network connection");
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    showToast("Using mobile network connection!");
                    getPlaylist();
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    getPlaylist();
                    break;
                default:
                    showToast("Network not used type = " + NetUtil.getNetName());
            }
            runTimer();
    }

    public void showToast(String msg) {
        if (toast == null) {
            toast = Toast.makeText(ma, msg, Toast.LENGTH_LONG);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    public void showToast(Exception e) {
        e.printStackTrace();
        String msg = e.getMessage();
        if (msg == null)
            msg = e.toString();
        showToast(msg);
    }

    public void showToast(int id) {
        showToast(ma.getResources().getText(id).toString());
    }


    public void setConfig(int id) {
        playerId = id;
        Editor e = prefs.edit();
        e.putInt("playerId", playerId);
        e.commit();
        start();
    }
/*
    void settings() {
        Intent i = new Intent(ma.getApplicationContext(), SettingActivity.class);
        i.putExtra("id", getDeviceId());
        i.putExtra("id", playerId);
        ma.startActivity(i);
    }
    */


    public boolean handleMessage(Message msg) {
        boolean r = false;
        switch (msg.what) {
            case Update:
                r = true;
                runTimer();
                break;
        }
        return r;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        playlist.remove(current);
        if (current > 0) current--;
        return false;
    }

    @Override
    void ready(boolean ok) {
        if (ok && file.exists()) {
            try {
                playlists.clear();
                JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                reader.beginObject();
                while (reader.hasNext()) {
                    switch (reader.nextName()) {
                        case "device_id":
                            playerId = reader.nextInt();
                            break;
                        case "playlists":
                            reader.beginArray();
                            while (reader.hasNext()) playlists.add(new Playlist(reader));
                            reader.endArray();
                            break;
                        default:
                            reader.skipValue();
                    }
                }
                updatePlaylist();
            } catch (Exception e) {
                showToast(e);
            }
        }
    }

    public void getPlaylist() {
        if (!loading && playerId > 0) {
            switch (NetUtil.getNetType()) {
                case ConnectivityManager.TYPE_MOBILE:
                case ConnectivityManager.TYPE_WIFI:
                    count = 0;
                    new FileDownloader(this).execute(url + Integer.toString(playerId), true);
                    break;
                default:
                    //    showToast("Network not used type = " + NetUtil.getNetName());
            }
        }
    }
    /*
    public static void toast(Object ob) {
		handler.sendMessage(Message.obtain(handler, TOAST, ob));
	}
    */

    void updatePlaylist() {
        Date date = new Date();
        List<Uri> list = new ArrayList();
       boolean changed = false;
        for (int i = 0; i < playlists.size(); ++i) {
            Playlist playlist = playlists.get(i);
            if (playlist.start.before(date) && playlist.end.after(date)) {
                for (int n = 0; n < playlist.videos.size(); ++n) {
                    Video video = playlist.videos.get(n);
                    if (video.isReady()) {
                        if (!this.playlist.contains(video.src())) changed = true;
                        list.add(video.src());
                    }
                }
            }
        }
        if (playlist.size() != list.size()) changed = true;
        if (changed) {
            if (current > list.size() - 1) current = 0;
            playlist = list;
            if (!ma.video.isPlaying() && !list.isEmpty()) {
               //ma.video.setVideoPath(list.get(current).getAbsolutePath());
                ma.video.setVideoURI(playlist.get(current));
            }
        }
    }


    void runTimer() {
        if (++count > 3) getPlaylist();
        else updatePlaylist();
        handler.sendEmptyMessageDelayed(Update, 60000);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //mp.setLooping(true);
        // mp.setWakeMode(ma, PowerManager.PARTIAL_WAKE_LOCK);
        mp.setScreenOnWhilePlaying(true);
        mp.setOnVideoSizeChangedListener(ma.video);
        ma.video.start();

        /*
        if (Build.VERSION.SDK_INT >= 16) {
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        }
        */
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // start next
        if (!playlist.isEmpty()) {
            if (current < playlist.size() - 1) current++;
            else current = 0;
           // ma.video.setVideoPath(playlist.get(current).getAbsolutePath());
            ma.video.setVideoURI(playlist.get(current));
        }
    }

    protected void onDestroy() {
        for (int i = 0; i < playlists.size(); i++) playlists.get(i).onDestroy();
    }

}
