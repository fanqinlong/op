package controllers;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import play.data.validation.Email;
import play.data.validation.Required;
import play.modules.excel.RenderExcel;

import models.airport.StuInfo;
import models.ordering.Dishes;
import models.ordering.Menu;
import models.ordering.Orders;
import models.ordering.Restaurant;

/**
 * 订餐模块
 * @author summer
 *
 */
public class Ordering extends Application{
	
	/**
	 * 登录之前
	 */
	public static void preLogin(){
		renderTemplate("Ordering/login.html");
	}
	/***
	 * 登录操作
	 * @param email
	 * @param password
	 */
	public static void login(String email,String password){
		Restaurant restaurant = Restaurant.findByEmail(email);
		if (restaurant == null) {
			flash.error("邮箱不存在。");
			preLogin();
		}else if(!restaurant.checkPassword(password)){
			flash.error("密码错误。");
			preLogin();
		}else{
			session.put("signed", restaurant.id);
			view(restaurant.id);
		}
	}
	
	
	
	/**
	 * 订餐预览页面，每个商家都有不同的id进行登录
	 * @param id
	 */
	public static void preview(long id){
		Restaurant res = Restaurant.findById(id);
		List<Menu> menus =  Menu.find("SELECT m FROM Menu m WHERE m.restaurant.id = ?", res.id).fetch();
		
		render(res,menus);
	}
	
	/**
	 * 订单生成页面，用户选自己的喜欢的菜谱，并且输入自己信息，然后提交
	 * @param id
	 */
	public static void execute(long id){
		List<Menu> menus =  Menu.find("SELECT m FROM Menu m WHERE m.restaurant.id = ?", id).fetch();
		render(menus);
	}
	/**
	 * 用户点餐的所有操作
	 * @param dishesId
	 * @param number
	 * @param remark
	 * @param userName
	 * @param restaurantId
	 * @param phone
	 * @param weixin
	 * @param email
	 * @param qq
	 * @param time
	 * @param location
	 */
	public static void produce(
			Long [] menuId, 
			Long [] number,
			String remark,
			@Required String userName,
			@Required int restaurantId,
			@Required long phone,
			@Required String weixin,
			@Required @Email String email,
			@Required long qq,
			@Required Date time,
			@Required String location){
		if("".equals(userName)){
			validation.keep();
			params.flash();
			flash.error("请输入姓名");
			execute(restaurantId);
		}
		if(phone==0){
			validation.keep();
			params.flash();
			flash.error("请输入电话号/输入错误");
			execute(restaurantId);
		}
		if("".equals(weixin)){
			validation.keep();
			params.flash();
			flash.error("请输入微信号");
			execute(restaurantId);
		}
		if("".equals(email)){
			validation.keep();
			params.flash();
			flash.error("请输入邮箱");
			execute(restaurantId);
		}
		if(qq==0){
			validation.keep();
			params.flash();
			flash.error("请输入QQ/QQ不合法");
			execute(restaurantId);
		}
		if(time==null){
			validation.keep();
			params.flash();
			flash.error("请选择时间");
			execute(restaurantId);
		}
		if("".equals(location)){
			validation.keep();
			params.flash();
			flash.error("请选择送餐地点");
			execute(restaurantId);
		}
		
		String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		Random random = new Random();
		StringBuffer sb=new StringBuffer(3);
		for (int i = 0; i < 4; i++) {
			sb.append(random.nextInt(9));
		}
		String orderNum = date+sb;//订单号
		float totalPrice=0;
		Orders order = null;
		List<Dishes> dishes = new ArrayList<Dishes>();
		if(number.length==0){
			validation.keep();
			params.flash();
			flash.error("请至少选择一种菜品");
			execute(restaurantId);
		}else{
			long timeStamp = time.getTime();
			long orderTime = System.currentTimeMillis();
			order = new Orders(totalPrice, orderNum, userName, email, qq, phone, weixin, timeStamp, orderTime,location, "未完成", restaurantId,false,false,remark);
			
			for(int i=0;i<number.length;i++){
				if(number[i]!=0){
					Menu m  = Menu.findById(menuId[i]);
					totalPrice+=number[i]*m.price;
					dishes.add( new Dishes(m,number[i],order));
				}
			}
		}
		
		renderTemplate("Ordering/produce.html",totalPrice, order,dishes);
	}
	/**
	 * 用户生成订单提交
	 * @param id
	 */
	public static void submit(long id){
		Orders order = Orders.findById(id);
		order.status = "等待派送";
		order.isSubmit = true;
		order.save();
		renderTemplate("Ordering/success.html",order);
	}
	
	/**
	 * 订单查看，通过商家id来查看商家自己的信息
	 * @param id
	 */
	public static void view(long id){
		if(session.get("signed")==null){
			preLogin();
		}else{
			int wait = Orders.find("SELECT o FROM Orders o WHERE o.status LIKE ? and o.restaurantId = ?","等待派送",id).fetch().size();
			int doing = Orders.find("SELECT o FROM Orders o WHERE o.status LIKE ? and o.restaurantId = ?","正在派送",id).fetch().size();
			int done = Orders.find("SELECT o FROM Orders o WHERE o.status LIKE ? and o.restaurantId = ?","交易成功",id).fetch().size();
			renderTemplate("Ordering/businessView.html",wait,doing,done,id);
		}
	}
	/**
	 * 订单显示操作
	 * @param id
	 */
	public static void operate(long id,String st,String start,String end,String keyWord){
		if(session.get("signed")==null){
			preLogin();
		}else{
			String condition="";//sql语句的条件
			if(st==null){
				condition+="";
			}else if(Integer.parseInt(st)==1){
				condition+="";
			}else if(Integer.parseInt(st)==2){
				st="等待派送";
				condition+=" and o.status = '"+st+"' ";
			}else if(Integer.parseInt(st)==3){
				st="正在派送";
				condition+=" and o.status = '"+st+"' ";
			}else if(Integer.parseInt(st)==4){
				st="交易成功";
				condition+=" and o.status = '"+st+"' ";
			}
		
			long startTemp = 0;
			long endTemp = 0;
			try {
				SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
				if(!(start== null || start.length() <= 0)){
					startTemp = formatDate.parse(start).getTime();
					condition+=" and  o.orderTime >='"+startTemp+"' ";
				}
				if(!(end== null || end.length() <= 0)){
					endTemp = formatDate.parse(end).getTime();
					condition+=" and  o.orderTime<= '"+endTemp+"' ";
				}
				

				if(!(keyWord== null || keyWord.length() <= 0)){
					condition+=" and ( o.orderNumber like '"+"%"+keyWord+"%"+"' or  o.userName like '"+"%"+keyWord+"%"
							+"'  or  o.email like '"+"%"+keyWord+"%"+"'   or  o.location like '"+"%"+keyWord+"%"
							+"'   or  o.phone like '"+"%"+keyWord+"%"+"'   or  o.qq like '"+"%"+keyWord+"%"
							+"'   or  o.weixin like '"+"%"+keyWord+"%"+"' )";
					
				}
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//搜索这家餐厅的订单，并且订单是提交过的，并且是没有被删除的
			List<Orders> orders = Orders.find("SELECT o FROM Orders o WHERE o.restaurantId = '"+id+"' and o.isSubmit = '"+1+"' and o.isDelete = '"+0+"' "+condition).fetch();
			renderTemplate("Ordering/businessOperate.html",orders,st,keyWord,start,end,id);
		}
	}
	/**
	 * 改变订单状态
	 * @param id
	 * @param staus
	 */
	public static void changeStatus(long id,String status,String keyWord,String start,String end){
		if(session.get("signed")==null){
			preLogin();
		}else{
			Orders order = Orders.findById(id);
			order.status = status;
			order.save();
			operate(order.restaurantId,status,keyWord,start,end);
		}
	}
	/**
	 * 删除订单，改变isDelete属性，false为没有被删除
	 * true为已经被删除，在回收站中可以看到
	 * @param id
	 */
	public static void delete(long id,String status,String keyWord,String start,String end){
		if(session.get("signed")==null){
			preLogin();
		}else{
			Orders order = Orders.findById(id);
			order.isDelete = true;
			order.save();
			operate(order.restaurantId,status,keyWord,start,end);
		}
	}
	/**
	 * 到处excel表格
	 * @param id 餐厅ID
	 */
	public static void exportOrders(long id) {
		request.format = "xlsx";
		List<Orders> orders = Orders.find(
				"SELECT o FROM Orders o WHERE o.restaurantId = ? and o.isSubmit = ? and o.isDelete = ?",
				id ,true, false).fetch();
		Restaurant restaurant = Restaurant.findById(id);
		String __FILE_NAME__ = restaurant.rName + "_餐厅订单信息.xlsx";	
		renderArgs.put(RenderExcel.RA_ASYNC, true);
		render(__FILE_NAME__, orders);
	}
}
