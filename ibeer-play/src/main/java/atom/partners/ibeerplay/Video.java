package atom.partners.ibeerplay;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.JsonReader;

import java.io.File;

import static atom.partners.ibeerplay.AppHandler.H;


public class Video extends FileDownloader.Download {
    int id;
    private boolean ready;
    private String hash, extension, url;

    Video(JsonReader reader) throws Exception {
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "id":
                    id = reader.nextInt();
                    break;
                case "extension":
                    extension = reader.nextString();
                    break;
                case "hash":
                    hash = reader.nextString();
                    break;
                case "url":
                    url = reader.nextString();
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        if (hash == null || url == null || extension == null) throw new Exception("Invalid Video in Playlist");
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), hash + extension);
        ready = H.prefs.getBoolean(hash, false);
        // if (file.exists())  hash = FileDownloader.calculateMD5(file);
    }


    public boolean isReady() {
        if (!ready && !loading) {
            switch (NetUtil.getNetType()) {
                case ConnectivityManager.TYPE_MOBILE:
                   //  new FileDownloader(this).execute(url);
                    H.showToast("No video downloading on Mobile Internet");
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    new FileDownloader(this).execute(url);
                    break;
                default:
                    //    showToast("Network not used type = " + NetUtil.getNetName());
            }
        }
        return ready;
    }

    public Uri src() {
         return FileProvider.getUriForFile(AppHandler.H.ma, BuildConfig.APPLICATION_ID, file);
    }

    @Override
    void ready(boolean ok) {
        if (ok) {
            ready = true;
            SharedPreferences.Editor e = H.prefs.edit();
            e.putBoolean(hash, true);
            e.commit();
            H.updatePlaylist();
        } else H.showToast("File download failed " + file.getName());
    }


    protected void onDestroy() {
        if (loading) file.delete();
    }


}
