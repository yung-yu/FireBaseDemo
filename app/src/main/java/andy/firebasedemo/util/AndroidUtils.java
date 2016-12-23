package andy.firebasedemo.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import andy.firebasedemo.Log.L;

/**
 * Created by andyli on 2016/11/25.
 */

public class AndroidUtils {

	public static void startFragment(FragmentManager manager, Fragment fragment, int containerId, Bundle bd, boolean isAddBack){
		FragmentTransaction fragmentTransaction = manager.beginTransaction();
		Fragment curfragment = manager.findFragmentByTag(fragment.getClass().getName());
		try {
			if (curfragment == null) {
				fragmentTransaction.replace(containerId, fragment, fragment.getClass().getName());
				if (isAddBack) {
					fragmentTransaction.addToBackStack(fragment.getClass().getName());
				}
				fragmentTransaction.commit();
			}
		}catch (Exception e){
			L.e(e);
		}

	}


	public static void popAllFragments(FragmentManager manager){
		try {
			manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}catch (Exception e){
			L.e(e);
		}
	}
}
