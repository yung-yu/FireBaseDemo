package andy.firebasedemo.util;



import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by andyli on 2016/11/19.
 */

public class TimeUtils {

	 public static String  getTimeFormatStr(long time){
		 Date date = new Date(time);
		 SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd\nHH:mm:ss");
		 return format.format(date);
	 }
}
