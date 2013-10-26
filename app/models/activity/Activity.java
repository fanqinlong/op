package models.activity;

import play.*;
import play.data.validation.*;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class Activity extends Model {
	public long publisher_id;
	public String publisher_type;
	public String publisher_name;
	public String publisher_profile;
	@Required
	public String type;
	@Required
	@MaxSize(40)
	public String name;
	@Required
	public String timeFrom;
	@Required
	public String timeTo;

	public boolean isWeekend;
	public boolean isOpen; // 是否公开活动
	@Required
	public String location;
	@Required
	public String zip;

	public String poster;
	@Required
	public String scope;
	@Required
	public float money;
	@Required
	public String contract;
	public boolean is_checked;
	public boolean isHot;
	public String summary;
	@Required
	@Column(columnDefinition = "TEXT")
	public String intro;
	public long distance;
	public String distances;
	public String duration;
	public int joinerCount;
	public int likerCount;
	public long views;
	// 这里是男女
	public int manNumber;
	public int womanNumber;

	public void savePoster(String path) {
		this.poster = path;
		this.is_checked = false;
		save();
	}

	public static List<Activity> filterType(Long id) {
		return find("type", id).fetch();
	}

}