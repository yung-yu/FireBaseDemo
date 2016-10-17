package andy.firebasedemo;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by andyli on 2016/10/10.
 */

public class CustomMarker {
    public static MarkerOptions createMarker(Context context){
        MarkerOptions mMarkerOption = new MarkerOptions();
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.messageing);
        mMarkerOption.icon(icon);
        return mMarkerOption;
    }
}
