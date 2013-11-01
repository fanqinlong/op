package models.activity;

import play.*;
import play.data.binding.As;
import play.data.validation.Required;
import play.db.jpa.*;

import javax.persistence.*;

import models.users.SimpleUser;

import java.util.*;

@Entity
public class Joiner extends Model {
	@ManyToOne
	public Activity activity;
	public SimpleUser joiner;
	
	public String selfIntro;
	public boolean isAllown;
	@Required @As("yyyy-MM-dd HH:mm")
	public Date joinedAt;
	public boolean isNotified;

}