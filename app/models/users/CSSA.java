package models.users;

import play.*;
import play.data.validation.*;
import play.db.jpa.*;
import play.libs.Codec;

import javax.persistence.*;

import java.util.*;

import models.airport.School;

@Entity
public class CSSA extends Model {
	// Register Info
	
	@Email @Required
	public String email;
	@Required @MinSize(7) 
	public String passwordHash;

	public String needConfirmation;
	public String passwordConfirmation;
	public String profile;
	
	// User Detail
	@OneToOne(cascade=CascadeType.ALL)
	public School school ;
	@Required
	public String contract;
	@Required @MaxSize(200)
	public String selfIntro;
	@Required @URL
	public String homepage;
	@Required @MinSize(7) @MaxSize(40) 
	public String applicant;
	@Required  @MaxSize(100)
	public String applicantTitle;
	@Required 
	public String peopleNumber;
	public boolean isApproved;
	
	public CSSA(String email,String password,String contract,String selfIntro,String homepage,String applicant,String applicantTitle,String peopleNumber) {
		this.email = email;
		this.contract = contract;
		this.selfIntro = selfIntro;
		this.homepage = homepage;
		this.passwordHash = Codec.hexMD5(password);
		this.applicant = applicant;
		this.applicantTitle = applicantTitle;
		this.peopleNumber = peopleNumber;
		this.needConfirmation = Codec.UUID();
		this.isApproved = false;
		this.profile = "/public/images/cssa_default.png";
		create();
	}
	
	public static CSSA findByRegistrationUUID(String uuid) {
		return find("needConfirmation", uuid).first();
	}
	public static CSSA findByResetPasswordUUID(String uuid) {
		return find("passwordConfirmation", uuid).first();
	}


	public boolean checkPassword(String password) {
		return passwordHash.equals(Codec.hexMD5(password));
	}
	public void  changePassword(String password){
		this.passwordHash = Codec.hexMD5(password);
		save();
	}


	public static CSSA findByEmail(String email) {
		return find("email", email).first();
	}

	public static boolean isEmailAvailable(String email) {
		return findByEmail(email) == null;
	}
	public  void changeEmail(String email){
		this.email = email;
		save();
	}
	public void changeProfile(String path){
		this.profile = path;
		save();
	}
}
