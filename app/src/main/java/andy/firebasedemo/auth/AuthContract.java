package andy.firebasedemo.auth;


import andy.firebasedemo.main.BasePresenter;

/**
 * Created by andyli on 2016/11/25.
 */

public class AuthContract {
	public interface View {
		void onAuthSignOut();
		void onLogin();
	}

	public interface Presenter extends BasePresenter {
         void setAuthView(AuthContract.View authView);
	}
}
