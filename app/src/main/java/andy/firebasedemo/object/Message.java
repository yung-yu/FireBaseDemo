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
    public String fromId;
    public String text;
    public long time;
    public String id;

    public Message() {

    }

    public Message(String fromId, String text, long time) {
        this.fromId = fromId;
        this.text = text;
        this.time = time;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("fromId", fromId);
        result.put("text", text);
        result.put("time", time);
        return result;
    }

}
