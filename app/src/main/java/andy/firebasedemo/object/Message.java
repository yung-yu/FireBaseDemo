package andy.firebasedemo.object;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andyli on 2016/10/10.
 */
@IgnoreExtraProperties
public class Message {
    public String name;
    public String fromId;
    public String icon;
    public String msg;
    public long time;
    public String id;

    public Message() {

    }

    public Message(String fromId, String name, String icon, String msg, long time) {
        this.fromId = fromId;
        this.name = name;
        this.icon = icon;
        this.msg = msg;
        this.time = time;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("fromId", fromId);
        result.put("name", name);
        result.put("msg", msg);
        result.put("time", time);
        return result;
    }

}
