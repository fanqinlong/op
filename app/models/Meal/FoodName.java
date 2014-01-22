package models.Meal;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class FoodName extends Model {
	public String dishes;
	public int price;
	public int number;
	public String shopName;

	public FoodName(String dishes, int price, String shopName, int number) {
		this.dishes = dishes;
		this.price = price;
		this.shopName = shopName;
		this.number = number;
		create();
	}
}
