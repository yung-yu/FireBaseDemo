package andy.firebasedemo.chatroom;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import andy.firebasedemo.Log.L;
import andy.firebasedemo.R;
import andy.firebasedemo.adapter.ChatRoomMessageAdapter;
import andy.firebasedemo.manager.FireBaseManager;
import andy.firebasedemo.object.Message;

/**
 * Created by andyli on 2016/11/25.
 */

public class ChatRoomFragment extends Fragment implements ChatRoomContract.View{
	private final static String TAG = "ChatRoomFragment";
	private EditText editText;
	private Button button;
	private ListView listView;
	private ChatRoomPresenterImp mMessagePresenterImp;
	private ChatRoomMessageAdapter mMsgAdapter;
	private Context context;

	@Override
	public void onAttach(Context context) {
		L.i(TAG, "ChatRoomFragment");
		super.onAttach(context);
		this.context = context;
	}
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		L.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		L.i(TAG, "onCreateView");
		return inflater.inflate(R.layout.fragment_chatroom, null);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		L.i(TAG, "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
		editText = (EditText) view.findViewById(R.id.editText);
		button = (Button) view.findViewById(R.id.button);
		listView = (ListView) view.findViewById(R.id.listview);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mMessagePresenterImp.sendMessage(editText.getText().toString());
			}
		});
		mMsgAdapter = new ChatRoomMessageAdapter(context);
		listView.setAdapter(mMsgAdapter);
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				final Message msg = mMsgAdapter.getItem(i);
				FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
				if(user != null && msg.fromId.equals(user.getUid())) {
					new AlertDialog.Builder(context)
							.setMessage(R.string.app_name)
							.setMessage(R.string.delete_tip)
							.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {

									FireBaseManager.getInstance().deleteMessage(msg.id);
									dialogInterface.cancel();
								}
							})
							.setNegativeButton(R.string.cancel, null)
							.create().show();
				}
				return true;
			}
		});
		mMessagePresenterImp = new ChatRoomPresenterImp(context, this);
	}


	@Override
	public void onStart() {
		L.i(TAG, "onStart");
		super.onStart();
		if(mMessagePresenterImp!= null) {
			mMessagePresenterImp.start();
		}
	}

	@Override
	public void onResume() {
		L.i(TAG, "onResume");
		super.onResume();
	}

	@Override
	public void onPause() {
		L.i(TAG, "onPause");
		super.onPause();
	}

	@Override
	public void onStop() {
		L.i(TAG, "onStop");
		super.onStop();
		mMessagePresenterImp.stop();
	}

	@Override
	public void onDestroyView() {
		L.i(TAG, "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		L.i(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void sendMessageReady() {
		button.setEnabled(false);
	}

	@Override
	public void sendMessageSuccess() {
		button.setEnabled(true);
		editText.setText("");
	}

	@Override
	public void sendMessageFailed(String msg) {
		button.setEnabled(true);
		editText.setText("");
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNotify(List<Message> data) {
		if(mMsgAdapter != null) {
			mMsgAdapter.setData(data);
			mMsgAdapter.notifyDataSetChanged();
		}
		if(listView != null) {
			listView.setSelection(data.size());
		}
	}

	@Override
	public void onNotify() {
		if(mMsgAdapter != null) {
			mMsgAdapter.notifyDataSetChanged();
		}
	}
}
