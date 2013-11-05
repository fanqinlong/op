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
		List<Ques> ques = Ques.find("order by views desc").fetch(5);
		int QuesNum;
		List<Ques> qNumber = Ques.findAll();
		QuesNum = qNumber.size();

		List<Wel> wel = Wel.find("order by views desc").fetch(5);
		int WelNum;
		List<Wel> wNumber = Wel.findAll();
		WelNum = wNumber.size();

		List<Activity> activity = Activity.find("order by views desc").fetch(5);
		int AcNum;
		List<Activity> acNumber = Activity.findAll();
		AcNum = acNumber.size();

		List<StuInfo> stu = StuInfo.find("order by id desc").fetch(5);
		int StuNum;
		List<StuInfo> stuNumber = StuInfo.findAll();
		StuNum = stuNumber.size();

		// String userType = Utils.getUserType();
		// if(userType==null){
		// render();
		// }else if(userType.equals("simple")){
		// SimpleUsers.infoCenter();
		// }else{
		// CSSAs.infoCenter();
		// }

		render(ques, wel, activity, stu, QuesNum, WelNum, AcNum, StuNum);
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