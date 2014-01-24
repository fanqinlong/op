package controllers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import play.mvc.Controller;

public class Utils extends Controller {

	public static String getExtension(File f) {
		return (f != null) ? getExtension(f.getName()) : "";
	}

	public static String getExtension(String filename) {
		return getExtension(filename, "");
	}

	public static String getExtension(String filename, String defExt) {
		if ((filename != null) && (filename.length() > 0)) {
			int i = filename.lastIndexOf('.');

			if ((i > -1) && (i < (filename.length() - 1))) {
				return filename.substring(i + 1);
			}
		}
		return defExt;
	}

	public static String trimExtension(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int i = filename.lastIndexOf('.');
			if ((i > -1) && (i < (filename.length()))) {
				return filename.substring(0, i);
			}
		}
		return filename;
	}

	public static String getUserType() {
		return session.get("usertype");
	}
	public static long getUserId(){
		return Long.parseLong(session.get("logged"));
	}
	public static String getNowTime(){
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
	}
	public static String getNowDate(){
		return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
	}
}