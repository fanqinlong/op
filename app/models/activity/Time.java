package models.activity;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Time extends Model {
    @ManyToOne
    public Activity activity;
    public String date;
    public String timeFrom;
    public String timeTo;
    
}
