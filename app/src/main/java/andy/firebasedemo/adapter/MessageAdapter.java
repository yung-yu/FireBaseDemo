package andy.firebasedemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebViewDatabase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import andy.firebasedemo.Message;

/**
 * Created by andyli on 2016/10/18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> implements ValueEventListener{
    private Context context;
    private DatabaseReference database;
    private List<Message> data = new ArrayList<>();
    private DatabaseReference userDatabase;

    public MessageAdapter(Context context) {
        this.context = context;
        database =  FirebaseDatabase.getInstance().getReference("messages");
    }
    public void startLisetener(){
        database.orderByKey().limitToLast(100).addValueEventListener(this);
    }
    public void stopLisetener(){
        database.removeEventListener(this);
    }
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null));
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
         Message msg = data.get(position);
         if(msg != null){
             holder.text1.setTextColor(Color.WHITE);
             holder.text1.setText(msg.name+":"+msg.msg);
         }
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() :0;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if(dataSnapshot.getChildrenCount() > 0){
            for (DataSnapshot item: dataSnapshot.getChildren()) {
                Message msg =  item.getValue(Message.class);
                data.add(0,msg);
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
