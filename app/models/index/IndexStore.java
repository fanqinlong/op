package models.index;

import java.util.HashMap;
import java.util.Map;

import models.activity.Activity;
import models.charity.Wel;
import models.qa.Ques;

import org.apache.solr.common.SolrInputDocument;

public class IndexStore {
	
	private static IndexStore storeIndex=null;
	public static IndexStore getInstance(){
		if(storeIndex==null){
			storeIndex = new IndexStore();
		}
		return storeIndex;
	}
	/**
	 * 添加索引
	 */
	public void addIndexActivity(Activity a) {
		try {
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
			SolrContent.getInstance().getServer().add(doc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addIndexQues(Ques q) {
		try {
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
			SolrContent.getInstance().getServer().add(doc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addIndexWel(Wel w) {
		try {
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
			
			SolrContent.getInstance().getServer().add(doc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 更新索引
	 */
	public void updateIndexActivity(Activity a) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id","3"+ a.id);
			if(!"".equals(a.name)) map.put("msg_title", a.name);
			if(!"".equals(a.intro)) map.put("msg_content", a.intro);
			if(!"".equals(a.publisherSU == null ? (a.publisherCSSA.school.name + " CSSA")
					: a.publisherSU.name)) map.put("msg_name", a.publisherSU == null ? (a.publisherCSSA.school.name + " CSSA"): a.publisherSU.name);
			if(!"".equals(a.postAt)) map.put("msg_date", a.postAt);
			if(!"".equals(a.joiner.size())) map.put("msg_join", a.joiner.size());
			if(!"".equals(a.liker.size())) map.put("msg_attention", a.liker.size());
			SolrInputDocument sid = updateMapToDocument(map, "id");
			SolrContent.getInstance().getServer().add(sid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateIndexQues(Ques q) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id","2"+ q.id);
			if(!"".equals(q.title)) map.put("msg_title", q.title);
			if(!"".equals(q.content)) map.put("msg_content", q.content);
			if(!"".equals(q.username)) map.put("msg_name", q.username);
			if(!"".equals(q.date)) map.put("msg_date", q.date);
			if(!"".equals(q.answerNum)) map.put("msg_join", q.answerNum);
			if(!"".equals(q.focusNum)) map.put("msg_attention", q.focusNum);
			SolrInputDocument sid = updateMapToDocument(map, "id");
			SolrContent.getInstance().getServer().add(sid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateIndexWel(Wel w) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id","3"+ w.id);
			if(!"".equals(w.title)) map.put("msg_title", w.title);
			if(!"".equals(w.content)) map.put("msg_content", w.content);
			if(!"".equals(w.fromUser)) map.put("msg_name", w.fromUser);
			if(!"".equals(w.time)) map.put("msg_date", w.time);
			if(!"".equals(w.likerCount)) map.put("msg_join", w.likerCount);
			if(!"".equals(w.views)) map.put("msg_attention", w.views);
			SolrInputDocument sid = updateMapToDocument(map, "id");
			SolrContent.getInstance().getServer().add(sid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 删除索引
	 */
	/*
	 * public static void deleteIndex(String id, String type) { try {
	 * SolrContent.getInstance().getServer().deleteById(id);
	 * SolrContent.getInstance().getServer().commit(); } catch (Exception e) {
	 * // TODO Auto-generated catch block e.printStackTrace(); } }
	 */

	
	/**
	   * 更新map转换为更新doc
	   * @param map
	   * @param key
	   * @return
	   */
	  public SolrInputDocument updateMapToDocument(Map<String, Object> map, String key){
	    if(map == null || key == null)return null;
	    boolean hasKey = false;
	    SolrInputDocument doc = new SolrInputDocument();
	    for(String name : map.keySet()){
	      Object value = map.get(name);
	      if(name.equals(key)){
	        if(value != null){
	          doc.addField(name, value);
	          hasKey = true;
	        }
	      }
	      else{
	        Map<String, Object> optMap = new HashMap<String, Object>();
	        optMap.put("set", value);
	        doc.addField(name, optMap);
	      }
	    };
	    if(!hasKey)return null;
	    return doc;
	  }
}
