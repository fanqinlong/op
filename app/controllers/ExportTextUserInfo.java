package controllers;

import java.util.List;

import models.airport.VolInfo;
import models.users.SimpleUser;
import play.modules.excel.RenderExcel;
import play.mvc.*;

public class ExportTextUserInfo extends Controller {

    public static void index() {
        render();
    }
    public static void exportTextUserInfo() {
    	request.format = "xlsx";
		List<SimpleUser> simp = SimpleUser.find(
				"order by id desc").fetch();
		String __FILE_NAME__ =  "测试账号信息.xlsx";
		renderArgs.put(RenderExcel.RA_ASYNC, true);
		render(__FILE_NAME__, simp);
    }

}
