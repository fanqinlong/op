package notifiers;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import models.activity.Activity;
import models.airport.StuInfo;
import models.airport.VolInfo;
import models.charity.Wel;
import models.qa.Comments;
import models.qa.Ques;
import models.users.CSSA;
import models.users.SimpleUser;

import java.util.*;

@Entity
public class Trend extends Model{
    public String time;
    @ManyToOne
    public SimpleUser orderSU;//操作发出者
    @ManyToOne
    public CSSA orderCSSA; //操作发出者
    @ManyToOne
    public SimpleUser relationSU; //操作相关人
    @ManyToOne
    public CSSA relationCSSA; //操作相关人
    @ManyToOne
    public Activity a;
    @ManyToOne
    public Ques q;
    @ManyToOne
    public Wel w;
    @ManyToOne
    public StuInfo stu;
    @ManyToOne
    public VolInfo vol;
    public String type;//实例太多，判断麻烦，类型字段，方便判断为哪个。activity qa ...
    public String action; //具体操作
    
    //活动
    public Trend(String time,SimpleUser orderSU,CSSA orderCSSA,SimpleUser relationSU,
    		CSSA relationCSSA,Activity a,String action, String type){
    	this.time = time;
    	this.orderSU = orderSU;
    	this.orderCSSA = orderCSSA;
    	this.relationSU = relationSU;
    	this.relationCSSA = relationCSSA;
    	this.a = a;
    	this.action = action;
    	this.type = type;
    }
    //问答
    public Trend(String time,SimpleUser orderSU,CSSA orderCSSA,SimpleUser relationSU, 
    		CSSA relationCSSA,Ques q,String action, String type,Comments c){
    	this.time = time;
    	this.orderSU = orderSU;
    	this.orderCSSA = orderCSSA;
    	this.relationSU = relationSU;
    	this.relationCSSA = relationCSSA;
    	this.q = q;
    	this.action = action;
    	this.type = type;
    }
    //公益
    public Trend(String time,SimpleUser orderSU,CSSA orderCSSA,SimpleUser relationSU,
    		CSSA relationCSSA,Wel w,String action, String type){
    	this.time = time;
    	this.orderSU = orderSU;
    	this.orderCSSA = orderCSSA;
    	this.relationSU = relationSU;
    	this.relationCSSA = relationCSSA;
    	this.w = w;
    	this.action = action;
    	this.type = type;
    }
    //接机学生
    public Trend(String time,SimpleUser orderSU,CSSA orderCSSA,SimpleUser relationSU,
    		CSSA relationCSSA,StuInfo stu,String action, String type){
    	this.time = time;
    	this.orderSU = orderSU;
    	this.orderCSSA = orderCSSA;
    	this.relationSU = relationSU;
    	this.relationCSSA = relationCSSA;
    	this.stu = stu;
    	this.action = action;
    	this.type = type;
    }
    
    //接机志愿者
    public Trend(String time,SimpleUser orderSU,CSSA orderCSSA,SimpleUser relationSU,
    		CSSA relationCSSA,VolInfo vol,String action, String type){
    	this.time = time;
    	this.orderSU = orderSU;
    	this.orderCSSA = orderCSSA;
    	this.relationSU = relationSU;
    	this.relationCSSA = relationCSSA;
    	this.vol = vol;
    	this.action = action;
    	this.type = type;
    }
	
    
}
