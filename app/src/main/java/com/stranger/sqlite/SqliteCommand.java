package com.stranger.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * sqlite命令封装，方便直接调用增删查改功能模块，且针对不同的调用对函数进行重构
 * 
 * @author lake
 * 
 */
public class SqliteCommand {

	// SQLite的助手类对象
	private DatabaseHelper dbHelper;
	// 构造数据库对象
	SQLiteDatabase db = null;

	// 构造sqlite命令函数并初始化数据库对象
	public SqliteCommand(Context context, String name) {
		dbHelper = new DatabaseHelper(context, name);
	}

	/**
	 * 新建一个表
	 */
	public void CreateTable() {
		// 允许对数据库进行写入
		db = dbHelper.getWritableDatabase();
		String sql = "create table if not exists user"
				+ "(name varchar(20),latitude double,longitude double)";
		try {
			// Log.i("get", "create table");
			// 执行数据库语句
			db.execSQL(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			// Log.i("err", "create table failed");
		}
	}

	/**
	 * 插入数据
	 */
	public void InsertTb(String name, double latitude, double longitude) {
		db = dbHelper.getWritableDatabase();
		// Log.i("get", "insert table");
		String sql = "insert into user (name,latitude,longitude) "
				+ "values ('" + name + "'," + latitude + "," + longitude + ")";
		try {
			db.execSQL(sql);
			// Log.i("get", "insert table");
		} catch (SQLException e) {
			e.printStackTrace();
			// Log.i("err", "insert failed");
		}
	}

	/**
	 * 更新数据
	 */
	public void UpdateTb(String name, double latitude, double longitude) {
		db = dbHelper.getWritableDatabase();
		String sql = "Update user set latitude = " + latitude + ",longitude = "
				+ longitude + " where name = '" + name + "'";
		try {
			db.execSQL(sql);
			// Log.i("sys", "update table");
		} catch (SQLException e) {
			e.printStackTrace();
			// Log.i("err", "update failed");
		}
	}

	/**
	 * 删除数据
	 */
	public void DeleteTb(String name) {
		db = dbHelper.getWritableDatabase();
		String sql = "delete from user where name = '" + name + "'";
		try {
			db.execSQL(sql);
			// Log.i("get", "delete table");
		} catch (SQLException e) {
			e.printStackTrace();
			// Log.i("err", "delete failed");
		}
	}

	/**
	 * 查询数据boolean
	 */
	public boolean isQueryTb(Context context, String name) {
		db = dbHelper.getWritableDatabase();
		DatabaseHelper dbHelper = new DatabaseHelper(context,
				"Stranger_Talking");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from user", null);
		while (cursor.moveToNext()) {
			// Log.i("get", cursor.getString(0) + "--");
			if (cursor.getString(0) == "定位") {
				// Log.i("get", "isquery table");
				return true;
			}
		}
		return false;
	}

	/**
	 * 查询数据返回坐标经纬度
	 */
	public String QueryTb(Context context, String name) {
		db = dbHelper.getWritableDatabase();
		DatabaseHelper dbHelper = new DatabaseHelper(context,
				"Stranger_Talking");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from user where name = ?",
				new String[] { name });
		String lalo = null;
		// Log.i("sys", "query table" + cursor.moveToNext());
		while (cursor.moveToNext()) {
			double latitude = cursor.getDouble(cursor
					.getColumnIndex("latitude"));
			double longitude = cursor.getDouble(cursor
					.getColumnIndex("longitude"));
			lalo = longitude + "," + latitude;
			// Log.i("sys", "query table   " + lalo);
			return lalo;
		}
		return "114.422311,23.037629";
	}

	/**
	 * 查询数据返回用户界面
	 */
	public String QueryTb(Context context, double latitude, double longitude) {
		db = dbHelper.getWritableDatabase();
		DatabaseHelper dbHelper = new DatabaseHelper(context,
				"Stranger_Talking");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String la = latitude + "";
		String lo = longitude + "";
		Cursor cursor = db
				.rawQuery(
						"select * from user where latitude like ? and longitude like ?",
						new String[] { la, lo });
		// Log.i("sys", "query table" + cursor.moveToNext());
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("name"));
			// Log.i("sys", "query table   " + name);
			return name;
		}
		return " ";
	}

	/**
	 * 打开数据库
	 */
	public void OpenDb(Context context) {
		dbHelper = new DatabaseHelper(context, "Stranger_Talking");
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * 关闭数据库
	 */
	public void CloseDb() {
		dbHelper.close();
	}

}
