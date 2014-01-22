package controllers;

import java.awt.Menu;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import notifiers.Trend;

import com.mysql.jdbc.Util;

import play.Play;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.libs.Codec;
import play.libs.Files;
import play.libs.Images;
import play.mvc.Before;
import play.mvc.Scope.Flash;
import models.Meal.MealOrder;
import models.activity.Activity;
import models.activity.Comment;
import models.activity.Joiner;
import models.activity.Liker;
import models.activity.Period;
import models.activity.Scope;
import models.activity.Time;
import models.activity.Type;
import models.qa.Ques;
import models.Meal.FoodName;
import models.qa.Tag;
import models.users.CSSA;
import models.users.SimpleUser;
public class Meal extends Application {
	public static void index() {
		session.put("Meal_test", "test");
		if (session.get("logged") == null) {
			renderTemplate("Meal/test/index.html");
		} else {
			boolean isMerchants = false;
			long user_id = Long.parseLong(session.get("logged"));
			if (user_id == 1) {
				isMerchants = true;
			}
			renderTemplate("Meal/test/sellerInfo.html", isMerchants, user_id);
		}
	}
 
	public static void inputFrom() {
		String shopName = session.get("Meal_test");
		List<FoodName> foodname = FoodName.findAll();
		render(foodname, shopName);
	}
	public static void sellerInfo() {
		String shopName = session.get("Meal_test");
		List<FoodName> foodname = FoodName.findAll();
		renderTemplate("Meal/test/sellerInfo.html", foodname, shopName);
	}

	public static void doOrder(String username, String userType, Long userid,
			String shop_name, Long phone, Long QQ, String orNumber,
			String email, long[] foodId, String address, String meals_date,
			String generate_date, String note, String weixin, String date,int money,
			boolean cancelOr, boolean dlOr, boolean successful, boolean isSend,
			int[] number) {
//		if (foodId.equals(null)) {
//			flash.error("至少要选择一个菜名");
//			System.out.println("111111");
//			inputFrom();
//		} else 
		
		System.out.println("看看总价"+money);
		if (phone == null) {
			flash.error("请输入电话号码!");
			inputFrom();
		} else if (address.equals("")) {
			flash.error("请输入取餐地点!");
			inputFrom();
		} else if (meals_date.equals("")) {
			flash.error("请输入取餐时间!");
			inputFrom();
		}
		String foodDetail = "";
		for(int j=0;j<foodId.length;j++){
			FoodName fn = FoodName.findById(foodId[j]);
			if(number[j]==0){
				foodDetail = foodDetail; 
			}else{
				foodDetail = foodDetail + fn.dishes+"X"+number[j]+" ";	
			}
		}
		long user_id = Long.parseLong(session.get("logged"));
		SimpleUser simp = SimpleUser.findById(user_id);
		String user_name = simp.name;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss ");
		SimpleDateFormat da = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
		String d = (df.format(Calendar.getInstance().getTime()));
		String num = (sd.format(Calendar.getInstance().getTime()));
		String dt = (da.format(Calendar.getInstance().getTime()));
		int ornumber;
		List<MealOrder> or = MealOrder.findAll();
		ornumber = or.size();
		String on = Integer.toString(ornumber + 1);
		orNumber = num + "000" + on;
		new MealOrder(user_name, "simple", user_id, "test", phone, QQ,
				orNumber, email, foodDetail, address, meals_date, d, note,
				weixin, dt,money, false, false, false, false);
		render(orNumber);
	}
	public static void lookOr(Long user_id) {
		long userid = Long.parseLong(session.get("logged"));
		if (user_id == userid) {
			List<MealOrder> meal = MealOrder.find(
					"SELECT a FROM MealOrder a WHERE shop_name LIKE ?",
					"%" + "test" + "%").fetch();
			render(meal);
		} else {
			flash.error("用户不匹配!");
			index();
		}
	}

	public static void deleteOrder(long id) {
		long user_id = Long.parseLong(session.get("logged"));
		MealOrder mealOrder = MealOrder.findById(id);
		mealOrder.dlOr = true;
		mealOrder.save();
		lookOr(user_id);
	}

	public static void doSend(long id) {
		long user_id = Long.parseLong(session.get("logged"));
		MealOrder mealOrder = MealOrder.findById(id);
		mealOrder.isSend = true;
		mealOrder.save();
		lookOr(user_id);
	}

	public static void doCancel(long id) {
		long user_id = Long.parseLong(session.get("logged"));
		MealOrder mealOrder = MealOrder.findById(id);
		mealOrder.cancelOr = true;
		mealOrder.save();
		lookOr(user_id);
	}

	public static void doSuccessful(long id) {
		long user_id = Long.parseLong(session.get("logged"));
		MealOrder mealOrder = MealOrder.findById(id);
		mealOrder.successful = true;
		mealOrder.save();
		lookOr(user_id);
	}

	public static void searchOrder(String time) {
		long user_id = Long.parseLong(session.get("logged"));
		List<MealOrder> meal = MealOrder
				.find("SELECT a FROM MealOrder a WHERE date LIKE  ?  order by id desc",
						"%" + time + "%").fetch();
		renderTemplate("Meal/lookOr.html", user_id, meal);
	}
}
