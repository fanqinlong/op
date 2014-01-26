package models.activity;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class Type extends Model {
	@OneToMany(mappedBy="type",cascade=CascadeType.ALL)
	public List<Activity> acitivity; 
	public String name;
    public short sequence;
}
