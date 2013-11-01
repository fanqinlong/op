package controllers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import play.Play;
import play.libs.Codec;
import play.libs.Files;
import play.libs.Images;
import play.mvc.Before;
import models.activity.Activity;
import models.activity.Period;
import models.activity.Scope;
import models.activity.Time;
import models.activity.Type;
import models.users.SimpleUser;

public class Activities extends Application {

	@Before(unless = { "index", "detail", "filterType", "filterPeriod",
			"filterPeriodWeekend", "filterScope", "filterLocation" })
	public static void isLogged() {
		if (session.get("usertype") == null) {
			SimpleUsers.login();
		}
	}

	public static void index() {
		String type = session.get("type") == null ? "" : session.get("type");
		String scope = session.get("scope") == null ? "" : session.get("scope");
		String zip = session.get("zip") == null ? "" : session
				.get("location");
		String deadline ="";
		int days = session.get("days") == null ? -1 : Integer.parseInt(session.get("days"));
		if(days==-1)
			deadline="7777-77-77";
		else{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, +days);
			deadline = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		}
		boolean isWeekend = false;
		if(days==-2)
			isWeekend = true;
		List<Activity> a = Activity.find(
				"select distinct a from Activity a join a.time as t  where t.date <? and t.isWeekend=? and  a.type.name like ? and a.scope.scope like ? order by postAt,isTop,isHot,views desc",deadline,isWeekend,"%"+type+"%","%"+scope+"%")
				.fetch();

		List<Type> t = Type.find("order by sequence asc").fetch();
		List<Scope> s = Scope.find("order by sequence asc").fetch();
		List<Period> p = Period.find("order by sequence asc").fetch();
		render(a, t, s, p,days,type,scope);
	}

	public static void create() {
		render();
	}

	public static void next(File poster, int left, int top, int height,
			int width) {
		String path = "public/images/poster/" + Codec.UUID()
				+ poster.getName().substring(poster.getName().lastIndexOf("."));
		Images.crop(poster, poster, left, top, height, width);
		Images.resize(poster, poster, 300, 300, true);
		Files.copy(poster, Play.getFile(path));
		session.put("posterPath", path);
		flash.success("请填写活动详情。");
		List<Type> t = Type.find("order by sequence asc").fetch();
		List<Scope> s = Scope.find("order by sequence asc").fetch();
		String startDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar
				.getInstance().getTime());
		render(t, s, startDate);
	}

	public static void post(Activity a, Time time1, Time time2, Time time3,
			long type, long scope) {
		String poster = session.get("posterPath");
		a.poster = poster;
		if (Utils.getUserType().equals("simple")) {
			a.publisherSU = SimpleUser.findById(Utils.getUserId());
		}
		if (Utils.getUserType().equals("cssa")) {
			a.publisherCSSA = SimpleUser.findById(Utils.getUserId());
		}
		Type t = Type.findById(type);
		Scope s = Scope.findById(scope);
		String postAt = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
		a.postAt = postAt;
		a.type = t;
		a.scope = s;
		a.save();
		Calendar cal = Calendar.getInstance();
		if (!time1.date.equals("")) {
			cal.set(Integer.parseInt(time1.date.substring(0, 3)),
					Integer.parseInt(time1.date.substring(5, 6)),
					Integer.parseInt(time1.date.substring(8, 9)));
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
					|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
				time1.isWeekend = true;
			else 
				time3.isWeekend = false;
			
			time1.activity = a;
			time1.save();
		}
		if (!time2.date.equals("")) {
			cal.set(Integer.parseInt(time2.date.substring(0, 3)),
					Integer.parseInt(time2.date.substring(5, 6)),
					Integer.parseInt(time2.date.substring(8, 9)));
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
					|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
				time2.isWeekend = true;
			else 
				time3.isWeekend = false;
			time2.activity = a;
			time2.save();
		}
		if (!time3.date.equals("")) {
			cal.set(Integer.parseInt(time3.date.substring(0, 3)),
					Integer.parseInt(time3.date.substring(5, 6)),
					Integer.parseInt(time3.date.substring(8, 9)));
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
					|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
				time3.isWeekend = true;
			else 
				time3.isWeekend = false;
			time3.activity = a;
			time3.save();
		}

		flash.success("发布成功。");
		index();
	}

	public static void filterType(String type) {
		session.put("type", type);
		index();
	}

	public static void filterPeriod(short days) {
		session.put("days", days);
		
		
		index();
	}

	public static void filterPeriodWeekend() {
		session.put("days", "-2");
		index();
	}

	public static void filterScope(String scope) {
		session.put("scope", scope);
		index();
	}

	public static void filterZip(String zip) {
		session.put("zip", zip);
		index();
	}
	public static void detail(long id){
		Activity a = Activity.findById(id);
		a.views = a.views + 1;
		a.save();
		System.out.println(a.time.isEmpty());
		render(a);
	}

}
