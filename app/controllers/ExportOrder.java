package controllers;

import java.util.Date;
import java.util.List;

import play.modules.excel.RenderExcel;
import models.Meal.MealOrder;
import models.airport.StuInfo;
import models.airport.VolInfo;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

public class ExportOrder extends Controller {

	public static void index() {
		List<MealOrder> mealOrders = MealOrder.findAll();
		render(mealOrders);
	}
	public static void exportOrder() {
		request.format = "xlsx";
		List<MealOrder> voi = MealOrder.find(
				"SELECT a FROM MealOrder a WHERE shop_name LIKE ?",
				"%" + "test" + "%").fetch();
		String __FILE_NAME__ = "test" + "_订单.xlsx";
		renderArgs.put(RenderExcel.RA_ASYNC, true);
		render(__FILE_NAME__, voi);
	}

}
