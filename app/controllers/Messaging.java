package controllers;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import models.messaging.Message;
import models.users.SimpleUser;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.mvc.Before;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 私信/消息模块
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
		render();
	}

	public static void compose(String userType, long userID) {
		renderArgs.put("userType", userType);
		renderArgs.put("userID", userID);
		render();
	}

	public static void send(String userType, long userID, String title, String content) {
		setMessageCount(userType, userID, getMessageCount(userType, userID) + 1);

		Message msg = new Message(userID, userType, getMyID(), getMyUserType(), title, content, new Date(), false,
			false, false, false);
		msg.save();

		flash.success("消息已发送");
		index();
	}
	
	public static void markRead(List<Long> selectedMails) {
		for (Long msgID: selectedMails) {
			Message msg = Message.find("toID=? and toUserType=? and id=?", getMyID(), getMyUserType(), msgID).first();
			if (msg != null
				&& !msg.isRead) {
				setMessageCount(getMyUserType(), getMyID(), getMessageCount(getMyUserType(), getMyID()) - 1);
				msg.isRead = true;
				msg.save();
			}
		}
		long msgCount = getMessageCount(getMyUserType(), getMyID());
		if (msgCount < 0) {
			msgCount = 0;
		}
		PiggybackPacket pp = new PiggybackPacket(null, msgCount);
		renderJSON(pp);
	}
	
	public static void trash(List<Long> selectedMails) {
		for (Long msgID: selectedMails) {
			Message msg = Message.find("toID=? and toUserType=? and id=? and isDeleted=?", getMyID(), getMyUserType(), msgID, false).first();
			if (msg != null
				&& !msg.isDeleted) {
				if (!msg.isRead) {
					setMessageCount(getMyUserType(), getMyID(), getMessageCount(getMyUserType(), getMyID()) - 1);
				}
				msg.isDeleted = true;
				msg.save();
			}
		}
		long msgCount = getMessageCount(getMyUserType(), getMyID());
		if (msgCount < 0) {
			msgCount = 0;
		}
		PiggybackPacket pp = new PiggybackPacket(null, msgCount);
		renderJSON(pp);
	}

	public static void mail() {
		render();
	}

	public static void notifications() {
		render();
	}

	public static void announcements() {
		render();
	}

	public static void settings() {
		render();
	}

	// AJAX
	public static void fetchInbox() {
		Query query = JPA
			.em()
			.createQuery(
				"from Message m, SimpleUser u where m.toID=(:toID) and m.toUserType=(:toUserType) and m.isDeleted=(:isDeleted) and u.id=m.fromID order by m.time desc")
			.setParameter("toID", getMyID())
			.setParameter("toUserType", getMyUserType())
			.setParameter("isDeleted", false);

		PiggybackPacket pp = new PiggybackPacket(
			query.getResultList(),
			getMessageCount(getMyUserType(), getMyID()));

		renderText(inboxJSONSerializer.toJson(pp));
	}

	public static void fetchOutbox() {
		Query query = JPA
			.em()
			.createQuery(
				"from Message m, SimpleUser u where m.fromID=(:fromID) and m.toUserType=(:fromUserType) and u.id=m.toID order by m.time desc")
			.setParameter("fromID", getMyID())
			.setParameter("fromUserType", getMyUserType());

		PiggybackPacket pp = new PiggybackPacket(
			query.getResultList(),
			getMessageCount(getMyUserType(), getMyID()));

		renderText(outboxJSONSerializer.toJson(pp));
	}

	public static void fetchNotifications() {
		renderJSON(Message.find("toID=? and toUserType=? and isSystemMessage=1", getMyID(), getMyUserType()).fetch());
	}

	public static void fetchAnnouncements() {
	}

	// 缓存
	// TODO-zhao: CAS？
	private static final String MSG_CACHE_PREFIX = "msgCount_";

	static long getMessageCount(String userType, long userID) {
		long count;

		if (Cache.get(MSG_CACHE_PREFIX + userType + "_" + userID) == null) {
			count = Message.count("toID = ? and toUserType=? and isRead=? and isDeleted=?", userID, userType, false, false);

			Cache.set(MSG_CACHE_PREFIX + userType + "_" + userID, count);
		} else {
			count = Cache.get(MSG_CACHE_PREFIX + userType + "_" + userID, Long.class);
		}

		return count;
	}

	static void setMessageCount(String userType, long userID, long count) {
		Cache.set(MSG_CACHE_PREFIX + userType + "_" + userID, count);
	}

	// 捎带更新消息数
	private static class PiggybackPacket {
		@SuppressWarnings("unused")
		private Object msg;
		@SuppressWarnings("unused")
		private long unread;

		public PiggybackPacket(Object msg, long unread) {
			this.msg = msg;
			this.unread = unread;
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
				|| (f.getDeclaringClass() == Message.class && f.getName().equals("isRead"))
				|| (f.getDeclaringClass() == Message.class && f.getName().equals("isStarred"))
				|| (f.getDeclaringClass() == Message.class && f.getName().equals("isDeleted"));
		}

		public boolean shouldSkipClass(Class<?> c) {
			return false;
		}
	}).create();
}
