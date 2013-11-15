package controllers;

import java.util.List;

import models.airport.School;
import models.airport.StuInfo;
import models.airport.VolInfo;
import models.users.CSSA;
import models.users.SimpleUser;
import play.data.validation.Validation;
import play.mvc.*;

public class Airports extends Application {

	@Before
	public static void isCSSA() {
		if (session.get("logged") != null
				&& !session.get("usertype").equals("simple")) {
			CSSA cssa = CSSA.findById(Long.parseLong(session.get("logged")));
			if (cssa.school.name != null == true)
				renderArgs.put("isCSSA", true);
		}
	}

	@Before
	public static void isSimpleUser() {
		if (session.get("logged") != null
				&& session.get("usertype").equals("simple")) {
			SimpleUser simp = SimpleUser.findById(Long.parseLong(session
					.get("logged")));
			if (simp.name != null == true)
				renderArgs.put("isSimpleUser", true);
		}
	}

	@Before
	public static void isLogged() {
		if (session.get("usertype") == null) {
			SimpleUsers.login();
		}
	}

	public static void index() {
		String usertype = session.get("usertype");
		String username = null;
		if (!usertype.equals("simple")) {
			CSSA cssa = CSSA.findById(Long.parseLong(session.get("logged")));
			username = cssa.school.name;
		}
		List ls = School.findAll();
		render(ls, username);
	}

	public static void addStuInfo(Long id) {
		School sch = School.findById(id);
		String explain = sch.synopsis;
		Long schoolId = sch.id;
		String schoolName = sch.name;
		
		String stuName;
		String stuhometown;
		SimpleUser simp = SimpleUser.findById(Long.parseLong(session
				.get("logged")));
		stuName=simp.name;
		stuhometown = simp.hometown;
		
		render(schoolId, explain, schoolName,stuName,stuhometown);
	}

	public static void doAddStuInfo(StuInfo s, Long schoolId) {
		Long sid = schoolId;
		
//		final Validation.ValidationResult validationResult = validation
//				.valid(s);
//		if (!validationResult.ok) {
//			validation.keep();
//			params.flash();
//			flash.error("请更正错误。");
//			addStuInfo(sid);
//		}
		
		if(s.name.equals("")){
			System.out.println("111111");
			validation.keep();
			params.flash();
			flash.error("请填写正确的姓名");
			addStuInfo(sid);
		}else if (s.gender == null){
			validation.keep();
			params.flash();
			flash.error("请选择性别");
			addStuInfo(sid);
		}else if (s.phone.equals("")) {
			validation.keep();
			params.flash();
			flash.error("请填写电话号码");
			addStuInfo(sid);
		}else if (s.email.equals("")) {
			validation.keep();
			params.flash();
			flash.error("请填写正确的邮箱");
			addStuInfo(sid);
		}else if (s.major.equals("")){
			validation.keep();
			params.flash();
			flash.error("请填写专业");
			addStuInfo(sid);
		}else if (s.airport.equals("")) {
			validation.keep();
			params.flash();
			flash.error("请正确填写机票上的目的地");
			addStuInfo(sid);
		}else if (s.flight.equals("")) {
			validation.keep();
			params.flash();
			flash.error("请正确填写机票上的航班号");
			addStuInfo(sid);
		}else if (s.date.equals("")) {
			validation.keep();
			params.flash();
			flash.error("请正确填写机票上的到达时间");
			addStuInfo(sid);
		}else if (s.luggage.equals("")) {
			validation.keep();
			params.flash();
			flash.error("请描述行李大小，方便我们安排车型接机");
			addStuInfo(sid);
		}
		s.save();
		render();
	}

	public static void addVolInfo(Long id) {
		School sch = School.findById(id);
		String explain = sch.synopsis;
		Long schoolId = sch.id;
		String schoolName = sch.name;
		render(schoolId, explain, schoolName);
	}

	public static void doAddVolInfo(VolInfo v, Long schoolId) {
		Long sid = schoolId;
//		final Validation.ValidationResult vr = validation.valid(v);
//		if (!vr.ok) {
//			validation.keep();
//			params.flash();
//			flash.error("请更正错误。");
//			addVolInfo(sid);
//		}
		if(v.name.equals("")){
			validation.keep();
			params.flash();
			flash.error("请填写正确的姓名");
			addVolInfo(sid);
		}else if (v.gender == null){
			validation.keep();
			params.flash();
			flash.error("请选择性别");
			addVolInfo(sid);
		}else if (v.email.equals("")) {
			validation.keep();
			params.flash();
			flash.error("请填写正确的邮箱");
			addVolInfo(sid);
		}else if (v.phone.equals("")) {
			validation.keep();
			params.flash();
			flash.error("请填写电话号码");
			addVolInfo(sid);
		}else if (v.car.equals("")) {
			validation.keep();
			params.flash();
			flash.error("请填写你的车型");
			addVolInfo(sid);
		}else if (v.date.equals("")) {
			validation.keep();
			params.flash();
			flash.error("请填写你方便接机的时间");
			addVolInfo(sid);
		}else if (v.remarks.equals("")) {
			validation.keep();
			params.flash();
			flash.error("请填写你的备注");
			addVolInfo(sid);
		}
		v.save();
		render();
	}

	public static void stuInfo(String username) {
		List<StuInfo> stu = StuInfo.find(
				"SELECT a FROM StuInfo a WHERE school LIKE ?",
				"%" + username + "%").fetch();
		render(stu, username);
	}

	public static void volInfoInfo(String username) {
		List<VolInfo> vol = VolInfo.find(
				"SELECT a FROM VolInfo a WHERE school LIKE ?",
				"%" + username + "%").fetch();
		render(vol, username);
	}
}
