package andy.firebasedemo.chatroom;

import android.net.Uri;

import java.util.List;

import andy.firebasedemo.main.BasePresenter;
import andy.firebasedemo.main.BaseView;
import andy.firebasedemo.object.Message;

/**
 * Created by andyli on 2016/11/5.
 */

public interface ChatRoomContract {

	interface View  extends BaseView{

		void sendMessageReady();

		void sendMessageSuccess();

		void sendMessageFailed(String msg);

		void onNotify(List<Message> data);

		void onNotify();

		void sendImageSuccess();

		void onImageUploadProgress(int progress);

		void sendImageFailed(String msg);
	}

	interface Presenter extends BasePresenter {
		void sendMessage(String message);

		void sendImage(Uri uri);
	}
}
