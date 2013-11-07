package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import models.activity.Activity;
import models.airport.StuInfo;
import models.charity.Wel;
import models.qa.Ques;
import models.users.CSSA;
import models.users.SimpleUser;

public class Application extends Controller {

	@Before
	static void globals() {

		String userType = session.get("usertype");
		if (userType == null) {
			renderArgs.put("connectedSimpleUser", null);
			renderArgs.put("connectedCSSA", null);
		} else if (userType.equals("simple")) {
			renderArgs.put("connectedSimpleUser", connectedSimple());
		} else if (userType.equals("cssa")) {
			renderArgs.put("connectedCSSA", connectedCSSA());
		}

	}

	public static void index() {
		List<Ques> ques = Ques.find("order by views desc").fetch(6);
		int QuesNum;
		List<Ques> qNumber = Ques.findAll();
		QuesNum = qNumber.size();

		List<Wel> wel = Wel.find("order by views desc").fetch(6);
		int WelNum;
		List<Wel> wNumber = Wel.findAll();
		WelNum = wNumber.size();

		List<Activity> activity = Activity.find("order by views desc").fetch(6);
		long AcNum;
		AcNum = Activity.count();

		//render(ques, wel, activity, QuesNum, WelNum, AcNum);

		List<StuInfo> stu = StuInfo.find("order by id desc").fetch(5);
		int StuNum;
		
		List<StuInfo> stuNumber = StuInfo.findAll();
		StuNum = stuNumber.size();

		boolean isNotLogin = false;
		if (session.get("logged") == null) {
			isNotLogin = true;
			System.out.println("1111");

    	}else{
    		isNotLogin = false;
    		String userType = session.get("usertype");
    		long userid = Long.parseLong(session.get("logged"));
    		
    		String userprofile;
    		if(userType.equals("simple")){
    			SimpleUser sip = SimpleUser.findById(userid);
    			userprofile = sip.profile;
    		}else{
    			CSSA cssa = CSSA.findById(userid);
    			userprofile = cssa.profile;
    		}
    		System.out.println("头像路径"+userprofile);
    		render(ques, wel, activity, stu, QuesNum, WelNum, AcNum, StuNum,isNotLogin,userprofile);
    	}
		render(ques, wel, activity, stu, QuesNum, WelNum, AcNum, StuNum,isNotLogin);
	}

	static SimpleUser connectedSimple() {
		return SimpleUser.findById(Utils.getUserId());
	}

	static CSSA connectedCSSA() {
		return CSSA.findById(Utils.getUserId());
	}

	@Before
	static void checkSecure() {
		Secure secure = getActionAnnotation(Secure.class);
		if (secure != null) {
			if (connectedSimple() == null
					|| (secure.admin() && !connectedSimple().isAdmin())) {
				forbidden();
			}
		}
	}

	@Before
	static void rememberedUser() {
		// response.cookies.get("email").value;

	}

	@Before
	static void checkMessages() {
		String userType = session.get("usertype");
		String userID = session.get("logged");
		if (userID != null) {
			renderArgs.put("mailCount",
					Messaging.getMailCount(userType, Long.valueOf(userID)));
			renderArgs.put(
					"notificationCount",
					Messaging.getNotificationCount(userType,
							Long.valueOf(userID)));
		}
	}
}