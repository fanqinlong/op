package models.messaging;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 用户私信
 */
@Entity
public class Message extends Model {

	public long toID;
	public String toUserType;
	public long fromID;
	public String fromUserType;
	public String title;
	public String content;
	public Date time;

	// 消息属性
	public Boolean isSystemMessage;
	public Boolean isStarred;
	public Boolean isRead;
	public Boolean isDeleted;

	public Message(long toID, String toUserType, long fromID,
			String fromUserType, String title, String content, Date time,
			Boolean isSystemMessage, Boolean isStarred, Boolean isRead,
			Boolean isDeleted) {
		super();
		this.toID = toID;
		this.toUserType = toUserType;
		this.fromID = fromID;
		this.fromUserType = fromUserType;
		this.title = title;
		this.content = content;
		this.time = time;
		this.isSystemMessage = isSystemMessage;
		this.isStarred = isStarred;
		this.isRead = isRead;
		this.isDeleted = isDeleted;
	}
}
