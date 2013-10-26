package controllers;

import java.util.Date;

import models.messaging.Message;
import play.cache.Cache;
import play.mvc.Before;

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
		
		Message msg = new Message(userID, userType, getMyID(), getMyUserType(), title, content, new Date(), false, false, false, false);
		msg.save();
		
		flash.success("消息已发送");
		index();
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
		PiggybackPacket pp = new PiggybackPacket(
				Message.find("toID=? and toUserType=? and isSystemMessage=?", getMyID(), getMyUserType(), false).fetch(),
				getMessageCount(getMyUserType(), getMyID()));
		renderJSON(pp);
	}
	
	public static void fetchOutbox() {
		PiggybackPacket pp = new PiggybackPacket(
				Message.find("fromID=? and fromUserType=?", getMyID(), getMyUserType()).fetch(),
				getMessageCount(getMyUserType(), getMyID()));
		renderJSON(pp);
	}
	
	public static void markRead(long msgID) {
		Message msg = Message.find("toID=? and toUserType=? and id=?", getMyID(), getMyUserType(), msgID).first();
		if (msg != null
			&& !msg.isRead) {
			setMessageCount(getMyUserType(), getMyID(), getMessageCount(getMyUserType(), getMyID()) - 1);
			msg.isRead = true;
			msg.save();
		}
		long msgCount = getMessageCount(getMyUserType(), getMyID());
		if (msgCount < 0) {
			msgCount = 0;
		}
		PiggybackPacket pp = new PiggybackPacket(null, msgCount);
		renderJSON(pp);
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
			count = Message.count("toID = ? and toUserType=? and isRead=?", userID, userType, false);
			
			Cache.set(MSG_CACHE_PREFIX + userType + "_" + userID, count);
		} else {
			count = Cache.get(MSG_CACHE_PREFIX + userType + "_" + userID, Long.class);
		}
		
		return count;
	}
	
	static void setMessageCount(String userType, long userID, long count) {
		Cache.set(MSG_CACHE_PREFIX + userType + "_" + userID, count);
	}
	
	
	static class PiggybackPacket {
		@SuppressWarnings("unused")
		private Object msg;
		@SuppressWarnings("unused")
		private long unread;
		
		public PiggybackPacket(Object msg, long unread) {
			this.msg = msg;
			this.unread = unread;
		}
	}
}
