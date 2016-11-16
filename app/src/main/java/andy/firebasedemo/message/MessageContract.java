package andy.firebasedemo.message;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import andy.firebasedemo.main.BasePresenter;
import andy.firebasedemo.main.BaseView;
import andy.firebasedemo.map.MapContract;
import andy.firebasedemo.object.Message;

/**
 * Created by andyli on 2016/11/5.
 */

public interface MessageContract {

	interface View {

		void sendMessageReady();

		void sendMessageSuccess();

		void sendMessageFailed(String msg);

		void onNotify(List<Message> data);

		void setRefresh(boolean isRefresh);
	}

	interface Presenter extends BasePresenter {
		void sendMessage(String message);
	}
}
