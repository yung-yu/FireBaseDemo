package andy.firebasedemo.object;

import com.google.android.gms.maps.model.Marker;
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
    public long time;
    public String token;

    public Member() {
    }

    public Member(String name, String icon, long time, String token) {
        this.name = name;
        this.icon = icon;
        this.time = time;
        this.token = token;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("icon", icon);
        result.put("time", time);
        result.put("token", token);
        return result;
    }


}
