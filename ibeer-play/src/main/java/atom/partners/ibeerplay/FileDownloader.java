package atom.partners.ibeerplay;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileDownloader extends AsyncTask<Object, Object, Exception> {
    static public abstract class Download {
        File file;
        boolean loading = false;

        abstract void ready(boolean ok);
    }

    private Download parent;


    public FileDownloader(Download parent) {
        this.parent = parent;
    }

    @Override
    protected void onPreExecute() {
        parent.loading = true;
    }

    @Override
    protected Exception doInBackground(Object... o) {
        boolean json = o.length > 1;
        try {
            Log.d("FileDownloader start", o[0].toString());
            HttpURLConnection conn = (HttpURLConnection) new URL(o[0].toString()).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla /5.0 (linux-gnu)");
            if (json) conn.setRequestProperty("Accept", "application/json; charset=utf-8");
            conn.setUseCaches(true);
            conn.setAllowUserInteraction(false);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(9000);
            conn.connect();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                conn.disconnect();
                return new IOException(conn.getResponseMessage() + " " + conn.getURL().toString());
            }
            DataInputStream in = new DataInputStream(conn.getInputStream());
            byte[] buffer = new byte[1024];
            int length;
            FileOutputStream out = new FileOutputStream(parent.file);
            while ((length = in.read(buffer)) > -1) {
                if (length == 0) Thread.sleep(256);
                else out.write(buffer, 0, length);
            }
            out.close();
            in.close();
            // conn.disconnect();
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Exception e) {
        //  Log.d("FileDownloader end", parent.file.getAbsolutePath());
        if (e != null) AppHandler.H.showToast(e);
        parent.ready(e == null);
        parent.loading = false;
    }


    public static String calculateMD5(File file) {
        try {
            Process process = Runtime.getRuntime().exec("md5 " + file.getAbsolutePath());
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return inputStream.readLine().split(" ")[0];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void logStream(String name, InputStream in) throws IOException {
        byte[] b = new byte[4000];
        int i = in.read(b);
        in.close();
        Log.d(name, new String(b, 0, i, "UTF-8"));
    }


}
