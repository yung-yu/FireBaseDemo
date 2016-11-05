package andy.firebasedemo.login;

import andy.firebasedemo.main.BasePresenter;
import andy.firebasedemo.main.BaseView;

/**
 * Created by andyli on 2016/11/5.
 */

public interface LoginContract {
	interface View extends BaseView {
		void setProgessBarVisiably(int visiably);

		void LoginSuccess(LoginType type);

		void LoginFailed(String msg);

	}

	interface Presenter extends BasePresenter {
		void doEmailLoginLogin(String email, String password);

		void doGoogleLogin();

		void doFacebookLogin();

		void doAnonymouslyLogin();
	}

}
