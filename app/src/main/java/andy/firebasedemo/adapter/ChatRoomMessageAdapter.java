package andy.firebasedemo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
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
import andy.firebasedemo.util.TimeUtils;


/**
 * Created by andyli on 2016/11/7.
 */

public class ChatRoomMessageAdapter extends BaseAdapter {
	private final static String TAG = "ChatRoomMessageAdapter";
	private Context context;
	private List<Message> data = new ArrayList<>();
	private ImageLoader imageLoader;

	public void setData(List<Message> data) {
		this.data.clear();
		this.data.addAll(data);
		initImageLoader();
	}

	public ChatRoomMessageAdapter(Context context) {
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
    class ViewHolder{
		ImageView leftHeader;
		ImageView rightHeader;
		LinearLayout leftTextContent;
		LinearLayout rightTextContent;
		TextView leftText;
		TextView rightText;
		TextView leftTime;
		TextView rightTime;
	}
	private boolean isMe(String uid){
		return  FirebaseAuth.getInstance().getCurrentUser() != null
				&& uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid());
	}
	@Override
	public View getView(final int i, View view, ViewGroup viewGroup) {
		ViewHolder vh;
		if(view == null){
			vh = new ViewHolder();
			view = LayoutInflater.from(context).inflate(R.layout.adapter_message_item, null);
			vh.leftHeader = (ImageView) view.findViewById(R.id.left_header);
			vh.rightHeader =  (ImageView) view.findViewById(R.id.right_header);
			vh.leftText = (TextView) view.findViewById(R.id.leftText);
			vh.leftTextContent = (LinearLayout) view.findViewById(R.id.leftTextContent);
			vh.rightText = (TextView) view.findViewById(R.id.rightText);
			vh.rightTextContent = (LinearLayout) view.findViewById(R.id.rightTextContent);
			vh.leftTime = (TextView) view.findViewById(R.id.leftTime);
			vh.rightTime = (TextView) view.findViewById(R.id.rightTime);
			view.setTag(vh);
		}else{
			vh = (ViewHolder) view.getTag();
		}
		Message message = getItem(i);

		if(message != null){
			Member member = MemberManager.getInstance().getMemberById(message.fromId);
			if(member != null) {
				if (isMe(message.fromId)) {
					vh.leftTextContent.setVisibility(View.GONE);
					vh.rightTextContent.setVisibility(View.VISIBLE);
					vh.rightTime.setText(TimeUtils.getTimeFormatStr(message.time));
					vh.rightText.setText(member.name + "說:\n" + message.msg);
					vh.rightHeader.setTag(member.icon);
					vh.leftHeader.setTag(null);
					Bitmap bitmap = imageLoader.getMemoryCache().get(member.icon);
					if (bitmap != null && !bitmap.isRecycled()) {
						vh.rightHeader.setImageBitmap(bitmap);
					} else {
						imageLoader.displayImage(member.icon, vh.rightHeader, getDisplayImageOptions());
					}

				} else {
					vh.leftTextContent.setVisibility(View.VISIBLE);
					vh.rightTextContent.setVisibility(View.GONE);
					vh.leftTime.setText(TimeUtils.getTimeFormatStr(message.time));
					vh.leftHeader.setTag(member.icon);
					vh.rightText.setText(null);
					vh.leftText.setText(member.name + "說:\n" + message.msg);
					Bitmap bitmap = imageLoader.getMemoryCache().get(member.icon);
					if (bitmap != null && !bitmap.isRecycled()) {
						vh.leftHeader.setImageBitmap(bitmap);
					} else {
						imageLoader.displayImage(member.icon, vh.leftHeader, getDisplayImageOptions());
					}
				}
			}else{
				vh.leftTextContent.setVisibility(View.VISIBLE);
				vh.rightTextContent.setVisibility(View.GONE);
				vh.rightHeader.setTag(null);
				vh.leftHeader.setTag(null);
				vh.leftTime.setText(TimeUtils.getTimeFormatStr(message.time));
				vh.leftHeader.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
				vh.leftText.setText("某人說:\n" + message.msg);
			}

		}
		return view;
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

			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				ImageView imageView = (ImageView) view;
				if(imageUri.equals(imageView.getTag().toString())){
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
				.resetViewBeforeLoading(false)
				.build();
	}
}
