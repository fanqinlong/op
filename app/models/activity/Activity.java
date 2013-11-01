package models.activity;

import play.*;
import play.data.validation.*;
import play.db.jpa.*;

import javax.persistence.*;

import models.users.CSSA;
import models.users.SimpleUser;

import java.util.*;

@Entity
public class Activity extends Model {
	@ManyToOne
	public SimpleUser publisherSU;
	@ManyToOne
	public CSSA publisherCSSA;
	
	@ManyToOne
	public Type type;
	@ManyToOne
	public Scope scope;
	@ManyToOne
	public Period period;
	
	@OneToMany(mappedBy="activity", cascade=CascadeType.ALL)
	public List<Joiner> joiner;
	@OneToMany(mappedBy="activity", cascade=CascadeType.ALL)
	public List<Liker> liker;
	@OneToMany(mappedBy="activity",cascade=CascadeType.ALL)
	public List<Comment> comment;
	
	@Required
	@MaxSize(40)
	public String name;
	
	@OneToMany(mappedBy="activity", cascade=CascadeType.ALL)
	public List<Time> time;
	@Required
	public String location;
	@Required
	public String zip;
	public String poster;	
	@Required
	public float money;
	@Required
	public String contract;
	public boolean isChecked;
	public boolean isHot;
	public String summary;
	@Required
	@Column(columnDefinition = "TEXT")
	public String intro;
	
	public boolean isWeekend;
	public boolean isOpen; // 是否公开活动
	public boolean isTop;
	public long views; //浏览量

	public void savePoster(String path) {
		this.poster = path;
		this.isChecked = false;
		save();
	}

	public static List<Activity> filterType(Long id) {
		return find("type", id).fetch();
	}

}