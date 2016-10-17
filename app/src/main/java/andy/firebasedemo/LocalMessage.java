package andy.firebasedemo;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andyli on 2016/10/10.
 */
@IgnoreExtraProperties
public class LocalMessage {

    public String name;
    public String msg;
    public Double lat;
    public Double lon;
    public Long time;


    public LocalMessage() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public LocalMessage(String name, String msg, Double lat, Double lon, Long time) {
        this.name = name;
        this.msg = msg;
        this.lat = lat;
        this.lon = lon;
        this.time = time;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("msg", msg);
        result.put("lat", lat);
        result.put("lon", lon);
        result.put("time", time);
        return result;
    }


}
