package models.users;

import play.*;
import play.db.jpa.*;
import play.libs.*;
import play.data.validation.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class SimpleUser extends Model {
	// Register Info
	public String email;
	public String passwordHash;
	public String needConfirmation;
	public String passwordConfirmation;
	public String profile; // 头像
	public String name;
	public String realName;
	public boolean dispRealName;
	public String gender;

	// User Detail

	public String eduMail;
	public String eduMailConfirmation;
	public String contract;
	public boolean dispContract;
	public String signature;
	public short age;
	public String birthday;
	public boolean dispBirthday;
	public String qq;
	public boolean dispQQ;
	public String ralationship;

	public String admissionTime; // 入学时间
	public String major;
	public String school;
	public String hometown;
	public String homeNow;

	public String selfIntro;
	public boolean isAdmin;
	public String signupDate; //注册时间

	public SimpleUser(String email, String password, String name, String gender,String signupDate,
			String profile) {
		this.email = email;
		this.name = name;
		this.gender = gender;
		this.passwordHash = Codec.hexMD5(password);
		this.needConfirmation = Codec.UUID();
		//this.profile = "/public/images/user_default.jpg";
		this.profile = profile;
		this.signupDate = signupDate;
		create();
	}

	public boolean isAdmin() {
		return email.equals(Play.configuration.getProperty("forum.adminEmail", ""));
	}

	public static SimpleUser findByRegistrationUUID(String uuid) {
		return find("needConfirmation", uuid).first();
	}

	public static SimpleUser findByResetPasswordUUID(String uuid) {
		return find("passwordConfirmation", uuid).first();
	}

	public boolean checkPassword(String password) {
		return passwordHash.equals(Codec.hexMD5(password));
	}

	public void changePassword(String password) {
		this.passwordHash = Codec.hexMD5(password);
		save();
	}

	public void changeProfile(String path) {
		this.profile = path;
		save();
	}

	public static SimpleUser findByEmail(String email) {
		return find("email", email).first();
	}

	public static boolean isEmailAvailable(String email) {
		if(findByEmail(email) == null && find("eduMail",email).fetch().isEmpty()){
		return true;
		}
		return false;
	}

	public void changeEmail(String email) {
		this.email = email;
		save();
	}

}
