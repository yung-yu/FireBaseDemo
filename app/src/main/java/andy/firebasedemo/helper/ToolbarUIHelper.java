package andy.firebasedemo.helper;

import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by andyli on 2016/12/19.
 */

public class ToolbarUIHelper {
	static ToolbarUIHelper instance;

	public static ToolbarUIHelper getInstance() {
		if (instance == null) {
			instance = new ToolbarUIHelper();
		}
		return instance;
	}

	private ProgressBar progressBar;
	private TextView textView;

	public void init(ProgressBar progressBar, TextView textView){
		this.progressBar = progressBar;
		this.textView = textView;
	}

	public void setProgressBarVisibility(int visibility){
		if(progressBar != null){
			progressBar.setVisibility(visibility);
		}
	}


	public void setTextViewVisibility(int visibility){
		if(textView != null){
			textView.setVisibility(visibility);
		}
	}


	public void setText(String text){
		if(textView != null){
			textView.setText(text);
		}
	}

	public void setText(int text){
		if(textView != null){
			textView.setText(text);
		}
	}
}
