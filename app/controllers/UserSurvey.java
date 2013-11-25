package controllers;

import models.US.Questioner;
import play.mvc.*;

public class UserSurvey extends Controller {
	public static void index(String name, String type, String content,
			String contact) {
		new Questioner(name, type, content, contact);
		
		System.out.println("1111");
		render();
	}

}
