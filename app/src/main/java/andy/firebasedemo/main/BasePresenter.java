package andy.firebasedemo.main;

import android.content.Intent;

/**
 * Created by andyli on 2016/11/5.
 */

public interface BasePresenter {
    void  start();
    void  stop();
    void  onActivityResult(int requestCode, int resultCode, Intent data);
}
