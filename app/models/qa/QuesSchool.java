package models.qa;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class QuesSchool extends Model {

	public String name;

	public QuesSchool(String name) {
		this.name = name;
		save();
	}
}
