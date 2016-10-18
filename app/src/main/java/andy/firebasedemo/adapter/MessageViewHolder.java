package andy.firebasedemo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import andy.firebasedemo.R;

/**
 * Created by andyli on 2016/10/18.
 */

public class MessageViewHolder extends RecyclerView.ViewHolder {
    public TextView text1;
    public TextView text2;
    public MessageViewHolder(View itemView) {
        super(itemView);

        text1 = (TextView) itemView.findViewById(android.R.id.text1);
    }
}
