package controllers;

import java.io.File;
import java.util.List;

import notifiers.Notifier;
import notifiers.Trend;
import play.Logger;
import play.Play;
import play.data.validation.Email;
import play.data.validation.Equals;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.data.validation.URL;
import play.libs.Codec;
import play.libs.Crypto;
import play.libs.Files;
import play.libs.Images;
import play.mvc.*;
import models.activity.Activity;
import models.activity.Joiner;
import models.activity.Liker;
import models.qa.Ques;
import models.users.CSSA;
import models.users.SimpleUser;
import models.airport.School;

public class CSSAs extends Application {

	@Before(unless = { "login", "signup", "register", "confirmRegistration", "authenticate", "resendConfirmation", "forgetPassword", "doForgetPassword", "resetPasswordConfirmation", "resetPassword","preview" })
	public static void isLogged() {
		if (session.get("logged") == null) {
			SimpleUsers.login();
		}else{
			CSSA user = CSSA.findById(Utils.getUserId());
			if(user==null)
				SimpleUsers.login();
		}

	}

	public static void index() {
		render();
	}

	public static void signup() {
		List<School> schools = School.findAll();
		render(schools);
	}

	public static void login() {
		render();
	}

	public static void register(@Email @Required String email, @Required  @MaxSize(20) String password, @Required long school, @Required String contract, @Required @MaxSize(200) String selfIntro, @Required @URL String homepage, @Required @MinSize(7) @MaxSize(40) String applicant, @Required @MinSize(7) @MaxSize(100) String applicantTitle, @Required String peopleNumber, @Equals("password") String password2) {
		if ((!SimpleUser.isEmailAvailable(email)) || (!CSSA.isEmailAvailable(email))) {
			validation.keep();
			params.flash();
			flash.error("邮箱已使用。");
			signup();
		} else if (validation.hasErrors()) {
			validation.keep();
			params.flash();
			flash.error("请更正错误。");
			signup();
		}
		CSSA user = new CSSA(email, password,  contract, selfIntro, homepage, applicant, applicantTitle, peopleNumber);
		School s = School.findById(school);
		user.school = s;
		user.save();
		try {
			if (Notifier.welcomeCSSA(user)) {
				flash.success("请登录注册邮箱激活帐号。");
				login();
			}
		} catch (Exception e) {
			Logger.error(e, "Mail error");
		}
		flash.error("呃。。邮件发送失败，请重试。");
		login();
	}

	public static void logout() {
		flash.success("注销成功。欢迎再次登录。");
		session.clear();
		Application.index();
	}

	public static void confirmRegistration(String uuid) {
		CSSA user = CSSA.findByRegistrationUUID(uuid);
		notFoundIfNull(user);
		user.needConfirmation = null;
		user.save();
		connectCSSA(user);
		flash.success("Welcome %s CSSA!", user.school.name);
		infoCenter();
	}

	public static void authenticate(String email, String password) {
		CSSA user = (CSSA) CSSA.findByEmail(email);
		if (user == null || !user.checkPassword(password)) {
			flash.error("用户名或密码错误！");
			flash.put("email", email);
			login();
		} else if (user.needConfirmation != null) {
			flash.error("账户未激活");
			flash.put("notconfirmed", user.needConfirmation);
			flash.put("email", email);
			login();
		}
		connectCSSA(user);
		flash.success("欢迎回来， %s CSSA!", user.school.name);
		infoCenter();
	}

	public static void resendConfirmation(String uuid) {
		CSSA user = CSSA.findByRegistrationUUID(uuid);
		notFoundIfNull(user);
		try {
			if (Notifier.welcomeCSSA(user)) {
				flash.success("请登陆邮箱激活帐号。");
				flash.put("email", user.email);
				login();
			}
		} catch (Exception e) {
			Logger.error(e, "Mail error");
		}
		flash.error("邮件未能发送。");
		flash.put("email", user.email);
		login();
	}

	public static void show(Long id) {
		long uid = Long.parseLong(session.get("logged"));
		if (id != uid) {
			id = uid;
		}
		CSSA user = CSSA.findById(id);
		notFoundIfNull(user);
		render(user);
	}

	static void connectCSSA(CSSA user) {
		session.put("logged", user.id);
		session.put("usertype", "cssa");
	}

	public static void changePassword() {
		render();
	}

	public static void doChangePassword(@Required String currentPassword, @Required @MinSize(7) @MaxSize(20) String password, @Required @Equals("password") String password2) {
		long id = Utils.getUserId();
		if (id != Long.parseLong(session.get("logged"))) {
			flash.error("帐户有误，请重新登陆");
			session.clear();
			login();
		} else if (validation.hasErrors()) {
			validation.keep();
			params.flash();
			flash.error("请更正错误。");
			changePassword();
		} else if (!((CSSA) CSSA.findById(id)).checkPassword(currentPassword)) {
			validation.keep();
			params.flash();
			flash.error("原密码不正确！");
			changePassword();
		} else {
			((CSSA) CSSA.findById(id)).changePassword(password);
			flash.success("密码更改成功。");
			infoCenter();
		}
	}

	public static void updateProfile() {
		CSSA user = CSSA.findById(Utils.getUserId());
		notFoundIfNull(user);
		render(user);
	}

	public static void doUpdateProfile(CSSA user) {
		user.id = Utils.getUserId();
		user.save();
		flash.success("资料更新成功");
		infoCenter();
	}

	public static void changeEmail() {
		render();
	}

	public static void dochangeEmail(@Required @Email String email) {
		long id = Utils.getUserId();
		if (!CSSA.isEmailAvailable(email)) {
			validation.keep();
			params.flash();
			flash.error("邮箱已存在。");
			changeEmail();
		} else if (validation.hasErrors()) {
			validation.keep();
			params.flash();
			flash.error("请更正错误。");
			changeEmail();
		}
		((CSSA) CSSA.findById(id)).changeEmail(email);
		flash.success("邮箱更改成功，请重新登陆。");
		session.clear();
		login();
	}

	public static void forgetPassword() {
		render();
	}

	public static void doForgetPassword(@Required @Email String email) {
		if (validation.hasErrors()) {
			validation.keep();
			params.flash();
			flash.error("请更正错误。");
			forgetPassword();
		} else if (CSSA.isEmailAvailable(email)) {
			validation.keep();
			params.flash();
			flash.error("邮箱不存在。");
			forgetPassword();
		}
		
		CSSA user = CSSA.findByEmail(email);
		user.passwordConfirmation = Codec.UUID();
		user.save();
		try {
			if (Notifier.resetPasswordCSSA(user)) {
				flash.success("请登录注册邮箱重置密码。");
				login();
			}
		} catch (Exception e) {
			Logger.error(e, "Mail error");
		}
		flash.error("呃。。邮件发送失败，请重试。");
		login();
	}

	public static void resetPasswordConfirmation(String uuid) {
		CSSA user = CSSA.findByResetPasswordUUID(uuid);
		notFoundIfNull(user);
		user.passwordConfirmation = null;
		user.save();
		connectCSSA(user);
		flash.success("Welcome %s CSSA!", user.school.name);
		resetPassword(user.id);
	}

	public static void resetPassword(Long id) {
		render(id);
	}

	public static void doResetPassword(@Required @MinSize(7) @MaxSize(20) String password, @Required @Equals("password") String password2, Long id) {
		if (validation.hasErrors()) {
			validation.keep();
			params.flash();
			flash.error("请更正错误。");
			resetPassword(id);
		} else {
			((CSSA) CSSA.findById(id)).changePassword(password);
			flash.success("密码更改成功。");
			infoCenter();
		}
	}

	public static void changeProfile() {
		CSSA user = CSSA.findById(Utils.getUserId());
		notFoundIfNull(user);
		render(user);
	}

	public static void doChangeProfile( File f, int left, int top,
			int height, int width) {
		long id = Utils.getUserId();
		 String path1 = "public/images/profile/" + Codec.UUID() + ".jpg";
		Images.crop(f, f, left, top, height, width);
		Images.resize(f, f, 150, 150);
		Files.copy(f, Play.getFile(path1));
		 ((CSSA) CSSA.findById(id)).changeProfile(path1);
		flash.success("头像更改成功");
		infoCenter();
	}

	public static void infoCenter() {
		long id = Utils.getUserId();

		List<Trend> trends = Trend
				.find("select distinct t from Trend t left join t.a.liker as l "
						+ "where l.likerCSSA.id = ? or t.orderCSSA.id = ? or t.relationCSSA.id = ? order by time desc",
						id, id,id).fetch();
		
		CSSA user = CSSA.findById(id);
		notFoundIfNull(user);
		render(user,trends);
	}

	public static void myActivity() {
		long userId = Long.parseLong(session.get("logged"));
		CSSA user = CSSA.findById(userId);
		List<Activity> postedActivity = Activity.find("publisher_id=? and publisher_type=? order by id desc", userId, "cssa").fetch();
		List<Activity> LikedActivity = Activity.find("select a from  ActivityLiker al,Activity a where  al.lid= ? and ltype=? and al.aid = a.id order by a.id desc ", userId, "cssa").fetch();
		notFoundIfNull(user);
		render(user, postedActivity, LikedActivity);
	}

	public static void homePage(long userid) {
		CSSA user = CSSA.findById(userid);
		notFoundIfNull(user);
		render("@infoCenter",user);	
	}

	public static void preview(long userid) {
	CSSA user = CSSA.findById(userid);
	List<Activity> activities = Activity.find("publisherCSSA.id = ? order by views", userid).fetch(3);
	List<Ques> questions =Ques.find("userid = ? and usertype=? order by views", userid,"cssa").fetch(3);
	render(user,activities,questions);
	}
	
	public static void detail(long id){
		CSSA cssa = CSSA.findById(id);
		List<Activity> activities = Activity.find("publisherCSSA.id = ?",id).fetch();
		render(activities,cssa);
	}
	public static void userQues(long id){
		CSSA cssa = CSSA.findById(id);
		List<Ques> ques = Ques.find("userid = ?",id).fetch();
		render(ques,cssa);
	}
	
	
	public static void publishedActivity() {
		long userId = Utils.getUserId();
		List<Activity> activities = Activity.find("publisherCSSA.id = ? order by postAt desc ", userId)
				.fetch();
		CSSA user = CSSA.findById(userId);
		String tag = "publish";
		render(user, activities, tag);
	}



	public static void likedActivity() {
		long userId = Utils.getUserId();
		List<Liker> activities = Liker.find("likerCSSA.id = ? order by likedAt desc " , userId).fetch();
		CSSA user = CSSA.findById(userId);
		String tag = "like";
		render(user, activities, tag);
	}
	
	
	
}
