package controllers;

import play.mvc.*;

public class Users extends Controller {

    public static void index() {
        render();
    }

	public static void preview(String type,long id) {
		if(type.equals("simple")){
			
		}
	render();
	}

}
