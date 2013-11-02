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
	@ManyToOne
	public SimpleUser joiner;
	
	public String selfIntro;
	public boolean isAllown;
	@Required
	public String joinedAt;
	public boolean isNotified;

}