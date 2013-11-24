package models.messaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

/**
 * 系统通知
 */
@Entity
public class Notification extends Model {
	public long userID;
	public String userType;
	public String tmplID;
	public ArrayList<String> params;
	public Date time;

	// 通知属性
	public Boolean isRead;
	public Boolean isDeleted;

	public Notification(long userID, String userType, String tmplID, ArrayList<String> params, Date time, Boolean isRead,
		Boolean isDeleted) {
		super();
		this.userID = userID;
		this.userType = userType;
		this.tmplID = tmplID;
		this.params = params;
		this.time = time;
		this.isRead = isRead;
		this.isDeleted = isDeleted;
	}
	
	// 运行时参数
	@Transient
	public String title;
	@Transient
	public String content;
}
