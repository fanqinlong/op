
package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import models.messaging.Mail;
import models.messaging.Notification;
import models.users.CSSA;
import models.users.SimpleUser;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.i18n.Messages;
import play.mvc.Before;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 私信/消息模块 v2
 */
public class Messaging extends Application {
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
	
	// 业务
	public static void index() {
		long myID = getMyID();
		String userType = getMyUserType();
		
		if (getMailCount(userType, myID) > 0) {
			mail();
		} else if (getNotificationCount(userType, myID) > 0) {
			notification();
		} else {
			mail();
		}
	}
	
	public static void mail() {
		render();
	}
	
	public static void notification() {
		render();
	}
	
	public static void compose(String userType, long userID) {
		renderArgs.put("userType", userType);
		renderArgs.put("userID", userID);
		String userName = "";
		if (userType.equals("simple")) {
			userName = ((SimpleUser) SimpleUser.find("id=?", userID).first()).name;
		} else if (userType.equals("cssa")) {
			userName = ((CSSA) CSSA.find("id=?", userID).first()).school.name;
		}
		renderArgs.put("userName", userName);
		render();
	}
	
	public static void sendMail(String userType, long userID, String title, String content) {
		setMailCount(userType, userID, getMailCount(userType, userID) + 1);
		Mail mailObj = new Mail(userID, userType, getMyID(), getMyUserType(), title, content, new Date(), false,
			false, false, false);
		mailObj.save();
		flash.success("消息已发送");
		mail();
	}
	
	public static void markRead(List<Long> selectedMails) {
		for (Long mailID : selectedMails) {
			Mail mail = Mail.find("toID=? and toUserType=? and id=?", getMyID(), getMyUserType(), mailID).first();
			if (mail != null
				&& !mail.isRead) {
				setMailCount(getMyUserType(), getMyID(), getMailCount(getMyUserType(), getMyID()) - 1);
				mail.isRead = true;
				mail.save();
			}
		}
		long mailCount = getMailCount(getMyUserType(), getMyID());
		if (mailCount < 0) {
			mailCount = 0;
		}
		PiggybackPacket pp = new PiggybackPacket(null, mailCount, getNotificationCount(getMyUserType(), getMyID()));
		renderJSON(pp);
	}
	
	public static void trashMail(List<Long> selectedMails) {
		for (Long mailID : selectedMails) {
			Mail mail = Mail.find("toID=? and toUserType=? and id=? and isDeleted=?", getMyID(), getMyUserType(),
				mailID, false).first();
			if (mail != null
				&& !mail.isDeleted) {
				if (!mail.isRead) {
					setMailCount(getMyUserType(), getMyID(), getMailCount(getMyUserType(), getMyID()) - 1);
				}
				mail.isDeleted = true;
				mail.save();
			}
		}
		long mailCount = getMailCount(getMyUserType(), getMyID());
		if (mailCount < 0) {
			mailCount = 0;
		}
		PiggybackPacket pp = new PiggybackPacket(null, mailCount, getNotificationCount(getMyUserType(), getMyID()));
		renderJSON(pp);
	}
	
	public static void deleteNotification(long notificationID) {
		Notification notification = Notification.find("id=? and userID=? and userType=? and isDeleted=?",
			notificationID, getMyID(), getMyUserType(), false).first();
		if (notification != null) {
			notification.isDeleted = true;
			notification.save();
		}
		PiggybackPacket pp = new PiggybackPacket(null, getMailCount(getMyUserType(), getMyID()), 0);
		renderJSON(pp);
	}

	static void addNotification(String userType, long userID, String notificationTmplID, ArrayList<String> params) {
		Notification notification = new Notification(userID, userType, notificationTmplID, params, new Date(), false,
			false);
		notification.create();
		setNotificationCount(userType, userID, getNotificationCount(userType, userID) + 1);
	}

	// AJAX
	public static void fetchInbox() {
		Query query = JPA
			.em()
			.createQuery(
				"from Mail m, SimpleUser u where m.toID=(:toID) and m.toUserType=(:toUserType) and m.isDeleted=(:isDeleted) and u.id=m.fromID order by m.time desc")
			.setParameter("toID", getMyID())
			.setParameter("toUserType", getMyUserType())
			.setParameter("isDeleted", false);

		PiggybackPacket pp = new PiggybackPacket(
			query.getResultList(),
			getMailCount(getMyUserType(), getMyID()),
			getNotificationCount(getMyUserType(), getMyID()));

		renderText(inboxJSONSerializer.toJson(pp));
	}

	public static void fetchOutbox() {
		Query query = JPA
			.em()
			.createQuery(
				"from Mail m, SimpleUser u where m.fromID=(:fromID) and m.toUserType=(:fromUserType) and u.id=m.toID order by m.time desc")
			.setParameter("fromID", getMyID())
			.setParameter("fromUserType", getMyUserType());

		PiggybackPacket pp = new PiggybackPacket(
			query.getResultList(),
			getMailCount(getMyUserType(), getMyID()),
			getNotificationCount(getMyUserType(), getMyID()));

		renderText(outboxJSONSerializer.toJson(pp));
	}

	public static void fetchNotifications() {
		List<Notification> notifications = Notification.find(
			"userID=? and userType=? and isDeleted=? order by time desc", getMyID(), getMyUserType(), false).fetch();

		for (Notification n : notifications) {
			// 取模板内容
			n.title = Messages.get("NT_TITLE_" + n.tmplID);
			n.content = Messages.get("NT_CONTENT_" + n.tmplID, n.params.toArray());
		}

		PiggybackPacket pp = new PiggybackPacket(notifications,
			getMailCount(getMyUserType(), getMyID()), 0);

		setNotificationCount(getMyUserType(), getMyID(), 0);

		String response = notificationJSONSerializer.toJson(pp);

		for (Notification n : notifications) {
			if (!n.isRead) {
				n.isRead = true;
				n.save();
			}
		}

		renderText(response);
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

	// 捎带更新消息数
	private static class PiggybackPacket {
		@SuppressWarnings("unused")
		private Object payload;
		@SuppressWarnings("unused")
		private long unreadMail;
		@SuppressWarnings("unused")
		private long unreadNotification;

		public PiggybackPacket(Object payload, long unreadMail, long unreadNotification) {
			this.payload = payload;
			this.unreadMail = unreadMail;
			this.unreadNotification = unreadNotification;
		}
	}

	// GSON
	private static Gson inboxJSONSerializer = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
		public boolean shouldSkipField(FieldAttributes f) {
			return (f.getDeclaringClass() == SimpleUser.class && !f.getName().equals("name"));
		}

		public boolean shouldSkipClass(Class<?> c) {
			return false;
		}
	}).create();

	private static Gson outboxJSONSerializer = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
		public boolean shouldSkipField(FieldAttributes f) {
			return (f.getDeclaringClass() == SimpleUser.class && !f.getName().equals("name"))
				|| (f.getDeclaringClass() == Mail.class && f.getName().equals("isRead"))
				|| (f.getDeclaringClass() == Mail.class && f.getName().equals("isStarred"))
				|| (f.getDeclaringClass() == Mail.class && f.getName().equals("isDeleted"));
		}

		public boolean shouldSkipClass(Class<?> c) {
			return false;
		}
	}).create();

	private static Gson notificationJSONSerializer = new GsonBuilder().create();
}
 