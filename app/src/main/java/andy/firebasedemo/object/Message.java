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
    public String uid;
    public String msg;
    public long time;

    public Message() {

    }

    public Message(String uid, String name, String msg, long time) {
        this.uid = uid;
        this.name = name;
        this.msg = msg;
        this.time = time;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("msg", msg);
        result.put("time", time);
        return result;
    }


}
