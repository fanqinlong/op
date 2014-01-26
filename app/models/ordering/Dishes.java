package models.ordering;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

/**
 * 菜谱表
 * @author summer
 *
 */

@Entity
public class Dishes extends Model{
	
	@ManyToOne
	public Menu menu;
	public long amount;
	@ManyToOne
	public Orders orders;
	
	public Dishes(Menu menu, long amount, Orders orders) {
		this.menu = menu;
		this.amount = amount;
		this.orders = orders;
		create();
	}
	
}
