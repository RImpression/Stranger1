package com.stranger.game;

import java.util.Random;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * 游戏界面和属性的设置类
 * 
 * @author lake
 * 
 */
public class SeeColor extends View {

	// 游戏界面
	double gameH = 0;
	double gameW = 0;

	// 游戏难度级别
	int level = 2;

	// 三颜色
	int colorB = 0;
	int colorG = 0;
	int colorR = 0;

	// 目标颜色图块
	int target_lX;
	int target_uY;
	int target_rX;
	int target_dY;

	// 画笔
	Paint p;

	// 随机数声明
	Random r;

	public SeeColor(Context context, int screenH, int screenW, int level) {
		super(context);
		p = new Paint();
		r = new Random();
		this.gameH = screenH;
		this.gameW = screenW;
		this.level = level;
	}

	/**
	 * 在画布上画出游戏的界面
	 */
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas c) {
		super.onDraw(c);

		// 设置画笔颜色
		colorR = this.getRandomColorValue();
		colorG = this.getRandomColorValue();
		colorB = this.getRandomColorValue();
		int colorAll = Color.rgb(colorR, colorG, colorB); // 所有图块
		int colorTarget = Color.rgb(colorR - 13, colorG - 13, colorB - 13); // 目标图块
		p.setColor(colorAll);

		// 游戏图块的绘画
		for (int i = 0; i < level; i++) {
			for (int j = 0; j < level; j++) {
				int leftX = (int) ((i * gameW / level) + 5);
				int upY = (int) ((j * gameH / level) + 5);
				int rightX = (int) (((i + 1) * gameW / level) - 5);
				int downY = (int) (((j + 1) * gameH / level) - 5);
				Rect rect = new Rect(leftX, upY, rightX, downY);
				c.drawRect(rect, p);
			}
		}

		// 游戏目标图块的绘画
		int targetl = r.nextInt(level);
		int targetr = r.nextInt(level);
		for (int i = targetl; i <= targetl; i++) {
			for (int j = targetr; j <= targetr; j++) {
				target_lX = (int) ((i * gameW / level) + 5);
				target_uY = (int) ((j * gameH / level) + 5);
				target_rX = (int) (((i + 1) * gameW / level) - 5);
				target_dY = (int) (((j + 1) * gameH / level) - 5);
				p.setColor(colorTarget);
				Rect rect = new Rect(target_lX, target_uY, target_rX, target_dY);
				c.drawRect(rect, p);
			}
		}
	}

	// 生成0-255随机数
	private int getRandomColorValue() {
		int color;
		color = r.nextInt(255);
		return color;
	}

}
