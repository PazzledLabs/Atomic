/*******************************************************************************
 * Copyright (c) 2013 venkat@pazzled.com.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     venkat@pazzled.com - Venkat
 ******************************************************************************/
package pazzled.game.utils;

import pazzled.game.atomic.R;
import android.app.Dialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class Popup implements OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = Popup.class.getSimpleName();
	private Dialog dialog;
	private PopupListener listener;
	private int id;
	private LinearLayout layout;

	public Popup(Context c, PopupListener listener, int id, String text) {
		this.listener = listener;
		this.id = id;
		showDialog(c, text, false, 1);
	}

	public Popup(Context c, PopupListener listener, int id, String text, boolean remove_header) {
		this.listener = listener;
		this.id = id;
		showDialog(c, text, remove_header, 1);
	}
	
	public Popup(Context c, PopupListener listener, int id, String text, int show) {
		this.listener = listener;
		this.id = id;
		showDialog(c, text, false, show);
	}
	
	public Popup(Context c, PopupListener listener, int id, String text, boolean remove_header, int show) {
		this.listener = listener;
		this.id = id;
		showDialog(c, text, remove_header, show);
	}
	
	private void showDialog(Context mContext, String text, boolean remove_header, int show) {
		dialog = new Dialog(mContext,
				android.R.style.Theme_Translucent_NoTitleBar_Fullscreen) {
			public boolean onTouchEvent(MotionEvent event) {
				if (this.isShowing()) {
					//Log.d(TAG, "Touch outside the dialog");
					//this.dismiss();
//					listener.OnDismiss(id);
				}
				return false;
			}
		};
		dialog.setContentView(R.layout.popup);
		LinearLayout popup = (LinearLayout) dialog.findViewById(R.id.dialog_popup);

		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    llp.setMargins(50, 50, 10, 10); 
		TextView msg = new TextView(mContext);
		
		msg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
		msg.setLayoutParams(llp);
		msg.setText(text);
		
		if (remove_header) {
			LinearLayout header = (LinearLayout) dialog.findViewById(R.id.popup_header);
			popup.removeView(header);
		}
		popup.addView(msg);
		popup.setOnClickListener(this);
		
		ImageView next = (ImageView) dialog.findViewById(R.id.next_popup);
		next.setOnClickListener(this);

		layout = popup;
		
		if(show == 1) {
			show();
		}
	}

	public void addView(View v) {
		layout.addView(v);
	}
	
	public void removeView(View v) {
		layout.removeView(v);
	}
	
	public void show() {
		dialog.show();
	}
	
	public void dismiss() {
		dialog.dismiss();
	}
	
	public View findViewById(int id) {
		return dialog.findViewById(id);
	}

	@Override
	public void onClick(View v) {
		dialog.dismiss();
		if (listener != null)
			listener.OnDismiss(id);
	}
}
