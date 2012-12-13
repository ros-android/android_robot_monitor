package org.ros.android.robot_monitor;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import diagnostic_msgs.DiagnosticArray;
import diagnostic_msgs.DiagnosticStatus;

public class DiagnosticsArrayDisplay {
	
	Context context;
	
	TableLayout tl;
	TextView tv;
	
	Drawable error;
	Drawable warn;
	Drawable ok;
	Drawable stale;
	int error_color;
	int warn_color;
	int ok_color;
	int stale_color;
	
	byte level;
	
	public DiagnosticsArrayDisplay(Context context){
		this.context = context;
	}
	
	void setTableLayout(TableLayout tl){
		this.tl = tl;
	}
	
	void setTextView(TextView tv){
		this.tv = tv;
	}
	
	void setDrawables(Drawable error, Drawable warn, Drawable ok, Drawable stale){
		this.error = error;
		this.warn = warn;
		this.ok = ok;
		this.stale = stale;
	}
	
	void setColors(int error_color, int warn_color, int ok_color, int stale_color){
		this.error_color = error_color;
		this.warn_color = warn_color;
		this.ok_color = ok_color;
		this.stale_color = stale_color;
	}
	
	void displayArray(DiagnosticArray msg){
		final List<DiagnosticStatus> dsa = msg.getStatus();
		this.level = DiagnosticStatus.OK;
		tl.post(new Runnable(){
			@Override
			public void run(){
				tl.removeAllViews();
				// Actually display each Status
				
				for(final DiagnosticStatus ds : dsa){
					Button b = new Button(context);
					b.setText(ds.getName());
					level = updateLevel(level, ds.getLevel());
					if(ds.getLevel() == 3){ // STALE is not part of the message definitions
						b.setTextColor(stale_color);
						b.setCompoundDrawablesWithIntrinsicBounds(stale, null, null, null);
					} else if(ds.getLevel() == DiagnosticStatus.ERROR){
						b.setTextColor(error_color);
						b.setCompoundDrawablesWithIntrinsicBounds(error, null, null, null);
					} else if(ds.getLevel() == DiagnosticStatus.WARN){
						b.setTextColor(warn_color);
						b.setCompoundDrawablesWithIntrinsicBounds(warn, null, null, null);
					} else { // Is OK!
						b.setTextColor(ok_color);
						b.setCompoundDrawablesWithIntrinsicBounds(ok, null, null, null);
					}
					/*b.setOnClickListener(new View.OnClickListener() {
			             public void onClick(View v) {
			            	 /*if(!buttonPressed){
			            		 buttonPressed = true;
				            	 // Do not kill node on activity change
				            	 saveNode = true;
				            	// Unregister listener
				         	   	MonitorApplication ma = (MonitorApplication)getApplicationContext();
				         	   	ma.getSubscriber().removeMessageListener(ml);
				            	 Intent myIntent = new Intent(dad, DiagnosticsStatusDisplay.class);
				            	 myIntent.putExtra("level", ds.level);
				            	 myIntent.putExtra("name", ds.name);
				            	 myIntent.putExtra("message", ds.message);
				            	 myIntent.putExtra("hardware_id", ds.hardware_id);
				            	 String[] keys = new String[ds.values.size()];
				            	 String[] values = new String[ds.values.size()];
				            	 for(int i = 0; i < ds.values.size(); i++){
				            		 keys[i] = ds.values.get(i).key;
				            		 values[i] = ds.values.get(i).value;
				            	 }
				            	 myIntent.putExtra("keys", keys);
				            	 myIntent.putExtra("values", values);
				            	 dad.startActivity(myIntent);
			            	 }
			             }
			         });*/
					tl.addView(b);
				}
			}
		});
		tl.postInvalidate();
		
		tv.post(new Runnable(){
			@Override
			public void run(){
				// TODO This is where I would store the message into buttons that scroll to look back in time?
				if(level == 3){ // STALE is not part of the message definitions
					tv.setBackgroundColor(stale_color);
				} else if(level == DiagnosticStatus.ERROR){
					tv.setBackgroundColor(error_color);
				} else if(level == DiagnosticStatus.WARN){
					tv.setBackgroundColor(warn_color);
				} else { // Is OK!
					tv.setBackgroundColor(ok_color);
				}
			}
		});
		tv.postInvalidate();

	}
	
	byte updateLevel(byte oldLevel, byte newLevel){
		// OK < WARN < ERROR
		if(newLevel >= oldLevel){
			//this.level = newLevel;
			return newLevel;
		} else {
			//this.level = oldLevel;
			return oldLevel;
		}
	}
}
