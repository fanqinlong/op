package models.activity;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class ActivityComment extends Model {
	public static long activityId;
	public static long publisherId;
	public static String comment;
	public static String date;
    
}
