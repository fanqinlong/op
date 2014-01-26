package models.qa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Paging {
	public Paging() {

	}

	public static int [] getRount(int Max, int pageCount, int showPage) {
		int[] inter = new int[2];// 定义一个具有两个元素的数组
		int rount = pageCount - Max + 1;
		if (rount > 0) {
			if (rount > Max) {
				if (showPage >= Max && showPage <= rount) {
					inter[0] = showPage;
					inter[1] = Max + showPage - 1;
				} else if (showPage > rount && showPage <= pageCount) {
					inter[0] = rount;
					inter[1] = pageCount;
				} else {
					inter[0] = 1;
					inter[1] = Max;
				}
			} else {
				if (showPage >= Max) {
					inter[0] = rount;
					inter[1] = pageCount;
				} else {
					inter[0] = 1;
					inter[1] = Max;
				}
			}
		} else {
			inter[0] = 1;
			inter[1] = pageCount;
		}
		
//		List<Integer> list = new ArrayList<Integer>(inter.length);
//		for (Integer i : inter) {
//			    list.add(i);
//		}
		
		return inter;
	}
}
