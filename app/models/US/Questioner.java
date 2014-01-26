package models.US;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class Questioner extends Model {
	public String name;
	public String type;
	public String content;
	public String contact;

	public Questioner(String name, String type, String content, String contact) {
		this.name = name;
		this.type = type;
		this.contact = contact;
		this.content = content;
		create();
	}
}
