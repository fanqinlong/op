package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

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
		List<Tag> t = Tag.findAll();
		render(t);
	}

	public static void dispAddQues(@Required String title,
			@Required String Tag, @Required String content, String school,
			String date, Long id, long answerNum, long focusNum,
			String selfIntro) {
		if (session.get("logged") == null) {
			flash.error("请登录!");
			SimpleUsers.login();
		}
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
		} else if (Tag == null) {
			validation.keep();
			params.flash();
			flash.error("错误:至少选择一个标签");
			QuesIndex();
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
		if (comment.equals("<br>")) {
			flash.error("答案不能为空！");
			showQuesInfo(quesid);
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

	public static void searchPage(long id, long question_id) {
		List<Tag> t = Tag.find("order by themeid desc").fetch();
		List<Ques> aQues = Ques.find("order by date desc").fetch(5);
		long pageCount = Ques.count() % 5 == 0 ? Ques.count() / 5 : (Ques
				.count() / 5 + 1);
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
		render(t, qArticles, pageCount, contentIsEmpty, topQues, topUser);
	}

	public static void searchQues(String ques) {
		List<Tag> t = Tag.find("order by themeid desc").fetch();
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
		boolean contentIsEmpty = false;
		if (qArticles.isEmpty()) {
			contentIsEmpty = true;
		}
		List<Comments> topUser = Comments.find("order by praiseNum desc")
				.fetch(5);
		List<Ques> topQues = Ques.find("order by focusNum desc").fetch(5);
		renderTemplate("QuestAnsw/searchPage.html", qArticles, t, pageCount,
				contentIsEmpty, topUser, topQues);
	}

	public static void searchTag(String tag) {
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
		boolean contentIsEmpty = false;
		if (qArticles.isEmpty()) {
			contentIsEmpty = true;
		}
		List<Tag> t = Tag.find("order by themeid desc").fetch();
		List<Comments> topUser = Comments.find("order by praiseNum desc")
				.fetch(5);
		List<Ques> topQues = Ques.find("order by focusNum desc").fetch(5);
		renderTemplate("QuestAnsw/searchPage.html", qArticles, t, pageCount,
				contentIsEmpty, topUser, topQues, tagid);
	}

	public static void showQuesInfo(long id) {
		Ques qw = Ques.findById(id);
		qw.views = qw.views + 1;
		qw.save();
		if (session.get("logged") == null) {
			Ques fQ = Ques.findById(id);
			String s = new String(fQ.label);
			String a[] = s.split(",");
			List<Ques> relatedQues = Ques
					.find("SELECT a FROM Ques a WHERE label LIKE ?",
							"%" + a[0] + "%").fetch(5);
			List<FocusQues> FQ = FocusQues.find("quesId = ?", id).fetch(5);
			List<Comments> listCom = Comments.find("quesid = ?", id).fetch();
			render(fQ, FQ, listCom, relatedQues);
		} else {
			String comentUsertype = session.get("usertype");
			long comentUserid = Long.parseLong(session.get("logged"));
			String comentUsername;
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

			List<Ques> aboutQues = Ques.find(
					"SELECT a FROM Ques a WHERE label LIKE ?",
					"%" + qw.label + "%").fetch(5);

			render(fQ, listCom, comentUsername, FQ, UserQues, CssaQues,
					UserComments, CssaComment, userid, comentUsertype,
					UserFocusQues, CSSAFocusQues, comentUserid, isSimple,
					isCSSA, aboutQues, relatedQues);
		}
	}

	public static void AttentionQues(long userId, long quesId) {
		Attention att = new Attention(userId, quesId);
		render(att);
	}

	public static void Quespaging(int pageNum) {
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
		List<Comments> topUser = Comments.find("order by praiseNum desc")
				.fetch(5);
		List<Ques> topQues = Ques.find("order by focusNum desc").fetch(5);
		renderTemplate("QuestAnsw/searchPage.html", qArticles, t, pageCount,
				pageNum, topUser, topQues);
	}

	public static void editQues(long id) {
		List<Tag> t = Tag.findAll();
		Ques eQues = Ques.findById(id);
		render(t, eQues);
	}
	public static void editSuccessful(Ques eQues) {
		eQues.save();
		render();
	}

	/**
	 * public static void deleteQues(long id, int pageNum) { Ques dques =
	 * Ques.findById(id); dques.delete(); List<Tag> t = Tag.findAll(); long
	 * pageCount = Ques.count() % 5 == 0 ? Ques.count() / 5 : (Ques .count() / 5
	 * + 1); if (pageNum < 1) { pageNum = 1; } else if (pageNum >= pageCount) {
	 * pageNum = (int) pageCount; } List<Ques> aQues =
	 * Ques.find("order by date desc") .from((pageNum - 1) * 5).fetch(5);
	 * Iterator iterator = aQues.iterator(); List<QuestionArticle> qArticles =
	 * new ArrayList<QuestionArticle>(); while (iterator.hasNext()) { Ques ques
	 * = (Ques) iterator.next(); List comments = Comments.find("quesid = ?",
	 * ques.id).fetch(); Comments comment = comments.isEmpty() ? null :
	 * (Comments) comments .get(0); QuestionArticle qa = new
	 * QuestionArticle(ques, comment); qArticles.add(qa); }
	 * renderTemplate("QuestAnsw/searchPage.html", qArticles, t, pageCount,
	 * pageNum); }
	 **/
	public static void deleteComent() {
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

	public static void searchSchool(String school) {
		List<Tag> t = Tag.find("order by themeid desc").fetch();
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
		boolean contentIsEmpty = false;
		if (qArticles.isEmpty()) {
			contentIsEmpty = true;
		}
		List<Comments> topUser = Comments.find("order by praiseNum desc")
				.fetch(5);
		List<Ques> topQues = Ques.find("order by focusNum desc").fetch(5);
		renderTemplate("QuestAnsw/searchPage.html", qArticles, t, pageCount,
				contentIsEmpty, topUser, topQues);
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
		render(findCom, fQ, FQ, username);
	}

	public static void editComSuccessful(Comments c) {
		System.out.println("kakka"+c.id);
		System.out.println("内容"+c.comment);
		if(c.comment.equals("<br>")){
			flash.error("答案不能为空!");
			editComent(c.userid, c.usertype, c.quesid);
		}
		c.save();
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
		System.out.println("不赞同这条回答");

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

	/**
	 * public static void orderbyTime() { List<Ques> anq =
	 * Ques.find("order by date desc").fetch(5); }
	 * 
	 * public static void orderbyAnswerNumber() { System.out.println("1111");
	 * List<Ques> anq = Ques.find("order by answerNum desc").fetch(5); List<Tag>
	 * t = Tag.find("order by themeid desc").fetch(); long pageCount =
	 * Ques.count() % 5 == 0 ? Ques.count() / 5 : (Ques .count() / 5 + 1);
	 * Iterator iterator = anq.iterator(); List<QuestionArticle> qArticles = new
	 * ArrayList<QuestionArticle>(); while (iterator.hasNext()) { Ques qu =
	 * (Ques) iterator.next(); List comments = Comments.find("quesid = ?",
	 * qu.id).fetch(); Comments comment = comments.isEmpty() ? null : (Comments)
	 * comments .get(0); QuestionArticle qa = new QuestionArticle(qu, comment);
	 * qArticles.add(qa); } boolean contentIsEmpty = false; if
	 * (qArticles.isEmpty()) { contentIsEmpty = true; }
	 * renderTemplate("QuestAnsw/searchPage.html", qArticles, t, pageCount,
	 * contentIsEmpty); }
	 * 
	 * public static void orderbyFocusNumber() { List<Ques> anq =
	 * Ques.find("order by focusNum desc").fetch(5); }
	 * 
	 * public static void bestRespondents(){ List<Comments> com =
	 * Comments.find("order by praiseNum desc").fetch(10); List<Ques> q =
	 * Ques.find("order by focusNum desc").fetch(10); }
	 **/
}
