package andy.firebasedemo.object;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andyli on 2016/10/19.
 */
@IgnoreExtraProperties
public class Member {
    public String name;
    public String icon;
    public double lat;
    public double lot;
    public long time;
    public String token;

    public Member() {
    }

    public Member(String name, String icon, double lat, double lot, long time, String token) {
        this.name = name;
        this.icon = icon;
        this.lat = lat;
        this.lot = lot;
        this.time = time;
        this.token = token;
    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("icon", icon);
        result.put("lat", lat);
        result.put("lot", lot);
        result.put("time", time);
        result.put("token", token);
        return result;
    }


}
