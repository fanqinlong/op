package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import models.qa.Paging;
import notifiers.Trend;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.mvc.Before;
import models.activity.Activity;
import models.qa.AgreeComment;
import models.qa.Attention;
import models.qa.ChildComment;
import models.qa.Comments;
import models.qa.FocusQues;
import models.qa.Ques;
import models.qa.QuestionArticle;
import models.qa.Tag;
import models.users.CSSA;
import models.users.SimpleUser;

public class QuestAnsw extends Application {
	public static void index() {
		render();
	}

	public static void QuesIndex() {
		if (session.get("logged") == null) {
			session.put("Askquestions", "goToQuesindex");
			flash.error("请登录!");
			SimpleUsers.login();
		} else {
			List<Tag> t = Tag.findAll();
			render(t);
		}
	}

	public static void dispAddQues(@Required String title,
			@Required String Tag, @Required String content, String school,
			String date, Long id, long answerNum, long focusNum,
			String selfIntro) {
		String usertype = session.get("usertype");
		long userid = Long.parseLong(session.get("logged"));
		String username;
		String userprofile;
		String userselfIntro;
		if (usertype.equals("simple")) {
			SimpleUser simp = SimpleUser.findById(userid);
			username = simp.name;
			userprofile = simp.profile;
			userselfIntro = simp.selfIntro;
		} else {
			CSSA cssa = CSSA.findById(userid);
			username = cssa.school.name;
			userprofile = cssa.profile;
			userselfIntro = cssa.selfIntro;
		}
		String string;
		
		string=content.replace("<br>", "");
		string=string.replace("</div>", "");
		string=string.replace("<div>", "");
		string=string.replace("&nbsp;", "");
		string=string.replace(" ", "");
		if (title.equals("")) {
			validation.keep();
			params.flash();
			flash.error("错误:请输入标题名");
			QuesIndex();
		} 
		if ("".equals(string)) {
			validation.keep();
			params.flash();
			flash.error("错误:请输入内容");
			QuesIndex();
		}
		if(Tag == null) {
			validation.keep();
			params.flash();
			flash.error("错误:至少选择一个标签");
			QuesIndex();
		}

		if (!school.isEmpty()) {
			String s = new String(school);
			String a[] = s.split(" - ");
			school = a[0] + a[1] + a[2];
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss ");
		String d = (df.format(Calendar.getInstance().getTime()));
		new Ques(title, Tag, school, content, d, userid, usertype, username,
				userprofile, answerNum, 1, userselfIntro);
		Ques ques = Ques.find("order by id desc").first();
		new FocusQues(usertype, userid, userprofile, ques.id, ques.title);
		if (usertype.equals("simple")) {
			SimpleUser sUser = SimpleUser.findById(userid);
			Ques q = Ques.find("order by id desc").first();
			Trend tend = new Trend(Utils.getNowTime(), sUser, null, null, null,
					q, "发布了", "addques", null);
			tend.save();
		} else {
			CSSA cssa = CSSA.findById(userid);
			Ques q = Ques.find("order by id desc").first();
			Trend tend = new Trend(Utils.getNowTime(), null, cssa, null, null,
					q, "发布了", "addques", null);
			tend.save();
		}
		render();
	}

	public static void addComent(long quesid, String comment, long praiseNum,
			long userid, String usertype, String username, String userprofile,
			String userSelfIntro, String date) {
		if (session.get("logged") == null) {
			flash.error("请登录后回答!");
			SimpleUsers.login();
		}
		Long uid;
		long comentUserid = Long.parseLong(session.get("logged"));
		String comentUsertype = session.get("usertype");
		String comentUsername;
		String comentUserprofile;
		String comentUserSelfIntro;
		if (comentUsertype.equals("simple")) {
			SimpleUser simp = SimpleUser.findById(comentUserid);
			comentUsername = simp.name;
			comentUserprofile = simp.profile;
			comentUserSelfIntro = simp.selfIntro;
			uid = simp.id;
		} else {
			CSSA cssa = CSSA.findById(comentUserid);
			comentUsername = cssa.school.name;
			comentUserprofile = cssa.profile;
			comentUserSelfIntro = cssa.selfIntro;
			uid = cssa.id;
		}

		// if(!userThisComment.isEmpty() || !cssaThisComment.isEmpty()){
		// validation.keep();
		// params.flash();
		// flash.error("不能重复提交或者答案不能为空！");
		// showQuesInfo(quesid);
		// }

		if (comment.equals("")) {
			validation.keep();
			params.flash();
			flash.error("不能重复提交或者答案不能为空！");
			showQuesInfo(quesid);
		}

		String QuesTitle;
		Ques ques = Ques.findById(quesid);
		QuesTitle = ques.title;
		ques.answerNum = ques.answerNum + 1;
		ques.focusNum = ques.focusNum + 1;
		ques.save();
		String s = new String(ques.label);
		String a[] = s.split(",");
		List<Ques> relatedQues = Ques.find(
				"SELECT a FROM Ques a WHERE label LIKE ?", "%" + a[0] + "%")
				.fetch(5);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss ");
		String d = (df.format(Calendar.getInstance().getTime()));
		new Comments(quesid, comment, 0, comentUserid, comentUsertype,
				comentUsername, comentUserprofile, comentUserSelfIntro, d,
				QuesTitle, 0);
		Ques fQ = Ques.findById(quesid);
		new FocusQues(comentUsertype, comentUserid, comentUserprofile, fQ.id,
				fQ.title);
		List<FocusQues> FQ = FocusQues.find("quesId = ?", quesid).fetch(5);
		List<Comments> listCom = Comments.find("quesid = ?", quesid).fetch();
		boolean isSimple = false;
		boolean isCSSA = false;
		if (comentUsertype.equals("simple")) {
			isSimple = true;
		} else {
			isCSSA = true;
		}

		// 判断是否回答过这个问题
		Boolean UserComments = false;
		Boolean CssaComment = false;

		List<Comments> userThisComment = Comments.find(
				"quesid = ? and userid = ? and usertype = ? ", quesid, uid,
				"simple").fetch();

		List<Comments> cssaThisComment = Comments.find(
				"quesid = ? and userid = ? and usertype = ? ", quesid, uid,
				"cssa").fetch();
		if (!userThisComment.isEmpty() || !cssaThisComment.isEmpty()) {
			if (!userThisComment.isEmpty() && comentUsertype.equals("simple")) {
				UserComments = true;
			} else if (!cssaThisComment.isEmpty()
					&& comentUsertype.equals("cssa")) {
				CssaComment = true;
			}
		}

		// 判断当前用户是否可以修改删除问题问题
		Boolean UserQues = false;
		Boolean CssaQues = false;

		List<Ques> userThisQues = Ques.find(
				"id = ? and userid = ? and usertype = ? ", quesid, uid,
				"simple").fetch();

		List<Ques> cssaThisQues = Ques.find(
				"id = ? and userid = ? and usertype = ? ", quesid, uid, "cssa")
				.fetch();
		if (!userThisQues.isEmpty() || !cssaThisQues.isEmpty()) {
			if (!userThisQues.isEmpty() && comentUsertype.equals("simple")) {
				UserQues = true;
			} else if (!cssaThisQues.isEmpty() && comentUsertype.equals("cssa")) {
				CssaQues = true;
			}
		}
		// 这里是判断是否已关注
		Boolean UserFocusQues = false;
		Boolean CSSAFocusQues = false;

		List<FocusQues> userThisFocusQues = FocusQues.find(
				"quesId = ? and userid = ? and userType = ? ", quesid, uid,
				"simple").fetch();

		List<FocusQues> cssaThisFocusQues = FocusQues.find(
				"quesId = ? and userid = ? and userType = ? ", quesid, uid,
				"cssa").fetch();
		if (!userThisFocusQues.isEmpty() || !cssaThisFocusQues.isEmpty()) {
			if (!userThisComment.isEmpty() && comentUsertype.equals("simple")) {
				UserFocusQues = true;
			} else if (!userThisFocusQues.isEmpty()
					&& comentUsertype.equals("cssa")) {
				CSSAFocusQues = true;
			}
		}
		/***
		 * 通知，回答了用户的问题
		 * 
		 * **/

		String noticName;
		if (comentUsertype.equals("simple")) {
			SimpleUser sUser = SimpleUser.findById(comentUserid);
			noticName = sUser.name;
			Ques q = Ques.findById(quesid);
			if (q.usertype.equals("simple")) {
				SimpleUser simpleUser = SimpleUser.findById(q.userid);
				Trend tend = new Trend(Utils.getNowTime(), sUser, null,
						simpleUser, null, q, "回答了", "qa", null);
				tend.save();
			} else {
				CSSA cs = CSSA.findById(q.userid);
				Trend tend = new Trend(Utils.getNowTime(), sUser, null, null,
						cs, q, "回答了", "qa", null);
				tend.save();
			}
		} else {
			CSSA cssa = CSSA.findById(comentUserid);
			noticName = cssa.email;
			Ques q = Ques.findById(quesid);
			if (q.usertype.equals("simple")) {
				SimpleUser simpleUser = SimpleUser.findById(q.userid);
				Trend tend = new Trend(Utils.getNowTime(), null, cssa,
						simpleUser, null, q, "回答了", "qa", null);
				tend.save();
			} else {
				CSSA cs = CSSA.findById(q.userid);
				Trend tend = new Trend(Utils.getNowTime(), null, cssa, null,
						cs, q, "回答了", "qa", null);
				tend.save();
			}
		}
		// 个人中心动态
		ArrayList<String> notification = new ArrayList();
		notification.add(noticName);
		notification.add("回答了您的问题");
		notification.add(ques.id + "");
		notification.add(QuesTitle);
		if (comentUsertype.equals("simple")) {
			Messaging.addNotification(ques.usertype, ques.userid, "qa",
					notification);
		} else {
			Messaging.addNotification(ques.usertype, ques.userid, "qa",
					notification);
		}

		List<Ques> aboutQues = Ques.find(
				"SELECT a FROM Ques a WHERE label LIKE ?",
				"%" + ques.label + "%").fetch(5);
		renderTemplate("QuestAnsw/showQuesInfo.html", fQ, listCom,
				comentUsername, FQ, UserQues, CssaQues, UserComments,
				CssaComment, userid, UserFocusQues, CSSAFocusQues,

				comentUserid, comentUsertype, isSimple, isCSSA, aboutQues,
				relatedQues);
	}

	public static void showQuesInfo(long id) {
		session.put("qusetionId", id);
		Ques qw = Ques.findById(id);
		qw.views = qw.views + 1;
		qw.save();
		boolean isLoged = false;
		
		if (session.get("logged") == null) {
			Ques fQ = Ques.findById(id);
			String s = new String(fQ.label);
			String a[] = s.split(",");
			List<Ques> relatedQues = Ques
					.find("SELECT a FROM Ques a WHERE label LIKE ?",
							"%" + a[0] + "%").fetch(5);
			List<FocusQues> FQ = FocusQues.find("quesId = ?", id).fetch(5);
			List<Comments> listCom = Comments.find("quesid = ?", id).fetch();
			isLoged = true;
			render(fQ, FQ, listCom, relatedQues,isLoged);
		} else {
			String comentUsertype = session.get("usertype");
			long comentUserid = Long.parseLong(session.get("logged"));
			String comentUsername;
			isLoged = false;
			Long userid;
			if (comentUsertype.equals("simple")) {
				SimpleUser su = SimpleUser.findById(comentUserid);
				comentUsername = su.name;
				userid = su.id;
			} else {
				CSSA cssa = CSSA.findById(comentUserid);
				comentUsername = cssa.school.name;
				userid = cssa.id;
			}
			// 判断当前用户是否可以修改删除问题问题
			Boolean UserQues = false;
			Boolean CssaQues = false;
			List<Ques> userThisQues = Ques.find(
					"id = ? and userid = ? and usertype = ? ", id, userid,
					"simple").fetch();
			List<Ques> cssaThisQues = Ques.find(
					"id = ? and userid = ? and usertype = ? ", id, userid,
					"cssa").fetch();
			if (!userThisQues.isEmpty() || !cssaThisQues.isEmpty()) {
				if (!userThisQues.isEmpty() && comentUsertype.equals("simple")) {
					UserQues = true;
				} else if (!cssaThisQues.isEmpty()
						&& comentUsertype.equals("cssa")) {
					CssaQues = true;
				}
			}
			// 判断是否回答过这个问题
			Boolean UserComments = false;
			Boolean CssaComment = false;
			List<Comments> userThisComment = Comments.find(
					"quesid = ? and userid = ? and usertype = ? ", id, userid,
					"simple").fetch();
			List<Comments> cssaThisComment = Comments.find(
					"quesid = ? and userid = ? and usertype = ? ", id, userid,
					"cssa").fetch();
			if (!userThisComment.isEmpty() || !cssaThisComment.isEmpty()) {
				if (!userThisComment.isEmpty()
						&& comentUsertype.equals("simple")) {
					UserComments = true;
				} else if (!cssaThisComment.isEmpty()
						&& comentUsertype.equals("cssa")) {
					CssaComment = true;
				}
			}
			// 这里是判断是否已关注
			Boolean UserFocusQues = false;
			Boolean CSSAFocusQues = false;
			List<FocusQues> userThisFocusQues = FocusQues.find(
					"quesId = ? and userid = ? and userType = ? ", id, userid,
					"simple").fetch();
			List<FocusQues> cssaThisFocusQues = FocusQues.find(
					"quesId = ? and userid = ? and userType = ? ", id, userid,
					"cssa").fetch();
			if (!userThisFocusQues.isEmpty() || !cssaThisFocusQues.isEmpty()) {
				if (!userThisFocusQues.isEmpty()
						&& comentUsertype.equals("simple")) {
					UserFocusQues = true;
				} else if (!cssaThisFocusQues.isEmpty()
						&& comentUsertype.equals("cssa")) {
					CSSAFocusQues = true;
				}
			}
			Ques fQ = Ques.findById(id);
			String s = new String(fQ.label);
			String a[] = s.split(",");
			List<Ques> relatedQues = Ques
					.find("SELECT a FROM Ques a WHERE label LIKE ?",
							"%" + a[0] + "%").fetch(5);
			List<FocusQues> FQ = FocusQues.find("quesId = ?", id).fetch(5);
			List<Comments> listCom = Comments.find("quesid = ?", id).fetch();
			boolean isSimple = false;
			boolean isCSSA = false;
			if (comentUsertype.equals("simple")) {
				isSimple = true;
			} else {
				isCSSA = true;
			}
			render(fQ, listCom, comentUsername, FQ, UserQues, CssaQues,
					UserComments, CssaComment, userid, comentUsertype,
					UserFocusQues, CSSAFocusQues, comentUserid, isSimple,
					isCSSA, relatedQues,isLoged);
		}
	}

	public static void AttentionQues(long userId, long quesId) {
		Attention att = new Attention(userId, quesId);
		render(att);
	}

	public static void editQues(long id) {
		List<Tag> t = Tag.findAll();
		Ques eQues = Ques.findById(id);
		session.put("quesid", id);
		render(t, eQues);
	}

	public static void editSuccessful(Ques eQues,String Tag) {
		Long quesid = Long.parseLong(session.get("quesid"));
		if(quesid!=eQues.id){
			flash.error("操作错误，请正确操作!");
			editQues(eQues.id);
		}
		if(eQues.title.equals("")){
			flash.error("标题不能为空!");
			editQues(eQues.id);
		}else if(eQues.content.equals("")){
			flash.error("内容不能为空!");
			editQues(eQues.id);
		}
		Ques q = Ques.findById(eQues.id);
		q.label="";
		q.label = Tag;
		System.out.println(Tag);
		System.out.println(q.label);
		q.save();
		if(Tag==null){
			flash.error("至少要选择一个标签!");
			editQues(eQues.id);
		}
		eQues.save();
		render();
	}

	public static void fcousOnQuestion(long id) {
		if (session.get("logged") == null) {
			flash.error("请登录!");
			SimpleUsers.login();
		}
		String fquserType = session.get("usertype");
		long userId = Long.parseLong(session.get("logged"));
		List<FocusQues> foc = FocusQues.find(
				"userid = ? and quesId = ? and userType = ? ", userId, id,
				fquserType).fetch();
		if (!foc.isEmpty()) {
			flash.error("您已经关注");
			showQuesInfo(id);
		}
		long fquserid;
		String fquserprofile;
		if (fquserType.equals("simple")) {
			SimpleUser simpleUser = SimpleUser.findById(userId);
			fquserid = simpleUser.id;
			fquserprofile = simpleUser.profile;
		} else {
			CSSA cssa = CSSA.findById(userId);
			fquserid = cssa.id;
			fquserprofile = cssa.profile;
		}
		String QuesTitle;
		Ques ques = Ques.findById(id);
		QuesTitle = ques.title;
		ques.focusNum = ques.focusNum + 1;
		ques.save();
		flash.success("关注成功");
		new FocusQues(fquserType, fquserid, fquserprofile, id, QuesTitle);
		/**
		 * 通知关注了用户的问题
		 * 
		 **/
		String noticName;
		if (fquserType.equals("simple")) {
			SimpleUser sUser = SimpleUser.findById(userId);
			noticName = sUser.name;
			Ques q = Ques.findById(id);
			if (q.usertype.equals("simple")) {
				SimpleUser simpleUser = SimpleUser.findById(q.userid);
				Trend tend = new Trend(Utils.getNowTime(), sUser, null,
						simpleUser, null, q, "关注了", "fq", null);
				tend.save();
			} else {
				CSSA cs = CSSA.findById(q.userid);
				Trend tend = new Trend(Utils.getNowTime(), sUser, null, null,
						cs, q, "关注了", "fq", null);
				tend.save();
			}
		} else {
			CSSA cssa = CSSA.findById(userId);
			noticName = cssa.email;
			Ques q = Ques.findById(id);
			if (q.usertype.equals("simple")) {
				SimpleUser simpleUser = SimpleUser.findById(q.userid);
				Trend tend = new Trend(Utils.getNowTime(), null, cssa,
						simpleUser, null, q, "关注了", "fq", null);
				tend.save();
			} else {
				CSSA cs = CSSA.findById(q.userid);
				Trend tend = new Trend(Utils.getNowTime(), null, cssa, null,
						cs, q, "关注了", "fq", null);
				tend.save();
			}
		}

		ArrayList<String> notification = new ArrayList();
		notification.add(noticName);
		notification.add("关注了你的问题");
		notification.add(ques.id + "");
		notification.add(QuesTitle);
		if (fquserType.equals("simple")) {
			Messaging.addNotification(ques.usertype, ques.userid, "qa",
					notification);
		} else {
			Messaging.addNotification(ques.usertype, ques.userid, "qa",
					notification);
		}
		showQuesInfo(id);
	}

	public static void editComent(Long userid, String userType, Long quesid) {
		List<Comments> come = Comments
				.find("SELECT a FROM Comments a WHERE userid LIKE ? and usertype like ? and quesid like ?",
						userid, "%" + userType + "%", quesid).fetch();
		Iterator iterator = come.iterator();
		Long comid = null;
		Long Quesid = null;
		String username = null;
		while (iterator.hasNext()) {
			Comments com = (Comments) iterator.next();
			Quesid = com.quesid;
			comid = com.id;
			username = com.username;
		}
		Comments findCom = Comments.findById(comid);
		Ques fQ = Ques.findById(Quesid);
		List<FocusQues> FQ = FocusQues.find("quesId = ?", Quesid).fetch(5);
		session.put("commentId", findCom.id);

		String comentUsertype = session.get("usertype");
		boolean isSimple = false;
		boolean isCSSA = false;
		if (comentUsertype.equals("simple")) {
			isSimple = true;
		} else {
			isCSSA = true;
		}
		String s = new String(fQ.label);
		String a[] = s.split(",");
		List<Ques> relatedQues = Ques.find(
				"SELECT a FROM Ques a WHERE label LIKE ?", "%" + a[0] + "%")
				.fetch(5);
		Boolean UserFocusQues = false;
		Boolean CSSAFocusQues = false;
		List<FocusQues> userThisFocusQues = FocusQues.find(
				"quesId = ? and userid = ? and userType = ? ", fQ.id, userid,
				"simple").fetch();
		List<FocusQues> cssaThisFocusQues = FocusQues.find(
				"quesId = ? and userid = ? and userType = ? ", fQ.id, userid,
				"cssa").fetch();
		if (!userThisFocusQues.isEmpty() || !cssaThisFocusQues.isEmpty()) {
			if (!userThisFocusQues.isEmpty()
					&& comentUsertype.equals("simple")) {
				UserFocusQues = true;
			} else if (!cssaThisFocusQues.isEmpty()
					&& comentUsertype.equals("cssa")) {
				CSSAFocusQues = true;
			}
		}
		
		render(findCom, fQ, FQ, username,relatedQues,UserFocusQues,CSSAFocusQues,isSimple,isCSSA);
	}

	public static void editComSuccessful(Comments c) {
		long cid = Long.parseLong(session.get("commentId"));
		if(cid!=c.id){
			flash.error("操作错误!请正确操作");
			editComent(c.userid, c.usertype, c.quesid);
		}
		if (c.comment.equals("")) {
			flash.error("答案不能为空！");
			editComent(c.userid, c.usertype, c.quesid);
		}
		c.save();
		session.remove("commentId");
		render();
	}

	// 取消关注
	public static void cancelFqcusQues(Long quesid) {
		if (session.get("logged") == null) {
			flash.error("请登录!");
			SimpleUsers.login();
		}
		String fquserType = session.get("usertype");
		long userId = Long.parseLong(session.get("logged"));
		Long fQuesUesrid;

		if (fquserType.equals("simple")) {
			SimpleUser sim = SimpleUser.findById(userId);
			fQuesUesrid = sim.id;
		} else {
			CSSA cssa = CSSA.findById(userId);
			fQuesUesrid = cssa.id;
		}
		List<FocusQues> focusQues = FocusQues
				.find("SELECT a FROM FocusQues a WHERE userid LIKE ? and usertype like ? and quesId like ?",
						fQuesUesrid, "%" + fquserType + "%", quesid).fetch();
		Iterator iterator = focusQues.iterator();
		Long cfq = null;
		while (iterator.hasNext()) {
			FocusQues cancelFQ = (FocusQues) iterator.next();
			cfq = cancelFQ.id;
		}
		FocusQues fques = FocusQues.findById(cfq);
		fques.delete();
		Ques q = Ques.findById(quesid);
		q.focusNum = q.focusNum - 1;
		q.save();

		String noticName;
		if (fquserType.equals("simple")) {
			SimpleUser sUser = SimpleUser.findById(userId);
			noticName = sUser.name;
		} else {
			CSSA cssa = CSSA.findById(userId);
			noticName = cssa.email;
		}
		ArrayList<String> s = new ArrayList();

		if (fquserType.equals("simple")) {
			Messaging.addNotification(q.usertype, q.userid, noticName
					+ "取消了关注您的问题" + q.title, s);
		} else {
			Messaging.addNotification(q.usertype, q.userid, noticName
					+ "取消了关注您的问题" + q.title, s);
		}
		showQuesInfo(quesid);
	}

	/**
	 * 点击添加赞同，再点击取消赞
	 */

	public static void notAgree(Long id, Long quesid) {
		if (session.get("logged") == null) {
			flash.error("请登录!");
			SimpleUsers.login();
		}
		// 传过来的id是回答的id
		String fquserType = session.get("usertype");
		long userId = Long.parseLong(session.get("logged"));
		List<AgreeComment> agreeCom = AgreeComment
				.find("userid = ? and quesId =? and commentsid = ? and  userType = ?",
						userId, quesid, id, fquserType).fetch();
		String quesTitle;

		if (!agreeCom.isEmpty()) {
			Iterator iterator = agreeCom.iterator();
			Long Acid = null;
			while (iterator.hasNext()) {
				AgreeComment aComment = (AgreeComment) iterator.next();
				Acid = aComment.id;
			}
			AgreeComment a = AgreeComment.findById(Acid);
			a.delete();
			Comments com = Comments.findById(id);
			com.praiseNum = com.praiseNum - 1;
			quesTitle = com.quesTitle;
			com.save();
			flash.error("您取消了对这条回答的赞同");
			showQuesInfo(quesid);
		} else {
			flash.error("您赞同了这条回答！");
			Comments com = Comments.findById(id);
			com.praiseNum = com.praiseNum + 1;
			quesTitle = com.quesTitle;
			com.save();
			new AgreeComment(fquserType, userId, id, quesid, quesTitle);

			Comments comments = Comments.findById(id);

			if (fquserType.equals("simple")) {
				SimpleUser sUser = SimpleUser.findById(userId);
				Ques q = Ques.findById(quesid);
				ArrayList<String> notification = new ArrayList();
				notification.add(sUser.name);
				notification.add("赞同了你答案,点击查看");
				notification.add(q.id + "");
				notification.add(q.title);
				Messaging.addNotification(comments.usertype, comments.userid,
						"qa", notification);

				if (comments.usertype.equals("simple")) {
					SimpleUser simpleUser = SimpleUser
							.findById(comments.userid);
					Trend tend = new Trend(Utils.getNowTime(), sUser, null,
							simpleUser, null, q, "攒了你的回答", "praise", comments);
					tend.save();
				} else {
					CSSA cssa = CSSA.findById(comments.userid);
					Trend tend = new Trend(Utils.getNowTime(), sUser, null,
							null, cssa, q, "攒了你的回答", "praise", comments);
					tend.save();
				}
			} else {
				CSSA cssa = CSSA.findById(userId);
				Ques q = Ques.findById(quesid);
				ArrayList<String> notification = new ArrayList();
				notification.add(cssa.school.name);
				notification.add("赞同了你答案,点击查看");
				notification.add(q.id + "");
				notification.add(q.title);
				Messaging.addNotification(comments.usertype, comments.userid,
						"qa", notification);

				if (comments.usertype.equals("simple")) {
					SimpleUser simpleUser = SimpleUser
							.findById(comments.userid);
					Trend tend = new Trend(Utils.getNowTime(), null, cssa,
							simpleUser, null, q, "回答了", "praise", comments);
					tend.save();
				} else {
					CSSA cs = CSSA.findById(comments.userid);
					Trend tend = new Trend(Utils.getNowTime(), null, cssa,
							null, cs, q, "回答了", "praise", comments);
					tend.save();
				}
			}
			showQuesInfo(quesid);
		}
	}

	public static void allFollowers(Long id) {
		Ques ques = Ques.findById(id);
		List<FocusQues> allFoll = FocusQues.find("quesId = ?", id).fetch();
		render(ques, allFoll);
	}

	public static void addChildComment(Long quesid, Long cUserid,
			String cUserName, String cUserType, String comments) {
		if (session.get("logged") == null) {
			flash.error("请登录!");
			SimpleUsers.login();
		} else {
			String userType = session.get("usertype");
			long userId = Long.parseLong(session.get("logged"));
			String userName = null;
			if (userType.equals("cssa")) {
				CSSA cssa = CSSA.findById(userId);
				userName = cssa.school.name;
			} else {
				SimpleUser simp = SimpleUser.findById(userId);
				userName = simp.name;
			}
			new ChildComment(cUserid, cUserName, cUserType, quesid, comments,
					userId, userName, userType);
		}
		render();
	}

	public static void userQues() {
		long userId = Long.parseLong(session.get("logged"));
		SimpleUser user = SimpleUser.findById(userId);
		List<Ques> UQues = Ques.find(
				"userid = ?  and usertype = ? order by id desc", userId,
				"simple").fetch();

		List<Comments> UComment = Comments
				.find("userid = ? and usertype =? order by id desc", userId,
						"simple").fetch();

		List<FocusQues> FQues = FocusQues.find(
				"userid = ? and userType = ? order by id desc", userId,
				"simple").fetch();
		notFoundIfNull(user);
		render(user, UQues, UComment, FQues);
	}

	public static void searchPage(int flag, int currentPage, int pageSize) {
		String sign = "searchPage";
		long rowsCount = Ques.count(); // 总记录数
		long pageCount = rowsCount % pageSize == 0 ? rowsCount / pageSize
				: (rowsCount / pageSize + 1);// 总页数
		List<Tag> t = Tag.find("order by themeid desc").fetch();
		List<Ques> aQues = null;
		if (flag == 1) {
			aQues = Ques.find("order by id desc").fetch(currentPage, pageSize);
		} else if (flag == 2) {
			aQues = Ques.find("order by date desc")
					.fetch(currentPage, pageSize);
		} else if (flag == 3) {
			aQues = Ques.find("order by answerNum desc").fetch(currentPage,
					pageSize);
		} else if (flag == 4) {
			aQues = Ques.find("order by focusNum desc").fetch(currentPage,
					pageSize);
		}
		Iterator iterator = aQues.iterator();
		List<QuestionArticle> qArticles = new ArrayList<QuestionArticle>();
		while (iterator.hasNext()) {
			Ques ques = (Ques) iterator.next();
			List<Comments> comments = Comments.find("quesid = ?", ques.id)
					.fetch();
			Comments comment = comments.isEmpty() ? null : (Comments) comments
					.get(0);
			QuestionArticle qa = new QuestionArticle(ques, comment);
			qArticles.add(qa);
		}
		boolean contentIsEmpty = false;
		if (qArticles.isEmpty()) {
			contentIsEmpty = true;
		}
		List<Comments> topUser = Comments.find("order by praiseNum desc")
				.fetch(5);
		List<Ques> topQues = Ques.find("order by focusNum desc").fetch(5);
		int[] inter = Paging.getRount(8, (int) pageCount, currentPage);
		render(t, qArticles, pageCount, contentIsEmpty, topQues, topUser, flag,
				sign, currentPage, inter);
	}

	public static void searchQues(int quesSortFlag, String ques,
			int currentPage, int pageSize) {
		
		String sign = "searchQues";
		long rowsCount = Ques
				.find("SELECT a FROM Ques a WHERE title LIKE ?",
						"%" + ques + "%").fetch().size();// 总记录数
		long pageCount = rowsCount % pageSize == 0 ? rowsCount / pageSize
				: (rowsCount / pageSize + 1);// 总共多少页
		List<Tag> t = Tag.find("order by themeid desc").fetch();
		List<Ques> anq = null;
		if (quesSortFlag == 1) {
			anq = Ques
					.find("SELECT a FROM Ques a WHERE title LIKE  ?  order by id desc",
							"%" + ques + "%").fetch(currentPage, pageSize);
		} else if (quesSortFlag == 2) {
			anq = Ques
					.find("SELECT a FROM Ques a WHERE title LIKE  ?  order by date desc",
							"%" + ques + "%").fetch(currentPage, pageSize);
		} else if (quesSortFlag == 3) {
			anq = Ques
					.find("SELECT a FROM Ques a WHERE title LIKE  ?  order by answerNum desc",
							"%" + ques + "%").fetch(currentPage, pageSize);
		} else if (quesSortFlag == 4) {
			anq = Ques
					.find("SELECT a FROM Ques a WHERE title LIKE  ?  order by focusNum desc",
							"%" + ques + "%").fetch(currentPage, pageSize);
		}
		Iterator iterator = anq.iterator();
		List<QuestionArticle> qArticles = new ArrayList<QuestionArticle>();
		while (iterator.hasNext()) {
			Ques qu = (Ques) iterator.next();
			List comments = Comments.find("quesid = ?", qu.id).fetch();
			Comments comment = comments.isEmpty() ? null : (Comments) comments
					.get(0);
			QuestionArticle qa = new QuestionArticle(qu, comment);
			qArticles.add(qa);
		}
		boolean contentIsEmpty = false;
		if (qArticles.isEmpty()) {
			contentIsEmpty = true;
		}
		List<Comments> topUser = Comments.find("order by praiseNum desc")
				.fetch(5);
		List<Ques> topQues = Ques.find("order by focusNum desc").fetch(5);
		int[] inter = Paging.getRount(8, (int) pageCount, currentPage);
		renderTemplate("QuestAnsw/searchPage.html", qArticles, t, pageCount,
				contentIsEmpty, topUser, topQues, sign, quesSortFlag, ques,
				inter, currentPage);
	}

	public static void searchSchool(int shoolSortFlag, String myschool,
			int currentPage, int pageSize) {
		if(myschool.equals("")){
			flash.error("学校不能为空!");
			searchPage(1, 10, 1);
		}

		String sign = "searchSchool";
		String s = new String(myschool);
		String a[] = s.split(" - ");
		myschool = a[0] + a[1] + a[2];
		
		long rowsCount = Ques
				.find("SELECT a FROM Ques a WHERE school LIKE ?",
						"%" + myschool + "%").fetch().size();// 总记录数
		long pageCount = rowsCount % pageSize == 0 ? rowsCount / pageSize
				: (rowsCount / pageSize + 1);// 总共多少页
		List<Ques> anq = null;
		if (shoolSortFlag == 1) {
			anq = Ques
					.find("SELECT a FROM Ques a WHERE school LIKE  ?  order by id desc",
							"%" + myschool + "%").fetch(currentPage, pageSize);
		} else if (shoolSortFlag == 2) {
			anq = Ques
					.find("SELECT a FROM Ques a WHERE school LIKE  ?  order by date desc",
							"%" + myschool + "%").fetch(currentPage, pageSize);
		} else if (shoolSortFlag == 3) {
			anq = Ques
					.find("SELECT a FROM Ques a WHERE school LIKE  ?  order by answerNum desc",
							"%" + myschool + "%").fetch(currentPage, pageSize);
		} else if (shoolSortFlag == 4) {
			anq = Ques
					.find("SELECT a FROM Ques a WHERE school LIKE  ?  order by focusNum desc",
							"%" + myschool + "%").fetch(currentPage, pageSize);
		}
		Iterator iterator = anq.iterator();
		List<QuestionArticle> qArticles = new ArrayList<QuestionArticle>();
		while (iterator.hasNext()) {
			Ques qu = (Ques) iterator.next();
			List comments = Comments.find("quesid = ?", qu.id).fetch();
			Comments comment = comments.isEmpty() ? null : (Comments) comments
					.get(0);
			QuestionArticle qa = new QuestionArticle(qu, comment);
			qArticles.add(qa);
		}
		boolean contentIsEmpty = false;
		if (qArticles.isEmpty()) {
			contentIsEmpty = true;
		}
		List<Tag> t = Tag.find("order by themeid desc").fetch();
		List<Comments> topUser = Comments.find("order by praiseNum desc")
				.fetch(5);
		List<Ques> topQues = Ques.find("order by focusNum desc").fetch(5);
		int[] inter = Paging.getRount(8, (int) pageCount, currentPage);
		renderTemplate("QuestAnsw/searchPage.html", qArticles, t, pageCount,
				contentIsEmpty, topUser, topQues, sign, shoolSortFlag,
				myschool, inter, currentPage);
	}

	public static void searchTag(int sortFlag, String tag, int currentPage,
			int pageSize) {
		String sign = "searchTag";
		// 寻找tag的id
		List<Tag> tg = Tag.find("tagName = ?", tag).fetch();
		Iterator iter = tg.iterator();
		Long tagid = null;
		while (iter.hasNext()) {
			Tag ta = (Tag) iter.next();
			tagid = ta.id;
		}
		Tag tagg = Tag.findById(tagid);
		tagg.themeid = tagg.themeid + 1;
		tagg.save();

		String tagname = null;
		tagname = tagg.tagName;
		long rowsCount = Ques
				.find("SELECT a FROM Ques a WHERE label LIKE ?",
						"%" + tag + "%").fetch().size(); // 总记录数
		long pageCount = rowsCount % pageSize == 0 ? rowsCount / pageSize
				: (rowsCount / pageSize + 1);
		List<Ques> anq = null;
		if (sortFlag == 1) {
			anq = Ques.find(
					"SELECT a FROM Ques a WHERE label LIKE ? order by id desc",
					"%" + tag + "%").fetch(currentPage, pageSize);
		} else if (sortFlag == 2) {
			anq = Ques
					.find("SELECT a FROM Ques a WHERE label LIKE ? order by date desc",
							"%" + tag + "%").fetch(currentPage, pageSize);
		} else if (sortFlag == 3) {
			anq = Ques
					.find("SELECT a FROM Ques a WHERE label LIKE ? order by answerNum desc",
							"%" + tag + "%").fetch(currentPage, pageSize);
		} else if (sortFlag == 4) {
			anq = Ques
					.find("SELECT a FROM Ques a WHERE label LIKE ? order by focusNum desc",
							"%" + tag + "%").fetch(currentPage, pageSize);

		}
		Iterator iterator = anq.iterator();
		List<QuestionArticle> qArticles = new ArrayList<QuestionArticle>();
		while (iterator.hasNext()) {
			Ques qu = (Ques) iterator.next();
			List comments = Comments.find("quesid = ?", qu.id).fetch();
			Comments comment = comments.isEmpty() ? null : (Comments) comments
					.get(0);
			QuestionArticle qa = new QuestionArticle(qu, comment);
			qArticles.add(qa);
		}
		boolean contentIsEmpty = false;
		if (qArticles.isEmpty()) {
			contentIsEmpty = true;
		}
		List<Tag> t = Tag.find("order by themeid desc").fetch();
		List<Comments> topUser = Comments.find("order by praiseNum desc")
				.fetch(5);
		List<Ques> topQues = Ques.find("order by focusNum desc").fetch(5);
		int[] inter = Paging.getRount(8, (int) pageCount, currentPage);
		renderTemplate("QuestAnsw/searchPage.html", qArticles, t, pageCount,
				contentIsEmpty, topUser, topQues, tagid, tagname, sortFlag,
				sign, inter, currentPage);
	}
}
