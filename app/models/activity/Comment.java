package models.activity;

import play.*;
import play.data.binding.As;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.*;

import javax.persistence.*;

import models.users.SimpleUser;

import java.util.*;

@Entity
public class Comment extends Model {
	@ManyToOne
	public Activity activity;
	public SimpleUser publisher;
	@MaxSize(1000)
	public static String comment;
	@Required @As("yyyy-MM-dd HH:mm")
	public static Date publishedAt;
	public static boolean isNotified;
    
}
