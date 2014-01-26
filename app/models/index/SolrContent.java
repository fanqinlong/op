package models.index;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

public class SolrContent {
	private final static String URL="http://localhost:8888/solr";
	private static SolrContent content = null;
	private static HttpSolrServer server = null;
	public static SolrContent getInstance(){
		if(content==null){
			content = new SolrContent();
		}
		return content;
	}
	public SolrServer getServer(){
		if(server==null){
			server = new HttpSolrServer(URL);
		}
		return server;
	} 
	
}
