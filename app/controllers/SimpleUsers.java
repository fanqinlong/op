package controllers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.mysql.jdbc.log.Log;

import notifiers.Notifier;
import play.Logger;
import play.Play;
import play.data.validation.Email;
import play.data.validation.Equals;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.libs.Codec;
import play.libs.Crypto;
import play.libs.Files;
import play.libs.Images;
import play.mvc.*;
import models.activity.Activity;
import models.activity.Joiner;
import models.qa.Ques;
import models.users.CSSA;
import models.users.SimpleUser;

public class SimpleUsers extends Application {

	@Before(unless = { "login", "signup", "register", "confirmRegistration", "authenticate", "resendConfirmation", "forgetpassword", "doforgetpassword", "resetPasswordConfirmation", "resetPassword","confirmEduMail" })
	public static void isLogged() {
		if (session.get("logged") == null) {
			login();
		}
	}

	public static void index() {
		render();
	}

	public static void signup() {
		render();
	}

	public static void login() {
		render();
	}

	public static void register(@Required @Email String email,
			@Required @MinSize(7) @MaxSize(20) String password,
			@Required String gender, 
			@Required @MinSize(2) @MaxSize(20) String name, 
			@Required String agreement,@Required String signupDate,
			String profile) {

		if ((!SimpleUser.isEmailAvailable(email)) || (!CSSA.isEmailAvailable(email))) {
			validation.keep();
			params.flash();
			flash.error("邮箱已存在。");
			signup();
		} else if (validation.hasErrors()) {
			validation.keep();
			params.flash();
			flash.error("请更正错误。");
			signup();
		}
		if(gender.equals("男")){
			profile = "/public/images/morentouxiang_nan.png";
		}else if(gender.equals("女")){
			profile = "/public/images/morentouxiang_nv.png";
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss ");
		signupDate = (df.format(Calendar.getInstance().getTime()));
		SimpleUser user = new SimpleUser(email, password, name, gender,signupDate,profile);
		try {
			if (Notifier.welcomeSimpleUser(user)) {
				flash.success("请登录 %s 激活帐号。", email);
				login();
			}
		} catch (Exception e) {
			Logger.error(e, "Mail error");
		}
		flash.error("向$s 发送邮件失败，请查看邮箱是否正确。",email);
		login();
	}

	public static void logout() {
		flash.success("注销成功。欢迎再次登录。");
		session.clear();
		Application.index();
	}

	public static void confirmRegistration(String uuid) {
		SimpleUser user = SimpleUser.findByRegistrationUUID(uuid);
		notFoundIfNull(user);
		user.needConfirmation = null;
		
		connectSimple(user);
		String email = user.email;
		String suffix = email.substring(email.length() - 3, email.length());
		if (suffix.equals("edu")) {
			user.eduMail = user.email;
			user.eduMailConfirmation = null;
			flash.success("Welcome %s !请完善个人信息。", user.name);
			user.save();
			updateProfile();
		} else {
			user.save();
			flash.success("Welcome %s !请验证EDU邮箱。", user.name);
			render("@edumail");
		}

	}
	public static void confirmEduMail(String uuid) {
		SimpleUser user = SimpleUser.find("eduMailConfirmation", uuid).first();
		notFoundIfNull(user);
		user.eduMailConfirmation = null;
		user.save();	
		connectSimple(user);
		flash.success("Welcome %s ! EDU邮箱验证成功，请完善个人信息。", user.name);
		updateProfile();
		
	}

	public static void eduMail() {
		long userid = Long.parseLong(session.get("logged"));
		String edumail;
		SimpleUser simp = SimpleUser.findById(userid);
		edumail = simp.eduMail;
		
		String  refere= request.headers.get("referer").values.get(0);
		render(refere,edumail);
	}

	public static void authEduMail(String eduMail) {		
		if (eduMail==null) {
			params.flash();
			flash.error("请填写邮箱。");
			render("@eduMail");
		}
		String suffix = eduMail.substring(eduMail.length()-3,eduMail.length());
		if(!suffix.equals("edu")){
			params.flash();
			flash.error("请填写EDU邮箱。");
			render("@eduMail");
		}
		if ((!SimpleUser.isEmailAvailable(eduMail)) || (!CSSA.isEmailAvailable(eduMail))) {
			params.flash();
			flash.error("邮箱已被使用。");
			render("@eduMail");
		} 
		Long uid = Long.parseLong(session.get("logged"));
		SimpleUser user = SimpleUser.findById(uid);
		user.eduMail = eduMail;
		user.eduMailConfirmation = Codec.UUID();
		user.save();
		
		try {
			if (Notifier.authEduMail(user)) {
				flash.success("请登录 %s 验证EDU邮箱", eduMail);
				login();
			}
		} catch (Exception e) {
			Logger.error(e, "Mail error");
		}
		flash.error("向$s 发送邮件失败，请查看邮箱是否正确。",eduMail);
		session.clear();
		login();

	}

	public static void authenticate(String email, String password) {
		CSSA cssa = (CSSA) CSSA.findByEmail(email);
		if (cssa == null) {
			SimpleUser user = (SimpleUser) SimpleUser.findByEmail(email);
			if (user == null) {
				flash.error("邮箱不存在。");
				login();
			} else if (!user.checkPassword(password)) {
				flash.error("密码错误。");
				flash.put("email", email);
				login();
			} else if (user.needConfirmation != null) {
				flash.put("notconfirmed", user.needConfirmation);
				flash.put("email", email);
				login();
			} else {
				connectSimple(user);
				flash.success("欢迎回来， %s !", user.name);
				infoCenter();
			}
		} else if (!cssa.checkPassword(password)) {
			flash.error("CSSA密码错误！");
			flash.put("email", email);
			CSSAs.login();
		} else if (cssa.needConfirmation != null) {
			flash.error("账户未激活");
			flash.put("notconfirmed", cssa.needConfirmation);
			flash.put("email", email);
			CSSAs.login();
		} else {
			CSSAs.connectCSSA(cssa);
			flash.success("欢迎回来， %s !", cssa.name);
			CSSAs.infoCenter(cssa.id);
		}

	}

	public static void resendConfirmation(String uuid) {
		SimpleUser user = SimpleUser.findByRegistrationUUID(uuid);
		notFoundIfNull(user);
		try {
			if (Notifier.welcomeSimpleUser(user)) {
				flash.success("请登陆%s 激活帐号。",user.email);
				flash.put("email", user.email);
				login();
			}
		} catch (Exception e) {
			Logger.error(e, "Mail error");
		}
		flash.error("向$s 发送邮件失败，请查看邮箱是否正确。",user.email);
		flash.put("email", user.email);
		login();
	}

	public static void show(Long id) {
		long uid = Long.parseLong(session.get("logged"));
		if (id != uid) {
			id = uid;
		}
		SimpleUser user = SimpleUser.findById(id);
		notFoundIfNull(user);
		render(user);
	}

	static void connectSimple(SimpleUser user) {
		session.put("logged", user.id);
		session.put("usertype", "simple");
	}

	public static void changePassword() {
		render();
	}

	public static void doChangePassword(@Required @MinSize(7) @MaxSize(20) String password, @Required @MinSize(7) @MaxSize(20) String password1, @Required @Equals("password") String password2) {
		long id = Long.parseLong(session.get("logged"));
		if (validation.hasErrors()) {
			validation.keep();
			params.flash();
			flash.error("请更正错误。");
			changePassword();
		} else if (!((SimpleUser) SimpleUser.findById(id)).checkPassword(password)) {
			validation.keep();
			params.flash();
			flash.error("原密码不正确！");
			changePassword();
		} else {
			((SimpleUser) SimpleUser.findById(id)).changePassword(password1);
			flash.success("密码更改成功。");
			infoCenter();
		}
	}

	public static void updateProfile() {
		long id = Long.parseLong(session.get("logged"));
		SimpleUser user = SimpleUser.findById(id);
		notFoundIfNull(user);
		render(user);
	}

	public static void doUpdateProfile(SimpleUser user) {
		user.save();
		flash.success("资料更新成功");
		infoCenter();
	}

	public static void changeEmail(Long id) {
		render(id);
	}

	public static void dochangeEmail(@Required @Email String email, Long id) {
		if (!SimpleUser.isEmailAvailable(email)) {
			validation.keep();
			params.flash();
			flash.error("邮箱已存在。");
			changeEmail(id);
		} else if (validation.hasErrors()) {
			validation.keep();
			params.flash();
			flash.error("请更正错误。");
			changeEmail(id);
		}
		((SimpleUser) SimpleUser.findById(id)).changeEmail(email);
		flash.success("邮箱更改成功，请重新登陆。");
		session.clear();
		login();
	}

	public static void forgetpassword() {
		render();
	}

	public static void doforgetpassword(@Required @Email String email) {
		System.out.println("here");
		if (validation.hasErrors()) {
			validation.keep();
			params.flash();
			flash.error("请更正错误。");
			forgetpassword();
		} else if (SimpleUser.isEmailAvailable(email)) {
			validation.keep();
			params.flash();
			flash.error("邮箱不存在。");
			forgetpassword();
		}

		SimpleUser user = SimpleUser.findByEmail(email);
		user.passwordConfirmation = Codec.UUID();
		user.save();
		try {
			if (Notifier.resetPasswordSimpleUser(user)) {
				flash.success("请登录%s重置密码。",email);
				login();
			}
		} catch (Exception e) {
			Logger.error(e, "Mail error");
		}
		flash.error("向$s 发送邮件失败，请查看邮箱是否正确。",email);
		login();
	}

	public static void resetPasswordConfirmation(String uuid) {
		SimpleUser user = SimpleUser.findByResetPasswordUUID(uuid);
		notFoundIfNull(user);
		user.passwordConfirmation = null;
		user.save();
		connectSimple(user);
		flash.success("邮箱验证成功，请填写新密码。");
		long id = user.id;
		renderArgs.put("id", id);
		renderTemplate("SimpleUsers/resetPassword.html");
	}

	public static void resetPassword(Long id) {
		renderArgs.put("id", id);
		render();
	}

	public static void doResetPassword(@Required @MinSize(7) @MaxSize(20) String password, @Required @Equals("password") String password2, Long id) {
		if (validation.hasErrors()) {
			validation.keep();
			params.flash();
			flash.error("请更正错误。");
			renderArgs.put("id", id);
			renderTemplate("SimpleUsers/resetPassword.html");
		} else {
			((SimpleUser) SimpleUser.findById(id)).changePassword(password);
			flash.success("密码更改成功。");
			infoCenter();
		}
	}

	public static void changeProfile(Long id) {
		SimpleUser user = SimpleUser.findById(id);
		notFoundIfNull(user);
		render(user);
	}

	public static void doChangeProfile(Long id, File poster, int left, int top, int height, int width) {
		String path = "public/images/profile/" + Codec.UUID() + ".jpg";
		Images.crop(poster, poster, left, top, height, width);
		Files.copy(poster, Play.getFile(path));

		((SimpleUser) SimpleUser.findById(id)).changeProfile(path);
		flash.success("头像更改成功");
		infoCenter();
	}

	public static void infoCenter() {
		long id = Long.parseLong(session.get("logged"));
		//List<Joiner> aj = Joiner.find("select aj from ActivityJoiner aj,Activity a where aj.aid = a.id and a.publisher_id = ?", id).fetch();
		SimpleUser user = SimpleUser.findById(id);
		List<Ques> ques = Ques.find("userid = ?", id).fetch();
		notFoundIfNull(user);
		render(user,ques);
	}

	public static void myActivity() {
		long userId = Long.parseLong(session.get("logged"));
		SimpleUser user = SimpleUser.findById(userId);
		List<Activity> postedActivity = Activity.find("publisher_id=? and publisher_type=? order by id desc", userId, "simple").fetch();
		List<Activity> JoinedActivity = Activity.find("select a from  ActivityJoiner aj,Activity a where  aj.jid= ? and aj.aid = a.id order by a.id desc ", userId).fetch();
		List<Activity> LikedActivity = Activity.find("select a from  ActivityLiker al,Activity a where  al.lid= ? and ltype=? and al.aid = a.id order by a.id desc ", userId, "simple").fetch();
		notFoundIfNull(user);
		render(user, postedActivity, JoinedActivity, LikedActivity);
	}

	public static void getActivityJoiner(long aid) {

		List<SimpleUser> s = SimpleUser.find("select s from SimpleUser s,ActivityJoiner aj where s.id = aj.jid and aid=?", aid).fetch();
		render(s);
	}



}
