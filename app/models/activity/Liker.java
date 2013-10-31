package models.activity;

import play.*;
import play.data.binding.As;
import play.data.validation.Required;
import play.db.jpa.*;

import javax.persistence.*;

import models.users.CSSA;
import models.users.SimpleUser;

import java.util.*;

@Entity
public class Liker extends Model {
	@ManyToOne
	public Activity activity;
	public SimpleUser joinerSU;
	public CSSA joineCSSA;
	@Required @As("yyyy-MM-dd HH:mm")
	public Date likedAt;
	public boolean isNotified;
}