package models.activity;

import play.*;
import play.data.validation.Required;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class Scope extends Model {
	@OneToMany(mappedBy = "scope", cascade = CascadeType.ALL)
	public List<Activity> acitivity;
	@Required
	public String scope;
	public short sequence;
}
