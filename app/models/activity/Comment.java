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
	@ManyToOne
	public SimpleUser publisher;
	
	public  String tag;
	@MaxSize(1000)
	public  String comment;
	public  String publishedAt;
	public  boolean isNotified;
    
}
