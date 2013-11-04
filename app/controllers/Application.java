package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import models.users.CSSA;
import models.users.SimpleUser;

public class Application  extends Controller {

	@Before
	static void globals() {
	
		String userType = session.get("usertype");
		if(userType==null){
			renderArgs.put("connectedSimpleUser",null);
			renderArgs.put("connectedCSSA", null);
		}else if(userType.equals("simple")){		
			renderArgs.put("connectedSimpleUser", connectedSimple());
		}else if(userType.equals("cssa") ){
			renderArgs.put("connectedCSSA", connectedCSSA());
		}
		
	}
    public static void index() {
    	String userType = Utils.getUserType();
    	if(userType==null){
    		render();
    	}else if(userType.equals("simple")){
    		SimpleUsers.infoCenter();
    	}else{
    		CSSAs.infoCenter();
    	}
    }
    static SimpleUser connectedSimple() {
    	System.out.println(Utils.getUserId()+"ddddddddddddd");
		return SimpleUser.findById(Utils.getUserId());
	}
    static CSSA connectedCSSA() {
		return CSSA.findById(Utils.getUserId());
	}
	@Before
	static void checkSecure() {
		Secure secure = getActionAnnotation(Secure.class);
		if (secure != null) {
			if (connectedSimple() == null
					|| (secure.admin() && !connectedSimple().isAdmin())) {
				forbidden();
			}
		}
	}
    @Before
    static void rememberedUser(){
    	//response.cookies.get("email").value;
    	
    }
    
    @Before
	static void checkMessages() {
		String userType = session.get("usertype");
		String userID = session.get("logged");
		if (userID != null) {
			renderArgs.put("mailCount", Messaging.getMailCount(userType, Long.valueOf(userID)));
			renderArgs.put("notificationCount", Messaging.getNotificationCount(userType, Long.valueOf(userID)));
		}
	}
}