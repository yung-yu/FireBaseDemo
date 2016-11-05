package andy.firebasedemo.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import andy.firebasedemo.main.BasePresenter;
import andy.firebasedemo.main.BaseView;

/**
 * Created by andyli on 2016/11/5.
 */

public interface MapContract {

	interface View {
		void OnMoveCamera(LatLng latLng, float zoom);

		Marker addMarker(MarkerOptions options);

	}

	interface Presenter extends BasePresenter {
		void Login();
	}
}
