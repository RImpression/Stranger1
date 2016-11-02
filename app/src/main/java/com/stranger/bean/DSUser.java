package com.stranger.bean;

import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.datatype.BmobFile;

/**
 * 用户类 性别，头像
 * 
 * @author RImperssion
 * 
 */
public class DSUser extends BmobChatUser {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean sex; // 性别,true为男
	private BmobFile image; // 头像
	private String birthday;// 生日
	private String interest;// 兴趣
	private String style; // 签名
	private Double Longitude;// 经度
	private Double Latitude; // 纬度
	private String sortLetters;// 显示拼音的首字母
	private String gamelevel;// 游戏等级
	private int gametime; // 游戏时间
	private boolean addreceipt;

	public boolean isAddreceipt() {
		return addreceipt;
	}

	public void setAddreceipt(boolean addreceipt) {
		this.addreceipt = addreceipt;
	}

	public int getGametime() {
		return gametime;
	}

	public void setGametime(int gametime) {
		this.gametime = gametime;
	}

	public String getGamelevel() {
		return gamelevel;
	}

	public void setGamelevel(String gamelevel) {
		this.gamelevel = gamelevel;
	}

	public String getSortLetters() {
		return sortLetters;
	}

	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}

	public boolean getSex() {
		return sex;
	}

	public void setSex(boolean sex) {
		this.sex = sex;
	}

	public String getInterest() {
		return interest;
	}

	public void setInterest(String interest) {
		this.interest = interest;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public Double getLongitude() {
		return Longitude;
	}

	public void setLongitude(Double longitude) {
		Longitude = longitude;
	}

	public Double getLatitude() {
		return Latitude;
	}

	public void setLatitude(Double latitude) {
		Latitude = latitude;
	}

	public BmobFile getImage() {
		return image;
	}

	public void setImage(BmobFile image) {
		this.image = image;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

}
