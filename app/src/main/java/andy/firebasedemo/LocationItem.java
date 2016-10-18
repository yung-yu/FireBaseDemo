package andy.firebasedemo;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andyli on 2016/10/18.
 */

@IgnoreExtraProperties
public class LocationItem {
    public double lat;
    public double lon;
    public long time;


    public LocationItem() {

    }

    public LocationItem(double lat, double lon, long time) {
        this.lat = lat;
        this.lon = lon;
        this.time = time;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("lat", lat);
        result.put("lon", lon);
        result.put("time", time);

        return result;
    }


}
