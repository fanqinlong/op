package models.activity;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class ActivityJoiner extends Model {
public long aid;
public long jid;
public String jname;
public String jgender;
public String jprofile;
public String selfIntro;
public boolean isAllown;
public String date;
public boolean isNoticed;

}