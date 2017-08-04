package atom.partners.ibeerplay;

import android.util.JsonReader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/*
{"device_id":1,"playlists":
[{"id":17,"startdate":"2016-10-31","enddate":"2016-11-13","starttime":"00:01","endtime":"23:01","landscape":true,"videos":[{"id":21,"name":"fornetti3-image","url":"https://ibeerupload.s3.eu-central-1.amazonaws.com/videos/22cc8c83fd912ac6081c482c8cd6584d","hash":"22cc8c83fd912ac6081c482c8cd6584d","extension":".mp4"},{"id":19,"name":"fornetti1-slideshow","url":"https://ibeerupload.s3.eu-central-1.amazonaws.com/videos/f5140989d70c7017c4c22e8a50f874f2","hash":"f5140989d70c7017c4c22e8a50f874f2","extension":".mp4"},{"id":20,"name":"fornetti2-keszites","url":"https://ibeerupload.s3.eu-central-1.amazonaws.com/videos/e0f5e1d9d84ea66afc8156ba7368d4da","hash":"e0f5e1d9d84ea66afc8156ba7368d4da","extension":".mp4"},{"id":19,"name":"fornetti1-slideshow","url":"https://ibeerupload.s3.eu-central-1.amazonaws.com/videos/f5140989d70c7017c4c22e8a50f874f2","hash":"f5140989d70c7017c4c22e8a50f874f2","extension":".mp4"}]}]}
*/

public class Playlist {
    Date start, end;
    int id;
    final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd,hh:mm");
    List<Video> videos = new ArrayList();

    Playlist(JsonReader reader) throws Exception {
        String startdate = null, starttime = null, enddate = null, endtime = null;
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "id":
                    id = reader.nextInt();
                    break;
                case "startdate":
                    startdate = reader.nextString();
                    break;
                case "starttime":
                    starttime = reader.nextString();
                    break;
                case "enddate":
                    enddate = reader.nextString();
                    break;
                case "endtime":
                    endtime = reader.nextString();
                    break;
                case "videos":
                    reader.beginArray();
                    while (reader.hasNext()) videos.add(new Video(reader));
                    reader.endArray();
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        if (startdate == null || starttime == null || enddate == null || endtime == null || videos.isEmpty())
            throw new Exception("Invalid Playlist");
        start = format.parse(startdate + "," + starttime);
        end = format.parse(enddate + "," + endtime);
    }

    protected void onDestroy() {
        for (int i = 0; i < videos.size(); i++) videos.get(i).onDestroy();
    }
}

