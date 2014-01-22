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
	@Before
	public static void isAdmin() {
		if (session.get("logged") != null
				&& session.get("usertype").equals("simple")) {
			SimpleUser su = SimpleUser.findById(Long.parseLong(session
					.get("logged")));
			if (su.isAdmin == true) {
				renderArgs.put("isAdmin", true);
			}
		}
	}

	public static void shopPage() {
		List<SimpleUser> s = SimpleUser.findAll();
		renderTemplate("Meal/test/shopPage.html", s);
	}

	public static void index(Long id) {
		SimpleUser simpleUser = SimpleUser.findById(id);
		if (simpleUser.isManager == true) {
			session.put("Meal_test", simpleUser.shopName);
		} else {
			session.put("Meal_test", "");
		}
		if (session.get("logged") == null) {
			renderTemplate("Meal/test/index.html", id);
		} else {
			boolean isMerchants = false;
			int allnumber = 0;
			int isSendnumber = 0;
			int isNotSendnumber = 0;
			int succnumber = 0;
			long user_id = Long.parseLong(session.get("logged"));
//			SimpleUser sp = SimpleUser.findById(id);
			if (id == user_id) {
				isMerchants = true;
				SimpleDateFormat da = new SimpleDateFormat("yyyy-MM-dd");
				String dt = (da.format(Calendar.getInstance().getTime()));
				List<MealOrder> moIsend = MealOrder
						.find("date = ? and shopName = ? and isOrSend = ? and isEffectiveOrder = ?",
								dt, session.get("Meal_test"), "isSuccessful",
								"isSuccess").fetch();
				List<MealOrder> moIssuccessful = MealOrder.find(
						"date = ? and shopName = ? and isSuccessful = ?", dt,
						session.get("Meal_test"), "isSuccessful").fetch();
				List<MealOrder> moIsnotSend = MealOrder
						.find("date = ? and shopName = ? and isOrSend = ? and isEffectiveOrder = ?",
								dt, session.get("Meal_test"), "isNot",
								"isSuccess").fetch();
				List<MealOrder> moisEffectiveOrder = MealOrder.find(
						"date = ? and shopName = ? and isEffectiveOrder = ?",
						dt, session.get("Meal_test"), "isSuccess").fetch();
				allnumber = moisEffectiveOrder.size();
				isSendnumber = moIsend.size();
				isNotSendnumber = moIsnotSend.size();
				succnumber = moIssuccessful.size();
			}
			renderTemplate("Meal/test/index.html", isMerchants, user_id,
					allnumber, isSendnumber, isNotSendnumber, succnumber);
		}
	}

	public static void inputFrom() {
		String shopName = null;
		if (session.get("Meal_test") != null) {
			shopName = session.get("Meal_test");
		}
		List<FoodName> foodname = FoodName.findAll();
		render(foodname, shopName);
	}

	public static void sellerInfo() {
		String shopName = null;
		if (session.get("Meal_test") != null) {
			shopName = session.get("Meal_test");
		}
		List<FoodName> foodname = FoodName.findAll();
		renderTemplate("Meal/test/sellerInfo.html", foodname, shopName);
	}

	public static void doOrder(String username, String userType, Long userid,
			String shopName, Long phone, Long QQ, String orNumber,
			String email, long[] foodId, String address, String meals_date,
			String generate_date, String note, String weixin, String date,
			int money, boolean cancelOr, boolean dlOr, boolean successful,
			boolean isSend, int[] number, boolean effectiveOrder,
			String isEffectiveOrder, String isSuccessful, String isOrSend,
			int price[]) {
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
		int allmoney = 0;
		for (int j = 0; j < foodId.length; j++) {
			FoodName fn = FoodName.findById(foodId[j]);
			if (number[j] == 0) {
				foodDetail = foodDetail;
			} else {
				allmoney = allmoney + price[j] * number[j];
				foodDetail = foodDetail + fn.dishes + "X" + number[j] + " ";
			}
		}
		long user_id = Long.parseLong(session.get("logged"));
		String shop_id = session.get("Meal_test");
		shopName = shop_id;
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
		MealOrder mealOrder = new MealOrder(user_name, "simple", user_id,
				shopName, phone, QQ, orNumber, email, foodDetail, address,
				meals_date, d, note, weixin, dt, allmoney, false, false, false,
				false, true, "isNot", "isNot", "isNot");
		render(mealOrder);
	}

	public static void editOrder(Long id) {
		String shopName = null;
		if (session.get("Meal_test") != null) {
			shopName = session.get("Meal_test");
		}
		List<FoodName> foodname = FoodName.findAll();
		MealOrder m = MealOrder.findById(id);
		render(foodname, shopName, m);
	}

	public static void confirm(MealOrder m, long[] foodId, int[] number,
			int price[]) {
		String foodDetail = "";
		int allmoney = 0;
		for (int j = 0; j < foodId.length; j++) {
			FoodName fn = FoodName.findById(foodId[j]);
			if (number[j] == 0) {
				foodDetail = foodDetail;
			} else {
				allmoney = allmoney + price[j] * number[j];
				foodDetail = foodDetail + fn.dishes + "X" + number[j] + " ";
			}
		}
		MealOrder mealOrder = MealOrder.findById(m.id);
		mealOrder.food = "";
		mealOrder.food = foodDetail;
		mealOrder.money = allmoney;
		mealOrder.save();
		renderTemplate("Meal/doOrder.html", mealOrder);
	}

	public static void orderSuccessful(Long id) {
		String orNumber = null;
		MealOrder mealOrder = MealOrder.findById(id);
		mealOrder.effectiveOrder = false;
		mealOrder.isEffectiveOrder = "isSuccess";
		orNumber = mealOrder.orNumber;
		mealOrder.save();
		render(orNumber);
	}

	public static void lookOr(Long user_id) {
		long userid = Long.parseLong(session.get("logged"));
		SimpleUser simpleUser = SimpleUser.findById(user_id);
		if (user_id == userid) {
			List<MealOrder> meal = MealOrder
					.find("SELECT a FROM MealOrder a WHERE shopName LIKE ? order by successful asc",
							simpleUser.shopName).fetch();
			String shopName = simpleUser.shopName;
			render(meal, shopName);
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
		mealOrder.isOrSend = "";
		mealOrder.isOrSend = "isSuccessful";
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
		System.out.println(mealOrder.isSend);
		if (mealOrder.isSend == false) {
			flash.error("订单还未派送不能设置为交易成功,如果需要取消订单请删除！");
			lookOr(user_id);
		} else {
			mealOrder.successful = true;
			mealOrder.isSuccessful = "";
			mealOrder.isSuccessful = "isSuccessful";
			mealOrder.save();
			lookOr(user_id);
		}

	}

	public static void searchOrder(String time) {
		long user_id = Long.parseLong(session.get("logged"));
		// List<MealOrder> meal = MealOrder
		// .find("SELECT a FROM MealOrder a WHERE date LIKE  ?  order by id desc",
		// "%" + time + "%").fetch();
		List<MealOrder> meal = MealOrder.find("date = ? and shopName = ? ",
				time, session.get("Meal_test")).fetch();

		System.out.println(session.get("Meal_test"));

		renderTemplate("Meal/lookOr.html", user_id, meal);
	}
}
