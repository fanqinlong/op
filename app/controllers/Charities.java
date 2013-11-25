package controllers;

import play.*;
import play.db.jpa.JPABase;
import play.libs.Files;
import play.mvc.*;
import play.mvc.Scope.Session;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
  












import notifiers.Trend;
import models.*;
import models.activity.Activity;
import models.activity.Liker;
import models.charity.Wel;
import models.charity.welLiker;
import models.users.CSSA;
import models.users.SimpleUser;

public class Charities extends Application{
  
	@Before
	public static void isAdmin(){
		
		if (session.get("logged") != null && session.get("usertype").equals("simple")) {
			SimpleUser su = SimpleUser.findById(Long.parseLong(session.get("logged")));
			if(su.isAdmin == true){
			renderArgs.put("isAdmin",true);		
			}
		 }
	}
	
	public static void fabu() {
		if(session.get("logged") == null) {
                        Charities.pigination(1);
                }
               
		render();
	 }	
	public static void smfabu() {
		if(session.get("logged") == null) {
                        Charities.pigination(1);
                }
               
		render();
	 }	
 
 
	@SuppressWarnings("unused")
	public static void WelSave(String title,String content,String time,File f,String  generalize,int likerCount,boolean isChecked) {
		 
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss ");
		String d = (df.format(Calendar.getInstance().getTime()));
		
		//上传图片
		 
		if(f!=null){
			String fileName = f.getName();
			String extName = fileName.substring(fileName.lastIndexOf("."));
			UUID uuid = UUID.randomUUID();
			fileName = uuid.toString() + extName;
			
			String path = "/public/images/upload/"+fileName;
			Files.copy(f,Play.getFile(path));
			
			Wel w = new Wel(title,content,d,path, generalize,likerCount,true);
			wel();
 	}else{
			 
			 validation.keep();
			 params.flash();
			 flash.error("请上传封面图片");
			 
			 fabu();
			
		} 
	}	
	 
	
	public static void SmWelSave(String title,String content,String time,File f,String  generalize,int likerCount,boolean isChecked) {
		 
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss ");
		String d = (df.format(Calendar.getInstance().getTime()));
		
		//上传图片
		 
		if(f!=null){
			String fileName = f.getName();
			String extName = fileName.substring(fileName.lastIndexOf("."));
			UUID uuid = UUID.randomUUID();
			fileName = uuid.toString() + extName;
			
			String path = "/public/images/upload/"+fileName;
			Files.copy(f,Play.getFile(path));
			
			Wel w = new Wel(title,content,d,path, generalize,likerCount,false);
			
			wel();
 	}else{
			 
			 validation.keep();
			 params.flash();
			 flash.error("请上传封面图片");
			 
			 fabu();
			
		} 
	}	
	
	public static void pass(Long id){
		
		  Wel w = Wel.findById(id);
		  w.isChecked = true;
		  w.save();
		  render(w);
		}
	
	
	
 
 	public static void wel() {
 		 
	}	
 
 	
	public static void edit(long id,int pageNo){
		if(session.get("logged") == null) {
			Charities.pigination(1);
		}
		SimpleUser su = SimpleUser.findById(Long.parseLong(session.get("logged")));
		if(su.isAdmin == false){
			Charities.pigination(1);
			
		}
		Wel w = Wel.findById(id);
		 render(w,pageNo);
 	 }
	
	public static void update(Wel w,File f,int pageNo){
	 
		if(f==null){
			 
			 w.save();
		 
		}else{
			String fileName = f.getName();
			String extName = fileName.substring(fileName.lastIndexOf("."));
			UUID uuid = UUID.randomUUID();
			fileName = uuid.toString() + extName;
			String path = "/public/images/upload/"+fileName;
			Files.copy(f,Play.getFile(path));
			w.f = path;
	 
			w.save();
		}
		pigination(pageNo);
	 }
	
	
	public static void welfare(long id){
	  Wel w = Wel.findById(id);
	  w.views = w.views+1;
	  w.save();
	  render(w);
 	
	}

	
	public static void del(long id,int pageNo){
		if(session.get("logged") == null) {
			Charities.pigination(1);
		}
		SimpleUser su = SimpleUser.findById(Long.parseLong(session.get("logged")));
		if(su.isAdmin == false){
			Charities.pigination(1);
			
		}
		Wel w = Wel.findById(id);
		w.delete();
		pigination(pageNo);
	  }




	public static void pigination(int pageNo) {
	 
		if(session.get("logged") == null) {
            Charities.pigination(1);
    }
 		SimpleUser su = SimpleUser.findById(Long.parseLong(session.get("logged")));
 	 if (session.get("logged") != null && session.get("usertype").equals("simple")) {
 		
		if(su.isAdmin){
			
		 
		 List<Wel> we= Wel.find("order by likerCount desc").from((pageNo-1)*5).fetch(5);
			long pageCount = Wel.count()%5==0? Wel.count()/5:(Wel.count()/5+1);
			renderTemplate("Charities/wel.html",we,pageCount,pageNo );
		 
		}else {
			
			List<Wel> we = Wel.find("isChecked=true order by time desc").from((pageNo-1)*5).fetch(5);
			int count = Wel.find("isChecked=true order by time desc").fetch().size();
			int pageCount = count%5==0?count/5:(count/5+1);
			
			renderTemplate("Charities/wel.html",we,pageCount,pageNo );
			}
		}	
		 
	 }
	
	
	public static void like(long aid,int pageNo) {
		if(session.get("logged") == null) {
			SimpleUsers.login();
		}
		
		
		String userType = session.get("usertype");
		long userId = Long.parseLong(session.get("logged"));
		String usertype = session.get("usertype");
		List al_exist = welLiker.find(
				"aid = ? and lid = ? and ltype = ? ", aid, userId, usertype)
				.fetch();
	 
		
		if (!al_exist.isEmpty()) {
			flash.error("您已关注");
			pigination(pageNo);
		}
		welLiker al = new welLiker();
		al.aid = aid;
		al.lid = userId;
		al.ltype = usertype;
		al.save();
		Wel w = Wel.findById(aid);
	
		w.likerCount = w.likerCount + 1;
		w.save();
	
		
		if(userType.equals("cssa")){
			CSSA cssa = CSSA.findById(userId);
		 
			Trend tend = new Trend(Utils.getNowTime(), null, cssa, null, null, w,"关注了", "wel");
			tend.save();
		}else{
			SimpleUser simp = SimpleUser.findById(userId);
			
			Trend tend = new Trend(Utils.getNowTime(), simp, null, null, null,w,"关注了", "wel");
			tend.save();
		}
		
		
		flash.success("关注成功");
		pigination(pageNo);
	}
	 
}
