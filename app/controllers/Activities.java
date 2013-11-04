package controllers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import com.mysql.jdbc.Util;

import play.Play;
import play.libs.Codec;
import play.libs.Files;
import play.libs.Images;
import play.mvc.Before;
import models.activity.Activity;
import models.activity.Joiner;
import models.activity.Liker;
import models.activity.Period;
import models.activity.Scope;
import models.activity.Time;
import models.activity.Type;
import models.users.CSSA;
import models.users.SimpleUser;

public class Activities extends Application {

	@Before(unless = { "index", "detail", "filterType", "filterPeriod",
			"filterPeriodWeekend", "filterScope", "filterLocation" })
	public static void isLogged() {
		if (Utils.getUserType() == null) {
			SimpleUsers.login();
		}
	}

	public static void index() {
		String type = session.get("type") == null ? "" : session.get("type");
		String scope = session.get("scope") == null ? "" : session.get("scope");
		String zip = session.get("zip") == null ? "" : session.get("location");
		String deadline = "";
		int days = session.get("days") == null ? -1 : Integer.parseInt(session
				.get("days"));
		if (days == -1)
			deadline = "7777-77-77";
		else {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, +days);
			deadline = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		}
		boolean isWeekend = false;
		if (days == -2)
			isWeekend = true;
		List<Activity> a;
		if (isWeekend) {
			a = Activity
					.find("select distinct a from Activity a join a.time as t  where t.date <? and t.isWeekend=true and  a.type.name like ? and a.scope.scope like ? order by isTop desc,isHot desc,isChecked desc,views desc",
							deadline, isWeekend, "%" + type + "%",
							"%" + scope + "%").fetch();
		} else {
			a = Activity
					.find("select distinct a from Activity a join a.time as t  where t.date <? and   a.type.name like ? and a.scope.scope like ? order by isTop desc,isHot desc,isChecked desc,views desc",
							deadline, "%" + type + "%", "%" + scope + "%")
					.fetch();
		}

		List<Type> t = Type.find("order by sequence asc").fetch();
		List<Scope> s = Scope.find("order by sequence asc").fetch();
		List<Period> p = Period.find("order by sequence asc").fetch();

		render(a, t, s, p, days, type, scope);
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
		String postAt = new SimpleDateFormat("yyyy-MM-dd HH:mm")
				.format(Calendar.getInstance().getTime());
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
					|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				time1.isWeekend = true;
			} else {
				time1.isWeekend = false;
			}

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
				time2.isWeekend = false;
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

	public static void detail(long id) {
		Activity a = Activity.findById(id);
		a.views = a.views + 1;
		a.save();
		boolean hasLiked = false;
		boolean hasJoined = false;
		boolean isAllown = false;
		boolean isOwner = false;
		if(Utils.getUserType().equals("simple")){
			List<Liker> likerExist =  Liker.find("likerSU.id = ? and activity.id = ?", Utils.getUserId(),id).fetch();
			hasLiked = likerExist.isEmpty()?false:true;
			List<Joiner> joinerExist =  Joiner.find("joiner.id = ? and activity.id = ?", Utils.getUserId(),id).fetch();
			hasJoined = joinerExist.isEmpty()?false:true;
			List<Joiner> allowner = Joiner.find("isAllown = ? and joiner.id = ? and activity.id = ?", true,Utils.getUserId(),id).fetch();
			isAllown = allowner.isEmpty()?false:true;
			if(a.publisherSU!=null && a.publisherSU.id == Utils.getUserId())
				isOwner = true;
		}else{
			List<Liker> likerExist =  Liker.find("likerCSSA.id = ? and activity.id = ?", Utils.getUserId(),id).fetch();
			hasLiked = likerExist.isEmpty()?false:true;
			hasJoined = true;
			if(a.publisherCSSA!=null && a.publisherCSSA.id == Utils.getUserId())
				isOwner = true;
		}
		List<Joiner> allownJoiner = Joiner.find("select distinct j from Joiner j join j.activity as a where a.id=? and j.isAllown = ?", id,true).fetch();
		
		
		render(a,hasLiked,hasJoined,isAllown,isOwner,allownJoiner);
		
	}

	public static void allJoinner(long aid) {
		boolean isOwner = false;
		List<Joiner> joiners = Joiner
				.find("select distinct j from Joiner j join j.activity as a where a.id=?",
						aid).fetch();
		Activity a = Activity.findById(aid);
		notFoundIfNull(a);
		if(Utils.getUserType().equals("simple") && a.publisherSU!=null && a.publisherSU.id == Utils.getUserId())
			isOwner = true;
		else if(a.publisherCSSA!=null && a.publisherCSSA.id == Utils.getUserId())
			isOwner = true;
		int allownCount =  Joiner.find("select distinct j from Joiner j join j.activity as a where a.id=? and j.isAllown = ?", aid,true).fetch().size();
		render(joiners,isOwner,allownCount);
	}
	public static void allownJoiner(long jid) {
		Joiner j = Joiner.findById(jid);
		j.isAllown = true;
		j.save();
		allJoinner(j.activity.id);
	}

	public static void disAllownJoiner(long jid) {
		Joiner j = Joiner.findById(jid);
		j.isAllown = false;
		j.save();
		allJoinner(j.activity.id);
	}

	public static void like(long aid) {
		Liker l = new Liker();
		if (Utils.getUserType().equals("cssa")) {
			List<Liker> likerExist =  Liker.find("likerCSSA.id = ? and activity.id = ?", Utils.getUserId(),aid).fetch();
			if(!likerExist.isEmpty()){
				flash.error("您已关注。");
				detail(aid);
			}
			l.likerCSSA = CSSA.findById(Utils.getUserId());
		} else {
			List<Liker> likerExist =  Liker.find("likerSU.id = ? and activity.id = ?", Utils.getUserId(),aid).fetch();
			if(!likerExist.isEmpty()){
				flash.error("您已关注。");
				detail(aid);
			}
			l.likerSU = SimpleUser.findById(Utils.getUserId());
		}
		Activity a = Activity.findById(aid);
		l.activity = a;
		l.likedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar
				.getInstance().getTime());
		l.save();
		detail(aid);
	}

	public static void join(long aid,String selfIntro) {
		Joiner j = new Joiner();
		if(Utils.getUserType().equals("cssa")){
			flash.error("抱歉，CSSA用户不可参加。");
			detail(aid);
		}else{
			List<Joiner> joinerExist = Joiner.find("joiner.id = ? and activity.id = ?",Utils.getUserId(),aid).fetch();
			if(!joinerExist.isEmpty()){
				flash.error("您已关注。");
				detail(aid);
			}
			j.activity = Activity.findById(aid);
			j.joiner = SimpleUser.findById(Utils.getUserId());
			j.selfIntro = selfIntro;
			j.isAllown = false;
			j.joinedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar
					.getInstance().getTime());
			j.save();
			flash.success("您申请参加成功，请静候审核结果。");
			detail(aid);
		}
	}

}
