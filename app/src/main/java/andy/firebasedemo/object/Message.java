package andy.firebasedemo.object;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andyli on 2016/10/10.
 *
 */
@IgnoreExtraProperties
public class Message {
    public String fromId;
    public String text;
    public long time;
    public String id;
    public String type;
    public String downloadUrl;


    public Message() {

    }

    public Message(String fromId, String text, long time) {
        this.fromId = fromId;
        this.text = text;
        this.time = time;
        this.type = MessageType.text.name();
    }

    public Message(String fromId, String type, String downloadUrl, long time) {
        this.fromId = fromId;
        this.type = type;
        this.downloadUrl = downloadUrl;
        this.time = time;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("fromId", fromId);
        result.put("type", type);
        result.put("text", text);
        result.put("downloadUrl", downloadUrl);
        result.put("time", time);

        return result;
    }

}
