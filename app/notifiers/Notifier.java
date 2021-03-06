package notifiers;

import play.mvc.*;

import javax.mail.internet.*;

import models.users.CSSA;
import models.users.SimpleUser;

;

public class Notifier extends Mailer {

	public static boolean welcomeSimpleUser(SimpleUser user) throws Exception {
		setSubject("Welcome %s", user.name);
		addRecipient(user.email);
		setFrom("Opporlink <Account@opporlink.com>");
		return sendAndWait(user);
	}

	public static boolean welcomeCSSA(CSSA user) throws Exception {
		setSubject("Welcome %s CSSA", user.school.name);
		addRecipient(user.email);
		setFrom("Opporlink <Account@opporlink.com>");
		return sendAndWait(user);
	}

	public static boolean resetPasswordSimpleUser(SimpleUser user) throws Exception {
		setSubject("重置密码");
		addRecipient(user.email);
		setFrom("Opporlink <Account@opporlink.com>");
		return sendAndWait(user);
	}

	public static boolean resetPasswordCSSA(CSSA user) throws Exception {
		setSubject("重置密码");
		addRecipient(user.email);
		setFrom("Opporlink <Account@opporlink.com>");
		return sendAndWait(user);
	}
	public static boolean authEduMail(SimpleUser user) throws Exception{
		setSubject("验证EDU邮箱");
		addRecipient(user.eduMail);
		setFrom("Opporlink <Account@opporlink.com>");
		return sendAndWait(user);
	}

}