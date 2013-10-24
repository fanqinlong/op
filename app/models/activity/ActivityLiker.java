package models.activity;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class ActivityLiker extends Model {
public long aid;
public long lid;
public String lname;
public String lgender;
public String lprofile;
public String ltype;
public String date;
public boolean isNoticed;
}