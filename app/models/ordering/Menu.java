package models.ordering;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;
/**
 * 菜单
 * @author summer
 */
@Entity
public class Menu extends Model{
	public String dishes;
	public float price;
	public String standard;
	public String introduct;
	
	@ManyToOne
	public Restaurant restaurant;
	
	
}
