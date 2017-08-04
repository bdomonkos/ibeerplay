package atom.partners.ibeerplay;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static atom.partners.ibeerplay.AppHandler.H;


class GetPlayerId extends AsyncTask<String, String, Exception> {
    private int playerId;

    public GetPlayerId() {
        execute(getDeviceId());
    }

    private static String getDeviceId() {
        //  return "web2";
        TelephonyManager tm = (TelephonyManager) H.ma.getSystemService(Context.TELEPHONY_SERVICE);
        String id = tm.getDeviceId();
        H.showToast("TELEPHONY_SERVICE=" + id);
        int i;
        if (id != null && id.length() > 2) {
            try {
                i = Integer.parseInt(id);
            } catch (Exception e) {
                i = 1;
            }
        } else i = 0;
        if (i == 0) {
            id = Settings.Secure.getString(H.ma.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            // String imei = SystemProperties.get("ro.gsm.imei")
            H.showToast("ANDROID_ID=" + id);
        }
        H.setDeviceId(id);
        return id;
    }

    @Override
    protected Exception doInBackground(String[] params) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("http://" + AppHandler.host + "/api/identify/?externalId=" + params[0]).openConnection();
            conn.setRequestMethod("POST");
            //  conn.setRequestProperty("User-Agent", "Mozilla /5.0 (linux-gnu)");
            conn.setRequestProperty("Accept", "application/json; charset=utf-8");
            // conn.setUseCaches(true);
            conn.setAllowUserInteraction(false);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(9000);
            conn.connect();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                conn.disconnect();
                return new IOException(conn.getResponseMessage() + " " + conn.getURL().toString());
            }
            JsonReader reader = new JsonReader(new InputStreamReader(conn.getInputStream()));
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "Id":
                        playerId = reader.nextInt();
                        break;
                    default:
                        reader.skipValue();
                }
            }
            //reader.endObject();
            reader.close();
            conn.disconnect();
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Exception e) {
        if (e != null) H.showToast(e);
        else switch (playerId) {
            case 0:
                H.showToast("Invalid Player ID");
                break;
            case -1:
                H.showToast("Player not registered !");

                break;
            default:
                H.setConfig(playerId);
                H.showToast("Id=" + playerId + " Downloadin in progress..... ");
        }
    }
}
