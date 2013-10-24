package controllers;

import java.util.Date;
import java.util.List;

import play.modules.excel.RenderExcel;
import models.airport.StuInfo;
import models.airport.VolInfo;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

public class ExportVoiInfo extends Controller {

	public static void index() {
		List<VolInfo> contacts = StuInfo.findAll();
		render(contacts);
	}
	public static void exportVoiInfo(String username) {
		request.format = "xlsx";
		List<VolInfo> voi = VolInfo.find(
				"SELECT a FROM VolInfo a WHERE school LIKE ?",
				"%" + username + "%").fetch();
		String __FILE_NAME__ = username + "_志愿者.xlsx";
		renderArgs.put(RenderExcel.RA_ASYNC, true);
		render(__FILE_NAME__, voi);

	}

}
