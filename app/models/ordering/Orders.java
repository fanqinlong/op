package models.ordering;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import play.db.jpa.Model;
import play.libs.Codec;
/**
 * 订单表
 * @author summer
 *
 */

@Entity
public class Orders extends Model{
	public float totalPrice;
	public String orderNumber;
	
	public String userName;
	public String email;
	public long qq;
	public long phone;
	public String weixin;
	public long time;
	public long orderTime;
	public String location;
	public String status;
	public boolean isDelete;
	public boolean isSubmit;
	public String remark;
	
	@OneToMany(mappedBy="orders", cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	public List<Dishes> dishes;
	
	//餐厅ID ，如果餐厅多的话，以便用来区分是哪家餐厅的订单
	public long restaurantId;

	public Orders(float totalPrice, String orderNumber,
			String userName, String email, long qq, long phone, String weixin,
			long time,long orderTime, String location, String status, long restaurantId,boolean isSubmit,boolean isDelete,String remark) {
		this.totalPrice = totalPrice;
		this.orderNumber = orderNumber;
		this.userName = userName;
		this.email = email;
		this.qq = qq;
		this.phone = phone;
		this.weixin = weixin;
		this.time = time;
		this.orderTime = orderTime;
		this.location = location;
		this.status = status;
		this.restaurantId = restaurantId;
		this.isSubmit = isSubmit;
		this.isDelete = isDelete;
		this.remark = remark;
		create();
	}
	

}
