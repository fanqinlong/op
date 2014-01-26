package models.qa;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class AgreeComment extends Model {
	public String userType;
	public Long userid;
	public Long commentsid;
	public Long quesId;
	public String quesTitle;

	public AgreeComment(String userType, Long userid, Long commentsid,
			Long quesId, String quesTitle) {

		this.userType = userType;
		this.userid = userid;
		this.commentsid = commentsid;
		this.quesId = quesId;
		this.quesTitle = quesTitle;
		create();
	}
}
