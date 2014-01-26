package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import models.activity.Activity;
import models.charity.Wel;
import models.index.Index;
import models.index.SolrContent;
import models.index.IndexStore;
import models.qa.Paging;
import models.qa.Ques;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class IndexController extends Application {

	
	/**
	 * 重构索引
	 */
	public static void updateReconstructorIndex() {
		try {
			SolrContent.getInstance().getServer().deleteByQuery("*:*");
			List<Activity> activity = Activity.findAll();// 活动
			List<Ques> ques = Ques.findAll();// 问答
			List<Wel> wel = Wel.findAll();// 公益
			// 判断活动是否过期先获取系统当前时间
			String startDate = new SimpleDateFormat("yyyy-MM-dd")
					.format(Calendar.getInstance().getTime());
			List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
			for (Activity a : activity) {
				if (a.time.get(0).date.compareTo(startDate) == 1) {
					SolrInputDocument doc = new SolrInputDocument();
					doc.addField("id", "1" + a.id);
					doc.addField("msg_id", a.id);
					doc.addField(
							"msg_name",
							a.publisherSU == null ? (a.publisherCSSA.school.name + " CSSA")
									: a.publisherSU.name);
					doc.addField("msg_title", a.name);
					doc.addField("msg_content", a.intro);
					doc.addField("msg_date", a.postAt);
					doc.addField("msg_join", a.joiner.size());
					doc.addField("msg_attention", a.liker.size());
					doc.addField("msg_type", "1");
					docs.add(doc);
				}
			}
			for (Ques q : ques) {
				SolrInputDocument doc = new SolrInputDocument();
				doc.addField("id", "2" + q.id);
				doc.addField("msg_id", q.id);
				doc.addField("msg_name", q.username);
				doc.addField("msg_title", q.title);
				doc.addField("msg_content", q.content);
				doc.addField("msg_date", q.date);
				doc.addField("msg_join", q.answerNum);
				doc.addField("msg_attention", q.focusNum);
				doc.addField("msg_type", "2");
				docs.add(doc);
			}
			for (Wel w : wel) {
				SolrInputDocument doc = new SolrInputDocument();
				doc.addField("id", "3" + w.id);
				doc.addField("msg_id", w.id);
				doc.addField("msg_name", w.fromUser);
				doc.addField("msg_title", w.title);
				doc.addField("msg_content", w.content);
				doc.addField("msg_date", w.time);
				doc.addField("msg_join", w.likerCount);
				doc.addField("msg_attention", w.views);
				doc.addField("msg_type", "3");
				docs.add(doc);
			}
			SolrContent.getInstance().getServer().add(docs);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 查询
	 * @param condition 需要查询的内容
	 * @param model  要搜索的域
	 * @param currentPage 显示的页  
	 */
	public static void findByModel(String condition, int model, int currentPage) {
		
		boolean contentIsEmpty = false;
		String sign = null;
		long pageCount = 0;
		if (model == 0) {
			sign = "all";
		} else if (model == 1) {// "1" 代表活动
			sign = "Activity";
		} else if (model == 2) {// "2" 代表问答
			sign = "QA";
		} else if (model == 3) {// "3" 代表公益
			sign = "Wel";
		}
		List<Index> datas = new ArrayList<Index>();
		try {
			 if(condition.equals("")||currentPage<=0){
				  contentIsEmpty=true; 
			}else{
				// 定义分页,每次显示十条数据
				int pageSize = 10;
				// 通过currPage和pageSize计算出开始，在solr里面不需要加1
				int start = (currentPage - 1) * pageSize;
				//把condition按照空格进行拆分
				String [] string = condition.split(" ");
				String conString = "";
				for (int i = 0; i < string.length; i++) {
					conString += string[i];
				}
				SolrQuery query = new SolrQuery();
				if (model == 0) {
					query.set("q", "msg_all:" + conString);
				} else {
					query.set("q", "msg_type:" + model + " AND msg_all:"
							+ conString);
				}
				query.setHighlight(true)
						.setHighlightSimplePre("<span style='color:red'>")
						.setHighlightSimplePost("</span>").setStart(start)
						.setRows(pageSize);
				query.setParam("hl.fl", "msg_title,msg_content");
	
				QueryResponse resp = SolrContent.getInstance().getServer()
						.query(query);
				SolrDocumentList sdl = resp.getResults();
				if (sdl.getNumFound()>0) {
					for (SolrDocument sd : sdl) {
						String id = (String) sd.getFieldValue("id");
						Index index = new Index();
						index.setId(id);
						index.setTitle(sd.getFieldValue("msg_title").toString());
						index.setSummary(sd.getFieldValue("msg_content")
								.toString());
						index.setFrom(sd.getFieldValue("msg_name").toString());
						index.setCreateDate(sd.getFieldValue("msg_date")
								.toString());
						index.setAttention((int) sd
								.getFieldValue("msg_attention"));
						index.setJoin((int) sd.getFieldValue("msg_join"));
						index.setType((String) sd.getFieldValue("msg_type"));
						if (resp.getHighlighting().get(id) != null) {
							List<String> htitle = resp.getHighlighting()
									.get(id).get("msg_title");
							if (htitle != null)
								index.setTitle(htitle.get(0));
							List<String> hcontent = resp.getHighlighting()
									.get(id).get("msg_content");
							if (hcontent != null)
								index.setSummary(hcontent.get(0));
						}
						datas.add(index);
						contentIsEmpty = false;
						pageCount = sdl.getNumFound() % pageSize == 0 ? sdl
								.getNumFound() / pageSize : (sdl.getNumFound()
								/ pageSize + 1);
					}
				}else{
					contentIsEmpty=true; 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int[] inter = Paging.getRount(8, (int) pageCount, currentPage);
		renderTemplate("Index/indexSearch.html", sign, datas, condition, inter,model,
				currentPage, pageCount, contentIsEmpty);
	}

}
