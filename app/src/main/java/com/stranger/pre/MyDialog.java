package com.stranger.pre;

import com.example.examplep.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * 注册界面点击头像调用相机，图库的dialog
 * 
 * @author RImperssion
 * 
 */
public class MyDialog extends Dialog {
	private Button dialog_camera, dialog_tuku, dialog_sure;
	private LayoutInflater inflater;
	private View parent;
	private String message;
	private String namePositiveButton, nameNegativeButton;
	boolean hasNegative;
	boolean hasTitle;
	protected OnClickListener onSuccessListener;

	Context context;

	public MyDialog(Context context) {
		super(context);
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		parent = inflater.inflate(R.layout.mydialog, null);
		setContentView(parent);
	}

	public MyDialog(Context context, String title, String message,
			String buttonText, boolean hasNegative, boolean hasTitle) {
		super(context);
		super.setTitle(title);
		this.setMessage(message);
		this.setNamePositiveButton(buttonText);
		this.hasNegative = hasNegative;
		this.hasTitle = hasTitle;
	}

	public MyDialog(Context context, String message, String buttonText,
			String negetiveText, String title, boolean isCancel) {
		super(context);
		super.setTitle(title);
		this.setMessage(message);
		this.setNamePositiveButton(buttonText);
		this.hasNegative = false;
		this.setNameNegativeButton(negetiveText);
		this.hasTitle = true;
		this.setCancel(isCancel);
	}

	private boolean isCancel = true;// 默认是否可点击back按键/点击外部区域取消对话框

	public boolean isCancel() {
		return isCancel;
	}

	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}

	public Button getCameraBt() {
		if (dialog_camera == null) {
			dialog_camera = (Button) parent.findViewById(R.id.dialog_camera);
		}
		return dialog_camera;
	}

	public Button getTukeBt() {
		if (dialog_tuku == null) {
			dialog_tuku = (Button) parent.findViewById(R.id.dialog_tuku);
		}
		return dialog_tuku;
	}

	public Button getSureBt() {
		if (dialog_sure == null) {
			dialog_sure = (Button) parent.findViewById(R.id.dialog_sure);
		}
		return dialog_sure;
	}

	/**
	 * @return 对话框提示信息
	 */
	protected String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            对话框提示信息
	 */
	protected void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return 确认按钮名称
	 */
	protected String getNamePositiveButton() {
		return namePositiveButton;
	}

	/**
	 * @param namePositiveButton
	 *            确认按钮名称
	 */
	protected void setNamePositiveButton(String namePositiveButton) {
		this.namePositiveButton = namePositiveButton;
	}

	/**
	 * @return 取消按钮名称
	 */
	protected String getNameNegativeButton() {
		return nameNegativeButton;
	}

	/**
	 * @param nameNegativeButton
	 *            取消按钮名称
	 */
	protected void setNameNegativeButton(String nameNegativeButton) {
		this.nameNegativeButton = nameNegativeButton;
	}

	/**
	 * 设置成功事件监听，用于提供给调用者的回调函数
	 * @param listener 成功事件监听
	 */
	public void SetOnSuccessListener(OnClickListener listener){
		onSuccessListener = listener;
	}
	
}
