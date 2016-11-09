package andy.firebasedemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import andy.firebasedemo.object.Message;


/**
 * Created by andyli on 2016/11/7.
 */

public class MsgAdapter extends BaseAdapter {
	private Context context;
	private List<Message> data = new ArrayList<>();


	public void setData(List<Message> data) {
		this.data.clear();
		this.data.addAll(data);
	}

	public MsgAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Message getItem(int i) {
		return i < data.size() ? data.get(i):null;
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		TextView text;
		if(view == null){
			view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null);
			text = (TextView) view.findViewById(android.R.id.text1);
			view.setTag(text);
		}else{
			text = (TextView) view.getTag();
		}
		Message message = getItem(i);
		text.setTextColor(Color.BLACK);
		if(message != null){
			text.setText(message.name+":"+message.msg);
		}
		return view;
	}
}
