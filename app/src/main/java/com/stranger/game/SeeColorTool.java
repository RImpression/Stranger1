package com.stranger.game;

/**
 * SeeColor类的工具类
 * @author lake
 *
 */
public class SeeColorTool {

	public static int getLevel(int i) {
		int level = 0;
		switch (i) {
		case 0:
		case 1:
			level = 2;
			break;
		case 2:
		case 3:
			level = 3;
			break;
		case 4:
		case 5:
			level = 4;
			break;
		case 6:
		case 7:
			level = 5;
			break;
		case 8:
		case 9:
			level = 6;
			break;
		case 10:
		case 11:
		case 12:
			level = 7;
			break;
		case 13:
		case 14:
		case 15:
			level = 8;
			break;
		case 16:
		case 17:
		case 18:
			level = 9;
			break;
		default:
			level = 10;
			break;
		}
		return level;
	}

}
