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
	
	public String timeType;//时间类型，连续时间，或者自定义时间。
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
	public boolean isCanceled;
	public String canceledReasion;

	public String summary;
	@Required
	@Column(columnDefinition = "TEXT")
	public String intro;
	
	public boolean isOpen; // 是否公开活动
	public boolean isTop;
	public long views; //浏览量
	public String postAt;//发布日期
	
	public boolean isFrontPage;
	public boolean isPublished;


}