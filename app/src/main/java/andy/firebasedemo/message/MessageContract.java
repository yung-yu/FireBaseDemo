package andy.firebasedemo.message;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import andy.firebasedemo.main.BasePresenter;
import andy.firebasedemo.main.BaseView;
import andy.firebasedemo.map.MapContract;

/**
 * Created by andyli on 2016/11/5.
 */

public interface MessageContract {

	interface View {

		void sendMessageReady();
		void sendMessageSuccess();
		void sendMessageFailed(String msg);

		void sendNotifcationMessageFinish();
	}

	interface Presenter extends BasePresenter {
		void sendNotifcationMessage(String uid);
		void sendMessage(String message);
	}
}
