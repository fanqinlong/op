package models.qa;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class ChildComment extends Model {
	public Long cUserid;     // 回答的用户id
	public String cUserName; // 回答的用户名字
	public String cUserType; // 回答者的类型
	public Long quesid;
	public Long commentsid;
	public String comments;
	public Long userid; // 操作者的用户id
	public String userName; // 操作者的姓名
	public String userType;

	public ChildComment(Long cUserid, String cUserName,String cUserType, Long quesid,
			String comments, Long userid, String userName,String userType) {
		this.cUserid = cUserid;
		this.cUserName = cUserName;
		this.cUserType = cUserType;
		this.quesid = quesid;
		this.comments = comments;
		this.userid = userid;
		this.userName = userName;
		this.userType = userType;
		create();

	}

}
