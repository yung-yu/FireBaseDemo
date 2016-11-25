package andy.firebasedemo.chatroom;

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
	}

	interface Presenter extends BasePresenter {
		void sendMessage(String message);
	}
}
