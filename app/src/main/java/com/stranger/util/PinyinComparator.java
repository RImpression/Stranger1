package com.stranger.util;

import java.util.Comparator;

import com.stranger.bean.DSUser;
/**
 * 根据拼音来排列LisiView里面的数据类
 *
 */

public class PinyinComparator implements Comparator<DSUser> {

	public int compare(DSUser o1, DSUser o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}
