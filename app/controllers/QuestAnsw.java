package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import play.data.validation.Validation;
import models.qa.AgreeComment;
import models.qa.Attention;
import models.qa.Comments;
import models.qa.FocusQues;
import models.qa.Ques;
import models.qa.QuestionArticle;
import models.qa.Tag;
import models.users.CSSA;
import models.users.SimpleUser;

public class QuestAnsw extends Application {
	private static String label;

	public static void index() {
		render();
	}

	public static void QuesIndex() {
		List<Tag> t = Tag.findAll();
		render(t);
	}

	public static void dispAddQues(String title, String Tag, String content,
			String school, String date, Long id, long answerNum, long focusNum,
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
			username = cssa.name;
			userprofile = cssa.profile;
			userselfIntro = cssa.selfIntro;
		}
		if (!Ques.isTitle(title)) {
			validation.keep();
			params.flash();
			flash.error("错误:请输入标题名");
			QuesIndex();
		} else if (!Ques.isContent(content)) {
			validation.keep();
			params.flash();
			flash.error("错误:请输入内容");
			QuesIndex();
		} else if (!Ques.islabel(label)) {
			validation.keep();
			params.flash();
			flash.error("错误:至少选择一个标签");
			QuesIndex();
		}

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss ");
		String d = (df.format(Calendar.getInstance().getTime()));
		new Ques(title, Tag, school, content, d, userid, usertype, username,
				userprofile, answerNum, focusNum, userselfIntro);
		render();
	}

	public static void comments(String qid, String comment) {
		long quesid = Long.parseLong(qid);
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
		} else {
			CSSA cssa = CSSA.findById(comentUserid);
			comentUsername = cssa.name;
			comentUserprofile = cssa.profile;
			comentUserSelfIntro = cssa.selfIntro;
		}
		String QuesTitle;
		Ques ques = Ques.findById(quesid);
		QuesTitle = ques.title;
		ques.answerNum = ques.answerNum + 1;
		ques.save();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss ");
		String d = (df.format(Calendar.getInstance().getTime()));
		new Comments(quesid, comment, 0, comentUserid, comentUsertype,
				comentUsername, comentUserprofile, comentUserSelfIntro, d,
				QuesTitle, 0);
		renderHtml(comment);
	}

	public static void addComent(long quesid, String comment, long praiseNum,
			long userid, String usertype, String username, String userprofile,
			String userSelfIntro, String date) {
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
			comentUsername = cssa.name;
			comentUserprofile = cssa.profile;
			comentUserSelfIntro = cssa.selfIntro;
			uid = cssa.id;
		}
		String QuesTitle;
		Ques ques = Ques.findById(quesid);
		QuesTitle = ques.title;
		ques.answerNum = ques.answerNum + 1;
		ques.save();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss ");
		String d = (df.format(Calendar.getInstance().getTime()));
		new Comments(quesid, comment, 0, comentUserid, comentUsertype,
				comentUsername, comentUserprofile, comentUserSelfIntro, d,
				QuesTitle, 0);
		Ques fQ = Ques.findById(quesid);

		List<FocusQues> FQ = FocusQues.find("quesId = ?", quesid).fetch(5);
		List<Comments> listCom = Comments.find("quesid = ?", quesid).fetch();

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
			if (!userThisQues.isEmpty()) {
				UserQues = true;
			} else if (!cssaThisQues.isEmpty()) {
				CssaQues = true;
			}
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
			if (!userThisComment.isEmpty()) {
				UserComments = true;
			} else if (!cssaThisComment.isEmpty()) {
				CssaComment = true;
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
			if (!userThisComment.isEmpty()) {
				UserFocusQues = true;
			} else if (!userThisFocusQues.isEmpty()) {
				CSSAFocusQues = true;
			}
		}
		renderTemplate("QuestAnsw/showQuesInfo.html", fQ, listCom,
				comentUsername, FQ, UserQues, CssaQues, UserComments,
				CssaComment, userid, UserFocusQues, CSSAFocusQues,comentUserid,comentUsertype);
	}

	public static void searchPage(long id, long question_id) {
		List<Tag> t = Tag.findAll();
		List<Ques> aQues = Ques.find("order by date desc").fetch(5);
		long pageCount = Ques.count() % 5 == 0 ? Ques.count() / 5 : (Ques
				.count() / 5 + 1);
		Iterator iterator = aQues.iterator();
		List<QuestionArticle> qArticles = new ArrayList<QuestionArticle>();
		while (iterator.hasNext()) {
			Ques ques = (Ques) iterator.next();
			List comments = Comments.find("quesid = ?", ques.id).fetch();
			Comments comment = comments.isEmpty() ? null : (Comments) comments
					.get(0);
			QuestionArticle qa = new QuestionArticle(ques, comment);
			qArticles.add(qa);
		}
		String userType = session.get("usertype");
		long userId = Long.parseLong(session.get("logged"));
		
		render(t, qArticles, pageCount,userType,userId);
	}

	public static void searchQues(String ques) {
		List<Tag> t = Tag.findAll();
		List<Ques> anq = Ques.find("SELECT a FROM Ques a WHERE title LIKE ?",
				"%" + ques + "%").fetch(5);

		long pageCount = Ques.count() % 5 == 0 ? Ques.count() / 5 : (Ques
				.count() / 5 + 1);
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
		
		String userType = session.get("usertype");
		long userId = Long.parseLong(session.get("logged"));
		
		renderTemplate("QuestAnsw/searchPage.html", qArticles, t, pageCount,userType,userId);
	}

	public static void searchTag(String tag) {
		List<Ques> anq = Ques.find("SELECT a FROM Ques a WHERE label LIKE ?",
				"%" + tag + "%").fetch(5);
		long pageCount = Ques.count() % 5 == 0 ? Ques.count() / 5 : (Ques
				.count() / 5 + 1);
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
		List<Tag> t = Tag.findAll();
		
		
		String userType = session.get("usertype");
		long userId = Long.parseLong(session.get("logged"));
		
		renderTemplate("QuestAnsw/searchPage.html", qArticles, t, pageCount,userType,userId);
	}

	public static void showQuesInfo(long id) {
		String comentUsertype = session.get("usertype");
		long comentUserid = Long.parseLong(session.get("logged"));
		String comentUsername;
		String userprofile;
		String userSelfIntro;
		Long userid;
		if (comentUsertype.equals("simple")) {
			SimpleUser su = SimpleUser.findById(comentUserid);
			comentUsername = su.name;
			userprofile = su.profile;
			userSelfIntro = su.selfIntro;
			userid = su.id;
		} else {
			CSSA cssa = CSSA.findById(comentUserid);
			comentUsername = cssa.name;
			userprofile = cssa.profile;
			userSelfIntro = cssa.selfIntro;
			userid = cssa.id;
		}

		Ques fQ = Ques.findById(id);

		List<FocusQues> FQ = FocusQues.find("quesId = ?", id).fetch(5);
		List<Comments> listCom = Comments.find("quesid = ?", id).fetch();

		// 判断当前用户是否可以修改删除问题问题
		Boolean UserQues = false;
		Boolean CssaQues = false;

		List<Ques> userThisQues = Ques
				.find("id = ? and userid = ? and usertype = ? ", id, userid,
						"simple").fetch();

		List<Ques> cssaThisQues = Ques.find(
				"id = ? and userid = ? and usertype = ? ", id, userid, "cssa")
				.fetch();
		if (!userThisQues.isEmpty() || !cssaThisQues.isEmpty()) {
			if (!userThisQues.isEmpty()) {
				UserQues = true;
			} else if (!cssaThisQues.isEmpty()) {
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
			if (!userThisComment.isEmpty()) {
				UserComments = true;
			} else if (!cssaThisComment.isEmpty()) {
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
			if (!userThisComment.isEmpty()) {
				UserFocusQues = true;
			} else if (!userThisFocusQues.isEmpty()) {
				CSSAFocusQues = true;
			}
		}
		render(fQ, listCom, comentUsername, FQ, UserQues, CssaQues,
				UserComments, CssaComment, userid, comentUsertype, UserFocusQues,
				CSSAFocusQues,comentUserid);
	}

	public static void AttentionQues(long userId, long quesId) {
		Attention att = new Attention(userId, quesId);
		render(att);
	}

	public static void popupUserInfo() {
		render();
	}

	public static void userInfoIndex() {
		render();
	}

	public static void Quespaging(int pageNum, String data) {
		List<Tag> t = Tag.findAll();
		long pageCount = Ques.count() % 5 == 0 ? Ques.count() / 5 : (Ques
				.count() / 5 + 1);
		if (pageNum < 1) {
			pageNum = 1;
		} else if (pageNum >= pageCount) {
			pageNum = (int) pageCount;
		}
		List<Ques> aQues = Ques.find("order by date desc")
				.from((pageNum - 1) * 5).fetch(5);

		// if(){
		// List<Ques> aQues = Ques.find("order by answerNum desc")
		// .from((pageNum - 1) * 5).fetch(5);
		// }else if(){
		// List<Ques> aQues = Ques.find("order by focusNum desc")
		// .from((pageNum - 1) * 5).fetch(5);
		// }else{
		// List<Ques> aQues = Ques.find("order by date desc")
		// .from((pageNum - 1) * 5).fetch(5);
		// }

		Iterator iterator = aQues.iterator();
		List<QuestionArticle> qArticles = new ArrayList<QuestionArticle>();
		while (iterator.hasNext()) {
			Ques ques = (Ques) iterator.next();
			List comments = Comments.find("quesid = ?", ques.id).fetch();
			Comments comment = comments.isEmpty() ? null : (Comments) comments
					.get(0);
			QuestionArticle qa = new QuestionArticle(ques, comment);
			qArticles.add(qa);
		}
		renderTemplate("QuestAnsw/searchPage.html", qArticles, t, pageCount,
				pageNum);
	}

	public static void editQues(long id) {
		List<Tag> t = Tag.findAll();
		Ques eQues = Ques.findById(id);
		render(t, eQues);
	}

	public static void editSuccessful(Ques q) {
		q.save();
		render();
	}

	public static void deleteQues(long id, int pageNum) {
		Ques dques = Ques.findById(id);
		dques.delete();
		List<Tag> t = Tag.findAll();
		long pageCount = Ques.count() % 5 == 0 ? Ques.count() / 5 : (Ques
				.count() / 5 + 1);
		if (pageNum < 1) {
			pageNum = 1;
		} else if (pageNum >= pageCount) {
			pageNum = (int) pageCount;
		}
		List<Ques> aQues = Ques.find("order by date desc")
				.from((pageNum - 1) * 5).fetch(5);
		Iterator iterator = aQues.iterator();
		List<QuestionArticle> qArticles = new ArrayList<QuestionArticle>();
		while (iterator.hasNext()) {
			Ques ques = (Ques) iterator.next();
			List comments = Comments.find("quesid = ?", ques.id).fetch();
			Comments comment = comments.isEmpty() ? null : (Comments) comments
					.get(0);
			QuestionArticle qa = new QuestionArticle(ques, comment);
			qArticles.add(qa);
		}
		renderTemplate("QuestAnsw/searchPage.html", qArticles, t, pageCount,
				pageNum);
	}

	public static void deleteComent() {
		render();
	}

	public static void fcousOnQuestion(long id) {
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
			CSSA cssa = CSSA.findById(id);
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

		showQuesInfo(id);
	}

	public static void SearchAnswNum() {
		List<Tag> t = Tag.findAll();
		List<Ques> aQues = Ques.find("order by answerNum DESC").fetch(5);
		long pageCount = Ques.count() % 5 == 0 ? Ques.count() / 5 : (Ques
				.count() / 5 + 1);
		Iterator iterator = aQues.iterator();
		List<QuestionArticle> qArticles = new ArrayList<QuestionArticle>();
		while (iterator.hasNext()) {
			Ques ques = (Ques) iterator.next();
			List comments = Comments.find("quesid = ?", ques.id).fetch();
			Comments comment = comments.isEmpty() ? null : (Comments) comments
					.get(0);
			QuestionArticle qa = new QuestionArticle(ques, comment);
			qArticles.add(qa);
		}
		render(t, qArticles, pageCount);
	}

	public static void searchSchool(String school) {
		System.out.println("查找 学校:" + school);
		List<Tag> t = Tag.findAll();
		List<Ques> anq = Ques.find("SELECT a FROM Ques a WHERE school LIKE ?",
				"%" + school + "%").fetch(5);
		long pageCount = Ques.count() % 5 == 0 ? Ques.count() / 5 : (Ques
				.count() / 5 + 1);
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
		
		String userType = session.get("usertype");
		long userId = Long.parseLong(session.get("logged"));
		
		renderTemplate("QuestAnsw/searchPage.html", qArticles, t, pageCount,userType,userId);
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
		renderTemplate("SimpleUsers/infoCenter.html", user, UQues, UComment,
				FQues);
	}

	public static void cssaQues() {
		long userId = Long.parseLong(session.get("logged"));
		CSSA user = CSSA.findById(userId);

		List<Ques> CQues = Ques
				.find("userid = ?  and usertype = ? order by id desc", userId,
						"cssa").fetch();

		List<Comments> CComment = Comments.find(
				"userid = ? and usertype =? order by id desc", userId, "cssa")
				.fetch();

		List<FocusQues> CFQues = FocusQues.find(
				"userid = ? and userType = ? order by id desc", userId, "cssa")
				.fetch();
		notFoundIfNull(user);
		renderTemplate("CSSAs/infoCenter.html", user, CQues, CComment, CFQues);
	}

	public static void showUserInfor(String usertype, long userid) {
		if (usertype.equals("simple")) {
			SimpleUser user = SimpleUser.findById(userid);
			List<Ques> UQues = Ques.find(
					"userid = ?  and usertype = ? order by id desc", userid,
					"simple").fetch(5);

			List<Comments> UComment = Comments.find(
					"userid = ? and usertype =? order by id desc", userid,
					"simple").fetch(5);

			List<FocusQues> FQues = FocusQues.find(
					"userid = ? and userType = ? order by id desc", userid,
					"simple").fetch(5);
			notFoundIfNull(user);
			renderTemplate("SimpleUsers/infoCenter.html", user, UQues,
					UComment, FQues);
		} else if (usertype.equals("cssa")) {
			CSSA user = CSSA.findById(userid);

			List<Ques> CQues = Ques.find(
					"userid = ?  and usertype = ? order by id desc", userid,
					"cssa").fetch(5);

			List<Comments> CComment = Comments.find(
					"userid = ? and usertype =? order by id desc", userid,
					"cssa").fetch(5);

			List<FocusQues> CFQues = FocusQues.find(
					"userid = ? and userType = ? order by id desc", userid,
					"cssa").fetch(5);
			notFoundIfNull(user);
			renderTemplate("CSSAs/infoCenter.html", user, CQues, CComment,
					CFQues);
		}
	}

	public static void editComent(Long userid, String userType,Long quesid) {
		List<Comments> come = Comments
				.find("SELECT a FROM Comments a WHERE userid LIKE ? and usertype like ? and quesid like ?",
						userid, "%" + userType + "%",quesid).fetch();
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
		System.out.println("用户id:"+userid);
		System.out.println("用户类型:"+userType);
		System.out.println("答案id:"+comid);

		Comments findCom = Comments.findById(comid);

		Ques fQ = Ques.findById(Quesid);
		System.out.println("看看找到的是什么："+fQ);
		List<FocusQues> FQ = FocusQues.find("quesId = ?", Quesid).fetch(5);
		System.out.println("关注的问题："+FQ);
		render(findCom, fQ, FQ, username);
	}

	public static void editComSuccessful(Comments c) {
		c.save();
		render();
	}

	public static void cancelFqcusQues(Long quesid) {
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
		showQuesInfo(quesid);
	}

	/**
	 * 点击添加赞同，再点击取消赞
	 */

	public static void notAgree(Long id, Long quesid) {
		// 传过来的id是回答的id
		String fquserType = session.get("usertype");
		long userId = Long.parseLong(session.get("logged"));
		List<AgreeComment> agreeCom = AgreeComment
				.find("userid = ? and quesId =? and commentsid = ? and  userType = ?",
						userId, quesid, id, fquserType).fetch();
		System.out.println(agreeCom);
		String quesTitle;

		boolean notEmpty = false;

		if (!agreeCom.isEmpty()) {
			notEmpty = true;
			System.out.println("不为空");
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
			System.out.println("数据库里面是空，走到这是添加了站台");
			flash.error("您赞同了这条回答！");
			Comments com = Comments.findById(id);
			com.praiseNum = com.praiseNum + 1;
			quesTitle = com.quesTitle;
			com.save();
			new AgreeComment(fquserType, userId, id, quesid, quesTitle);
			showQuesInfo(quesid);
		}
	}

	/**
	 * public static void goodAnswer(Long id, Long quesid) { String fquserType =
	 * session.get("usertype"); long userId =
	 * Long.parseLong(session.get("logged")); List<AgreeComment> agreeCom =
	 * AgreeComment
	 * .find("userid = ? and quesId =? and commentsid = ? and  userType = ?",
	 * userId, quesid, id, fquserType).fetch(); String quesTitle; if
	 * (!agreeCom.isEmpty()) { flash.error("您已经赞同过了这个问答"); showQuesInfo(quesid);
	 * } else { System.out.println("数据库里面是空，走到这是添加了站台");
	 * flash.error("您赞同了这条回答！"); Comments com = Comments.findById(id);
	 * com.praiseNum = com.praiseNum + 1; quesTitle = com.quesTitle; com.save();
	 * new AgreeComment(fquserType, userId, id, quesid, quesTitle);
	 * showQuesInfo(quesid); } }
	 * 
	 * public static void notAgree(Long id, Long quesid) { String fquserType =
	 * session.get("usertype"); long userId =
	 * Long.parseLong(session.get("logged")); List<AgreeComment> agreeCom =
	 * AgreeComment
	 * .find("userid = ? and quesId =? and commentsid = ? and  userType = ?",
	 * userId, quesid, id, fquserType).fetch(); String quesTitle; if
	 * (!agreeCom.isEmpty()) { flash.error("您已经反对了这条回答"); showQuesInfo(quesid);
	 * } else { System.out.println("数据库里面是空，走到这是添加了站台");
	 * flash.error("您反对了这条回答！"); Comments com = Comments.findById(id);
	 * com.hateNum = com.hateNum + 1; quesTitle = com.quesTitle; com.save(); new
	 * AgreeComment(fquserType, userId, id, quesid, quesTitle);
	 * showQuesInfo(quesid); } }
	 **/

	public static void changeComent(Long comentUserid, String comentUsertype) {
		List<Comments> come = Comments
				.find("SELECT a FROM Comments a WHERE userid LIKE ? and usertype like ?",
						comentUserid, "%" + comentUsertype + "%").fetch();
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
		System.out.println("用户id:"+comentUserid);
		System.out.println("用户类型:"+comentUsertype);
		System.out.println("答案id:"+comid);
		
		System.out.println("问题id:"+Quesid);
		System.out.println("username:"+username);

		Comments findCom = Comments.findById(comid);
		
		Ques fQ = Ques.findById(Quesid);
		List<FocusQues> FQ = FocusQues.find("quesId = ?", Quesid).fetch(5);
		renderTemplate("QuestAnsw/editComent.html",findCom, fQ, FQ, username);
	}
	
	
}
