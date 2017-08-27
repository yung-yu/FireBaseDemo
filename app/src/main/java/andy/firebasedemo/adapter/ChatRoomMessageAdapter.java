package andy.firebasedemo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import andy.firebasedemo.R;
import andy.firebasedemo.manager.MemberManager;
import andy.firebasedemo.object.Member;
import andy.firebasedemo.object.Message;
import andy.firebasedemo.object.MessageType;
import andy.firebasedemo.util.TimeUtils;


/**
 * Created by andyli on 2016/11/7.
 */

public class ChatRoomMessageAdapter extends RecyclerView.Adapter<ChatRoomMessageAdapter.ViewHolder> {
	private final static String TAG = "ChatRoomMessageAdapter";
	private Context context;
	private List<Message> data = new ArrayList<>();
	private ImageLoader imageLoader;

	private  OnItemEventListener onItemEventListener;


	public void setOnItemEventListener(OnItemEventListener oOnItemEventListener) {
		this.onItemEventListener = oOnItemEventListener;
	}

	public interface OnItemEventListener{
		void onItemClick(View view, Message message);
		boolean onLongItemClick(View view, Message message);
	}


	public class ViewHolder extends RecyclerView.ViewHolder{
		ImageView leftHeader;
		ImageView rightHeader;
		LinearLayout leftTextContent;
		LinearLayout rightTextContent;
		TextView leftText;
		TextView rightText;
		TextView leftTime;
		TextView rightTime;
		ImageView leftPhoto;
		ImageView rightPhoto;
		private Message msg;

		public ViewHolder(View view) {
			super(view);
			leftHeader = (ImageView) view.findViewById(R.id.left_header);
			rightHeader =  (ImageView) view.findViewById(R.id.right_header);
			leftText = (TextView) view.findViewById(R.id.leftText);
			leftTextContent = (LinearLayout) view.findViewById(R.id.leftTextContent);
			rightText = (TextView) view.findViewById(R.id.rightText);
			rightTextContent = (LinearLayout) view.findViewById(R.id.rightTextContent);
			leftTime = (TextView) view.findViewById(R.id.leftTime);
			rightTime = (TextView) view.findViewById(R.id.rightTime);
			leftPhoto = (ImageView) view.findViewById(R.id.leftPhoto);
			rightPhoto = (ImageView) view.findViewById(R.id.rightPhoto);
            itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if(onItemEventListener != null){
						onItemEventListener.onItemClick(view, msg);
					}

				}
			});
			itemView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					if(onItemEventListener != null){
						return  onItemEventListener.onLongItemClick(view, msg);
					}
					return false;
				}
			});
		}

		public void update(Message message){
			msg = message;
			if(message != null) {
				MessageType msgType = MessageType.text;
				if(!TextUtils.isEmpty(message.type)) {
					msgType = MessageType.valueOf(message.type);
				}
				Member member = MemberManager.getInstance().getMemberById(message.fromId);
				if (member != null) {
					if (isMe(message.fromId)) {
						leftTextContent.setVisibility(View.GONE);
						rightTextContent.setVisibility(View.VISIBLE);
						rightTime.setText(TimeUtils.getTimeFormatStr(message.time));

						if(msgType == null || msgType.equals(MessageType.text)){
							rightText.setVisibility(View.VISIBLE);
							rightPhoto.setVisibility(View.GONE);
							rightText.setText(member.name + "說:\n" + message.text);
						} else if (msgType.equals(MessageType.Photo)){
							rightText.setVisibility(View.GONE);
							rightPhoto.setVisibility(View.VISIBLE);
							rightPhoto.setTag(message.downloadUrl);
							Bitmap bitmap = imageLoader.getMemoryCache().get(message.downloadUrl);
							leftPhoto.setTag(null);
							if (bitmap != null && !bitmap.isRecycled()) {
								rightPhoto.setImageBitmap(bitmap);
							} else {
								rightPhoto.setImageBitmap(null);
								rightPhoto.setTag(message.downloadUrl);
								imageLoader.displayImage(message.downloadUrl, rightPhoto, getDisplayImageOptions());
							}
						}

						rightHeader.setTag(member.icon);
						leftHeader.setTag(null);

//					if (!TextUtils.isEmpty(member.icon)) {
//						Bitmap bitmap = imageLoader.getMemoryCache().get(member.icon);
//						if (bitmap != null && !bitmap.isRecycled()) {
//							rightHeader.setImageBitmap(bitmap);
//						} else {
//							imageLoader.displayImage(member.icon, rightHeader, getDisplayImageOptions());
//						}
//					} else {
//						rightHeader.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
//					}

					} else {
						leftTextContent.setVisibility(View.VISIBLE);
						rightTextContent.setVisibility(View.GONE);
						leftTime.setText(TimeUtils.getTimeFormatStr(message.time));
						leftHeader.setTag(member.icon);
						if(msgType == null || msgType.equals(MessageType.text)){
							leftText.setVisibility(View.VISIBLE);
							leftPhoto.setVisibility(View.GONE);
							leftText.setText(member.name + "說:\n" + message.text);
						} else if (msgType.equals(MessageType.Photo)){
							leftText.setVisibility(View.GONE);
							leftPhoto.setVisibility(View.VISIBLE);
							leftPhoto.setTag(message.downloadUrl);
							Bitmap bitmap = imageLoader.getMemoryCache().get(message.downloadUrl);
							rightPhoto.setTag(null);
							if (bitmap != null && !bitmap.isRecycled()) {
								leftPhoto.setImageBitmap(bitmap);
							} else {
								leftPhoto.setImageBitmap(null);
								leftPhoto.setTag(message.downloadUrl);
								imageLoader.displayImage(message.downloadUrl, leftPhoto, getDisplayImageOptions());
							}
						}
//					if (!TextUtils.isEmpty(member.icon)) {
//						Bitmap bitmap = imageLoader.getMemoryCache().get(member.icon);
//						if (bitmap != null && !bitmap.isRecycled()) {
//							leftHeader.setImageBitmap(bitmap);
//						} else {
//							imageLoader.displayImage(member.icon, leftHeader, getDisplayImageOptions());
//						}
//					} else {
//						leftHeader.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
//					}
					}
				} else {
					leftTextContent.setVisibility(View.VISIBLE);
					rightTextContent.setVisibility(View.GONE);
					rightHeader.setTag(null);
					leftHeader.setTag(null);
					leftTime.setText(TimeUtils.getTimeFormatStr(message.time));
					leftHeader.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
					if(msgType == null || msgType.equals(MessageType.text)){
						leftText.setVisibility(View.VISIBLE);
						leftPhoto.setVisibility(View.GONE);
						leftText.setText("某人說:\n" + message.text);
					} else if (msgType.equals(MessageType.Photo)){
						leftText.setVisibility(View.GONE);
						leftPhoto.setVisibility(View.VISIBLE);
						leftPhoto.setTag(message.downloadUrl);
//					Bitmap bitmap = imageLoader.getMemoryCache().get(message.downloadUrl);
//					rightPhoto.setTag(null);
//					if (bitmap != null && !bitmap.isRecycled()) {
//						leftPhoto.setImageBitmap(bitmap);
//					} else {
//						leftPhoto.setImageBitmap(null);
//						leftPhoto.setTag(message.downloadUrl);
//						imageLoader.displayImage(message.downloadUrl, leftPhoto, getDisplayImageOptions());
//					}
					}
				}

			}
		}
	}

	public void setData(List<Message> data) {
		this.data.clear();
		this.data.addAll(data);
		initImageLoader();
	}

	public ChatRoomMessageAdapter(Context context) {
		this.context = context;


	}


	public Message getItem(int i) {
		return i < data.size() ? data.get(i):null;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_message_item, null));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.update(getItem(position));
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

	private boolean isMe(String uid){
		return  FirebaseAuth.getInstance().getCurrentUser() != null
				&& uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid());
	}


	private void initImageLoader(){
		File cacheDir = StorageUtils.getCacheDirectory(context);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.memoryCacheExtraOptions(480, 800) // default = device screen dimensions
				.diskCacheExtraOptions(480, 800, null)
				.threadPoolSize(3)
				.threadPriority(Thread.NORM_PRIORITY - 2) // default
				.tasksProcessingOrder(QueueProcessingType.FIFO) // default
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new LruMemoryCache(3 * 1024 * 1024 * context.getResources().getDisplayMetrics().densityDpi))
				.diskCache(new UnlimitedDiskCache(cacheDir)) // default
				.diskCacheSize(50 * 1024 * 1024)
				.diskCacheFileCount(100)
				.diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
				.imageDownloader(new BaseImageDownloader(context)) // default
				.imageDecoder(new BaseImageDecoder(false)) // default
				.defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
				.build();
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);
		imageLoader.setDefaultLoadingListener(new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {

			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				ImageView imageView = (ImageView) view;
				if(imageUri.equals(imageView.getTag())){
					imageView.setImageResource(R.drawable.visitor);
				}
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				ImageView imageView = (ImageView) view;
				if(imageUri.equals(imageView.getTag())){
					imageView.setImageBitmap(loadedImage);
				}
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {

			}
		});
	}

	private  DisplayImageOptions getDisplayImageOptions(){
		return new DisplayImageOptions.Builder()
				.bitmapConfig(Bitmap.Config.RGB_565)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.cacheOnDisk(true)
				.resetViewBeforeLoading(true)
				.build();
	}

	public Bitmap getBitmap(String url){
		File file = imageLoader != null? imageLoader.getDiskCache().get(url): null;
		if(file != null && file.exists()){
			return  BitmapFactory.decodeFile(file.getPath());
		}
		return  null;
	}
}
