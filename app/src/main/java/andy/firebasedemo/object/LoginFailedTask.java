package andy.firebasedemo.object;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.Executor;

/**
 * Created by andyli on 2016/11/20.
 */

public class LoginFailedTask extends Task {
	@Override
	public boolean isComplete() {
		return false;
	}

	@Override
	public boolean isSuccessful() {
		return false;
	}

	@Override
	public Object getResult() {
		return null;
	}

	@Nullable
	@Override
	public Exception getException() {
		return null;
	}

	@NonNull
	@Override
	public Task addOnSuccessListener(@NonNull OnSuccessListener onSuccessListener) {
		return null;
	}

	@NonNull
	@Override
	public Task addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener onSuccessListener) {
		return null;
	}

	@NonNull
	@Override
	public Task addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener onSuccessListener) {
		return null;
	}

	@NonNull
	@Override
	public Task addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
		return null;
	}

	@NonNull
	@Override
	public Task addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
		return null;
	}

	@NonNull
	@Override
	public Task addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
		return null;
	}

	@Override
	public Object getResult(@NonNull Class aClass) throws Throwable {
		return null;
	}
}
