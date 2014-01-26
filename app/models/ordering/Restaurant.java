package models.ordering;

import javax.persistence.Entity;

import models.users.SimpleUser;

import play.data.validation.Email;
import play.data.validation.Password;
import play.data.validation.Phone;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Codec;

/**
 * 商家信息表
 * @author summer
 *
 */
@Entity
public class Restaurant extends Model{
	@Required
	public String rName;
	@Required @Email 
	public String email;
	@Required @Password
	public String passwordHash;
	
	public String poster;
	
	@Required
	public String address;
	
	@Required @Phone
	public long phone;
	public String introduct;
	
	
	public static Restaurant findByEmail(String email) {
		return find("email", email).first();
	}

	public boolean checkPassword(String password) {
		return passwordHash.equals(Codec.hexMD5(password));
	}

	public void changePassword(String password) {
		this.passwordHash = Codec.hexMD5(password);
		save();
	}
}
