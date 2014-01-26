package models.Meal;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class MealOrder extends Model {
	public String username;
	public Long userid;
	public String userType;
	public String shop_name;
	public String email;
	public String orNumber;
	public Long QQ;
	public Long phone;
	public String food;
	public String address;
	public String meals_date;
	public String generate_date;
	public String note;
	public String weixin;
	public String date;
	public int money;
	public boolean cancelOr;
	public boolean dlOr;
	public boolean successful;
	public boolean isSend;

	public MealOrder(String username, String userType, Long userid,
			String shop_name, Long phone, Long QQ, String orNumber,
			String email, String FoodName, String address, String meals_date,
			String generate_date, String note, String weixin,String date,int money, boolean cancelOr,
			boolean dlOr, boolean successful,boolean isSend) {
		this.username = username;
		this.userid = userid;
		this.userType = userType;
		this.phone = phone;
		this.shop_name = shop_name;
		this.QQ = QQ;
		this.email = email;
		this.orNumber = orNumber;
		this.address = address;
		this.food = FoodName;
		this.meals_date = meals_date;
		this.generate_date = generate_date;
		this.note = note;
		this.weixin = weixin;
		this.date=date;
		this.money = money;
		this.cancelOr = false;
		this.dlOr = false;
		this.successful = false;
		this.isSend = isSend;
		create();
	}

}
