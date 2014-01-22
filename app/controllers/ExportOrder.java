package controllers;

import java.util.Date;
import java.util.List;

import play.modules.excel.RenderExcel;
import models.Meal.MealOrder;
import models.airport.StuInfo;
import models.airport.VolInfo;
import models.users.SimpleUser;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

public class ExportOrder extends Controller {

	public static void index() {
		List<MealOrder> mealOrders = MealOrder.findAll();
		render(mealOrders);
	}

	public static void exportOrder(String shopName) {
		long userid = Long.parseLong(session.get("logged"));
		SimpleUser simpleUser = SimpleUser.findById(userid);
		System.out.println("看看店面1"+shopName);
		System.out.println("看看店面2"+simpleUser.shopName);
		if (shopName.equals(simpleUser.shopName)) {
			request.format = "xlsx";
			List<MealOrder> voi = MealOrder.find(
					"SELECT a FROM MealOrder a WHERE shopName LIKE ?", shopName)
					.fetch();
			String __FILE_NAME__ = shopName+"_订单.xlsx";
			renderArgs.put(RenderExcel.RA_ASYNC, true);
			render(__FILE_NAME__, voi);
		} else {
			flash.error("错误:没有你要查询的信息!");
			lookOr(userid);
		}

	}

	private static void lookOr(Long user_id) {
		// TODO Auto-generated method stub

	}

}
