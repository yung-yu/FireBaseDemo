package andy.firebasedemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import andy.firebasedemo.component.HeadShotMarker;

/**
 * Created by andyli on 2016/10/24.
 */

public class BigHeaderTask extends AsyncTask<String, Void, Bitmap>{
    private Marker marker;

    public BigHeaderTask(Marker marker) {
         this.marker = marker;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected Bitmap doInBackground(String... urls) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(urls[0])
                .build();

        Response response = null;
        Bitmap mIcon11 = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response.isSuccessful()) {
            try {
                mIcon11 = BitmapFactory.decodeStream(response.body().byteStream());
                mIcon11 = ThumbnailUtils.extractThumbnail(mIcon11, 100, 100);
                mIcon11 = HeadShotMarker.getRoundedShape(mIcon11);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

        }
        return mIcon11;
    }
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        try {
            if (marker != null) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
            }
        }catch (Exception e){

        }
    }

}
