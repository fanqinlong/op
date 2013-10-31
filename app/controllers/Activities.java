package controllers;

import java.util.List;

import play.mvc.Before;
import models.activity.Activity;
import models.activity.Period;
import models.activity.Scope;
import models.activity.Type;

public class Activities extends Application {
	
	@Before(unless = { "index", "detail", "filterType", "filterPeriod", "filterPeriodWeekend", "filterScope", "filterLocation"})
	public static void isLogged() {
		if (session.get("usertype") == null) {
			SimpleUsers.login();
		}
	}
	
    public static void index() {
    	String type = session.get("type") == null ? "" : session.get("type");
		String scope = session.get("scope") == null ? "" : session.get("scope");
		String location = session.get("location") == null ? "" : session.get("location");
		String period = session.get("period") == null ? "" : session.get("period");
		List<Activity> a = Activity.find("select a from Activity a  where a.type.name  =? ", type).fetch();
		
		List<Type> t = Type.find("order by sequence asc").fetch();
		List<Scope> s = Scope.find("order by sequence asc").fetch();
		List<Period> p = Period.find("order by sequence asc").fetch();
        render(a,t,s,p);
    }

	public static void create() {
		List<Type> at = Type.findAll();
		List<Scope> s = Scope.findAll();
		render(at, s);
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
		session.put("days", -2);
		index();
	}

	public static void filterScope(String scope) {
		session.put("scope", scope);
		index();
	}


	public static void filterZip() {
	
	}

}
