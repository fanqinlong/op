package controllers;

import models.*;
import models.activity.Activity;
import models.activity.Liker;
import models.charity.Wel;
import models.charity.welLiker;
import models.index.IndexStore;
import models.index.SolrContent;
import models.qa.Paging;
import models.qa.Ques;
import models.users.CSSA;
import models.users.SimpleUser;
import notifiers.Trend;
import play.*;
import play.db.jpa.JPABase;
import play.libs.Files;
import play.mvc.*;
import play.mvc.Scope.Session;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

import org.apache.solr.client.solrj.SolrServerException;


public class Charities extends Application {
	
	static int Number = 5;
	
    @Before
    public static void isAdmin() {
        if ((session.get("logged") != null) &&
                session.get("usertype").equals("simple")) {
            SimpleUser su = SimpleUser.findById(Long.parseLong(session.get(
                            "logged")));

            if (su.isAdmin == true) {
                renderArgs.put("isAdmin", true);
            }
        }
    }

    @Before
    public static void islogged() {
        if (session.get("logged") == null) {
            renderArgs.put("islogged", true);
        }
    }

    public static void fabu() {
        if (session.get("logged") == null) {
            Charities.pigination(1);
        }
        else {
            SimpleUser su = SimpleUser.findById(Long.parseLong(session.get(
                            "logged")));

            if (su.isAdmin == false) {
                Charities.smfabu();
            }
        }
        render();
    }

    public static void smfabu() {
        if (session.get("logged") == null) {
            Charities.pigination(1);
        }

        render();
    }

    public static void smindex() {
        if (session.get("logged") == null) {
            Charities.pigination(1);
        }
        render();
    }

    public static void WelSave(String title, String content, String time,
        File f, String generalize, int likerCount, boolean isChecked,
        String fromUser) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss ");
        String d = (df.format(Calendar.getInstance().getTime()));

        // 上传图片
        if (title.equals("")) {
            validation.keep();
            params.flash();
            flash.error("标题不能为空");
            fabu();
        } else if (f == null) {
            validation.keep();
            params.flash();
            flash.error("请上传封面图片");
            fabu();
        } else if (content.equals("")) {
            validation.keep();
            params.flash();
            flash.error("内容不能为空");
            fabu();
        } else if (generalize.equals("")) {
            validation.keep();
            params.flash();
            flash.error("说明不能为空");

            fabu();
        }
        else {
            String fileName = f.getName();
            String extName = fileName.substring(fileName.lastIndexOf("."));
            UUID uuid = UUID.randomUUID();
            fileName = uuid.toString() + extName;

            String path = "/public/images/upload/" + fileName;
            Files.copy(f, Play.getFile(path));
            Wel w = new Wel(title, content, d, path, generalize, likerCount, true, fromUser);
            IndexStore.getInstance().addIndexWel(w);
            pigination(1);
        }
    }

    public static void SmWelSave(String title, String content, String time,
        File f, String generalize, int likerCount, boolean isChecked,
        String fromUser) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss ");
        String d = (df.format(Calendar.getInstance().getTime()));

        // 上传图片
        if (title.equals("")) {
            validation.keep();
            params.flash();
            flash.error("标题不能为空");
            fabu();
        } else if (f == null) {
            validation.keep();
            params.flash();
            flash.error("请上传封面图片");
            fabu();
        } else if (content.equals("")) {
            validation.keep();
            params.flash();
            flash.error("内容不能为空");
            fabu();
        } else if (generalize.equals("")) {
            validation.keep();
            params.flash();
            flash.error("说明不能为空");
            fabu();
        }
        else {
            String fileName = f.getName();
            String extName = fileName.substring(fileName.lastIndexOf("."));
            UUID uuid = UUID.randomUUID();
            fileName = uuid.toString() + extName;
            String path = "/public/images/upload/" + fileName;
            Files.copy(f, Play.getFile(path));
            new Wel(title, content, d, path, generalize, likerCount, false, fromUser);
            renderTemplate("Charities/SmWelSave.html");
        }
    }

    public static void pass(Long id) {
        Wel w = Wel.findById(id);
        w.isChecked = true;
        w.save();
        IndexStore.getInstance().addIndexWel(w);
        render(w);
    }


    public static void edit(long id, int pageNo) {
        if (session.get("logged") == null) {
            Charities.pigination(1);
        }
        SimpleUser su = SimpleUser.findById(Long.parseLong(session.get("logged")));

        if (su.isAdmin == false) {
            Charities.pigination(1);
        }
        Wel w = Wel.findById(id);
        IndexStore.getInstance().updateIndexWel(w);
        render(w, pageNo);
    }

    public static void update(Wel w, File f, int pageNo) {
        if (f == null) {
            w.save();
        } else {
            String fileName = f.getName();
            String extName = fileName.substring(fileName.lastIndexOf("."));
            UUID uuid = UUID.randomUUID();
            fileName = uuid.toString() + extName;
            String path = "/public/images/upload/" + fileName;
            Files.copy(f, Play.getFile(path));
            w.f = path;
            w.save();
           IndexStore.getInstance().updateIndexWel(w);
         
        }
        pigination(pageNo);
    }

    public static void welfare(long id) {
        Wel w = Wel.findById(id);
        w.views = w.views + 1;
        w.save();
        IndexStore.getInstance().updateIndexWel(w);
        render(w);
    }
 
    
    
    public static void pigination(int pageNo) {
        if (session.get("logged") == null) {
            int count = Wel.find("isChecked=true order by time desc").fetch().size();
            int pageCount = ((count % Number) == 0) ? (count / Number) : ((count / Number) + 1);
            if (pageNo < 1) {
    			pageNo = 1;
    		} else if (pageNo >= pageCount && pageNo > 1) {
    			pageNo = (int) pageCount;
    		}
            List<Wel> we = Wel.find("isChecked=true order by time desc")
                              .from((pageNo - 1) * Number).fetch(Number);
            int[] inter = Paging.getRount(8, pageCount, pageNo);
            renderTemplate("Charities/wel.html", we, pageCount, pageNo,inter);
        }

        SimpleUser su = SimpleUser.findById(Long.parseLong(session.get("logged")));

        if (((session.get("logged") != null) &&
                session.get("usertype").equals("simple")) ||
                session.get("usertype").equals("cssa")) {
            if (su.isAdmin) {
                long pageCount = ((Wel.count() % Number) == 0) ? (Wel.count() / Number)
                                                          : ((Wel.count() / Number) +
                    1);

                if (pageNo < 1) {
        			pageNo = 1;

        		} else if (pageNo >= pageCount&& pageNo > 1) {
        			pageNo = (int) pageCount;
        		}
                List<Wel> we = Wel.find("order by time desc")
                                  .from((pageNo - 1) * Number).fetch(Number);
                
                int[] inter = Paging.getRount(8, (int)pageCount, pageNo);
                renderTemplate("Charities/wel.html", we, pageCount, pageNo,inter);
            } else {
                int count = Wel.find("isChecked=true order by likerCount desc")
                               .fetch().size();
                int pageCount = ((count % Number) == 0) ? (count / Number) : ((count / Number) +
                    1);

                if (pageNo < 1) {
        			pageNo = 1;

        		} else if (pageNo >= pageCount && pageNo > 1) {
        			pageNo = (int) pageCount;
        		}
                List<Wel> we = Wel.find(
                        "isChecked=true order by likerCount desc")
                                  .from((pageNo - 1) * Number).fetch(Number);
                
                int[] inter = Paging.getRount(8, pageCount, pageNo);

                renderTemplate("Charities/wel.html", we, pageCount, pageNo,inter);
              
            }
        }
    }

    public static void like(long aid, int pageNo) {
        if (session.get("logged") == null) {
            SimpleUsers.login();
        }

        String userType = session.get("usertype");
        long userId = Long.parseLong(session.get("logged"));
        String usertype = session.get("usertype");
        List al_exist = welLiker.find("aid = ? and lid = ? and ltype = ? ",
                aid, userId, usertype).fetch();

        if (!al_exist.isEmpty()) {
            flash.error("您已关注");
            pigination(pageNo);
        }

        welLiker al = new welLiker();
        al.aid = aid;
        al.lid = userId;
        al.ltype = usertype;
        al.save();

        Wel w = Wel.findById(aid);
        w.likerCount = w.likerCount + 1;
        w.save();
        IndexStore.getInstance().updateIndexWel(w);
        if (userType.equals("cssa")) {
            CSSA cssa = CSSA.findById(userId);

            Trend tend = new Trend(Utils.getNowTime(), null, cssa, null, null,
                    w, "关注了", "wel");
            tend.save();
        } else {
            SimpleUser simp = SimpleUser.findById(userId);

            Trend tend = new Trend(Utils.getNowTime(), simp, null, null, null,
                    w, "关注了", "wel");
            tend.save();
        }
        flash.success("关注成功");
        pigination(pageNo);
    }
}
