package controllers;

import models.messaging.Mail;
import models.messaging.Notification;
import play.cache.Cache;
import play.mvc.Before;

/**
 * 私信/消息模块 v2
 */
public class Messaging2 extends Application {
	// 验证
	@Before
	public static void isLogged() {
		if (session.get("logged") == null) {
			SimpleUsers.login();
		}
	}

	private static long getMyID() {
		return Long.valueOf(session.get("logged"));
	}

	private static String getMyUserType() {
		return session.get("usertype");
	}

	// 缓存
	private static final String MAIL_CACHE_PREFIX = "mailCount_";
	private static final String NOTIFICATION_CACHE_PREFIX = "notificationCount_";

	static long getMailCount(String userType, long userID) {
		long count;

		if (Cache.get(MAIL_CACHE_PREFIX + userType + "_" + userID) == null) {
			count = Mail.count("toID=? and toUserType=? and isRead=? and isDeleted=?", userID, userType, false,
				false);
			Cache.set(MAIL_CACHE_PREFIX + userType + "_" + userID, count);
		} else {
			count = Cache.get(MAIL_CACHE_PREFIX + userType + "_" + userID, Long.class);
		}

		return count;
	}

	static void setMailCount(String userType, long userID, long count) {
		Cache.set(MAIL_CACHE_PREFIX + userType + "_" + userID, count);
	}

	static long getNotificationCount(String userType, long userID) {
		long count;

		if (Cache.get(NOTIFICATION_CACHE_PREFIX + userType + "_" + userID) == null) {
			count = Notification.count("userID=? and userType=? and isRead=? and isDeleted=?", userID, userType, false,
				false);
			Cache.set(NOTIFICATION_CACHE_PREFIX + userType + "_" + userID, count);
		} else {
			count = Cache.get(NOTIFICATION_CACHE_PREFIX + userType + "_" + userID, Long.class);
		}

		return count;
	}

	static void setNotificationCount(String userType, long userID, long count) {
		Cache.set(NOTIFICATION_CACHE_PREFIX + userType + "_" + userID, count);
	}
}
