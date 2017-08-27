package andy.firebasedemo.chatroom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import andy.firebasedemo.Log.L;
import andy.firebasedemo.MainActivity;
import andy.firebasedemo.R;
import andy.firebasedemo.adapter.ChatRoomMessageAdapter;
import andy.firebasedemo.auth.AuthContract;
import andy.firebasedemo.auth.AuthPresenterImp;
import andy.firebasedemo.helper.ToolbarUIHelper;
import andy.firebasedemo.login.LoginDialogFragment;
import andy.firebasedemo.manager.FireBaseManager;
import andy.firebasedemo.object.Message;
import andy.firebasedemo.object.MessageType;

/**
 * Created by andyli on 2016/11/25.
 */

public class ChatRoomFragment extends Fragment implements ChatRoomContract.View , AuthContract.View{
	private final static String TAG = "ChatRoomFragment";
	private final static int GET_PHOTO_REQUEST_CODE = 8888;
	private EditText editText;
	private Button button;
	private Button sendImage;
	private RecyclerView listView;
	private ChatRoomPresenterImp mMessagePresenterImp;
	private ChatRoomMessageAdapter mMsgAdapter;
	private Context context;
	private AnimatorSet mCurrentAnimator;
	private ImageView expandedImageView;
	private Toolbar mToolbar;


	private AuthPresenterImp mAuthPresenterImp;

	@Override
	public void onAttach(Context context) {
		L.i(TAG, "ChatRoomFragment");
		super.onAttach(context);
		this.context = context;
		mAuthPresenterImp = new AuthPresenterImp() ;
		mAuthPresenterImp.setAuthView(this);
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
		mToolbar = (Toolbar) view.findViewById(R.id.toolbar);

		ToolbarUIHelper.getInstance().init((ProgressBar) view.findViewById(R.id.toolbar_progress_bar),
				(TextView) view.findViewById(R.id.toolbar_text));
		setHasOptionsMenu(true);
		((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
		editText = (EditText) view.findViewById(R.id.editText);
		button = (Button) view.findViewById(R.id.button);
		sendImage = (Button) view.findViewById(R.id.sendImage);
		listView = (RecyclerView) view.findViewById(R.id.historyList);

		expandedImageView = (ImageView) view.findViewById(R.id.expanded_image);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mMessagePresenterImp.sendMessage(editText.getText().toString());
			}
		});
		sendImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				Intent it = new Intent(Intent.ACTION_GET_CONTENT);
				it.setType("image/*");
				startActivityForResult(it, GET_PHOTO_REQUEST_CODE);
			}
		});
		mMsgAdapter = new ChatRoomMessageAdapter(context);
		listView.setLayoutManager(new LinearLayoutManager(context));
		listView.setAdapter(mMsgAdapter);
		mMsgAdapter.setOnItemEventListener(new ChatRoomMessageAdapter.OnItemEventListener() {
			@Override
			public void onItemClick(View view, Message message) {
				FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
				if(message.type != null && message.type.equals(MessageType.Photo.name())){
					if(user != null && message.fromId.equals(user.getUid())){
						ImageView imageView = (ImageView) view.findViewById(R.id.rightPhoto);
						zoomImageFromThumb(imageView, expandedImageView, mMsgAdapter.getBitmap(message.downloadUrl));
					}else{
						ImageView imageView = (ImageView) view.findViewById(R.id.leftPhoto);
						zoomImageFromThumb(imageView, expandedImageView, mMsgAdapter.getBitmap(message.downloadUrl));
					}
				}
			}

			@Override
			public boolean onLongItemClick(View view, final Message message) {
				FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
				if (user != null && message.fromId.equals(user.getUid())) {
					new AlertDialog.Builder(context)
							.setMessage(R.string.app_name)
							.setMessage(R.string.delete_tip)
							.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {

									FireBaseManager.getInstance().deleteMessage(message.id);
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

    private void initToolBar(Toolbar mToolbar){
		mToolbar.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

			}
		});

	}


	@Override
	public void onStart() {
		L.i(TAG, "onStart");
		super.onStart();
		if (mMessagePresenterImp != null) {
			mMessagePresenterImp.start();
		}
		mAuthPresenterImp.start();
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
		mAuthPresenterImp.stop();
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
		if (requestCode == GET_PHOTO_REQUEST_CODE && data != null) {
			Uri uri = data.getData();
			mMessagePresenterImp.sendImage(uri);
		}

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
		if (mMsgAdapter != null) {
			mMsgAdapter.setData(data);
			mMsgAdapter.notifyDataSetChanged();
		}
		if (listView != null) {
			listView.scrollToPosition(data.size());
		}
	}

	@Override
	public void onNotify() {
		if (mMsgAdapter != null) {
			mMsgAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void sendImageSuccess() {

	}

	@Override
	public void onImageUploadProgress(int progress) {

	}

	@Override
	public void sendImageFailed(String msg) {

	}



	private void zoomImageFromThumb(final ImageView thumbView, final ImageView expandedImageView, Bitmap bmp) {

		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}


		expandedImageView.setImageBitmap(bmp);

		final Rect startBounds = new Rect();
		final Rect finalBounds = new Rect();
		final Point globalOffset = new Point();

		thumbView.getGlobalVisibleRect(startBounds);
		getView().findViewById(R.id.container)
				.getGlobalVisibleRect(finalBounds, globalOffset);
		startBounds.offset(-globalOffset.x, -globalOffset.y);
		finalBounds.offset(-globalOffset.x, -globalOffset.y);

		float startScale;
		if ((float) finalBounds.width() / finalBounds.height()
				> (float) startBounds.width() / startBounds.height()) {
			startScale = (float) startBounds.height() / finalBounds.height();
			float startWidth = startScale * finalBounds.width();
			float deltaWidth = (startWidth - startBounds.width()) / 2;
			startBounds.left -= deltaWidth;
			startBounds.right += deltaWidth;
		} else {
			startScale = (float) startBounds.width() / finalBounds.width();
			float startHeight = startScale * finalBounds.height();
			float deltaHeight = (startHeight - startBounds.height()) / 2;
			startBounds.top -= deltaHeight;
			startBounds.bottom += deltaHeight;
		}

		thumbView.setAlpha(0f);
		expandedImageView.setVisibility(View.VISIBLE);

		expandedImageView.setPivotX(0f);
		expandedImageView.setPivotY(0f);

		AnimatorSet set = new AnimatorSet();
		set.play(ObjectAnimator.ofFloat(expandedImageView, View.X,
						startBounds.left, finalBounds.left))
			.with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
						startBounds.top, finalBounds.top))
			.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
						startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
			View.SCALE_Y, startScale, 1f));
		set.setDuration(200);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mCurrentAnimator = null;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mCurrentAnimator = null;
			}
		});
		set.start();
		mCurrentAnimator = set;

		final float startScaleFinal = startScale;
		expandedImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mCurrentAnimator != null) {
					mCurrentAnimator.cancel();
				}
				AnimatorSet set = new AnimatorSet();
				set.play(ObjectAnimator
						.ofFloat(expandedImageView, View.X, startBounds.left))
						.with(ObjectAnimator
								.ofFloat(expandedImageView,
										View.Y, startBounds.top))
						.with(ObjectAnimator
								.ofFloat(expandedImageView,
										View.SCALE_X, startScaleFinal))
						.with(ObjectAnimator
								.ofFloat(expandedImageView,
										View.SCALE_Y, startScaleFinal));
				set.setDuration(200);
				set.setInterpolator(new DecelerateInterpolator());
				set.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						thumbView.setAlpha(1f);
						expandedImageView.setVisibility(View.GONE);
						mCurrentAnimator = null;
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						thumbView.setAlpha(1f);
						expandedImageView.setVisibility(View.GONE);
						mCurrentAnimator = null;
					}
				});
				set.start();
				mCurrentAnimator = set;
			}
		});
	}

	@Override
	public void onAuthSignOut() {
		((MainActivity)getActivity()).showLoginDialog();

	}

	@Override
	public void onLogin() {

	}


}
