package controllers;

import java.util.Date;
import java.util.List;
import play.modules.excel.RenderExcel;
import models.airport.StuInfo;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

public class ExportStuinfo extends Controller {

	public static void index() {
		List<StuInfo> contacts = StuInfo.findAll();
		render(contacts);
	}
	public static void exportStu(String username) {
		request.format = "xlsx";
		List<StuInfo> stu = StuInfo.find(
				"SELECT a FROM StuInfo a WHERE school LIKE ?",
				"%" + username + "%").fetch();
		String __FILE_NAME__ = username + "_需要接机人信息.xlsx";
		renderArgs.put(RenderExcel.RA_ASYNC, true);
		render(__FILE_NAME__, stu);

	}
}
