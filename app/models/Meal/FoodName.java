package models.Meal;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class FoodName extends Model {
	public String dishes;
	public String price;
	public int number;
	public String shop_name;

	public FoodName(String dishes, String price, String shop_name,int number) {
		this.dishes = dishes;
		this.price = price;
		this.shop_name = shop_name;
		this.number = number;
		create();
	}
}
