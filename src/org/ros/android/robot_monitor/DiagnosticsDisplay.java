package org.ros.android.robot_monitor;


import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

import org.ros.message.MessageListener;
import org.ros.message.diagnostic_msgs.DiagnosticArray;
import org.ros.message.diagnostic_msgs.DiagnosticStatus;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

public class DiagnosticsDisplay extends AsyncTask<String, DiagnosticArray, String> implements NodeMain {
	private Activity activity;
	private Subscriber<DiagnosticArray> subscriber;
	
	DiagnosticsDisplay(Activity activity){
		this.activity = activity;
	}
	
	

	@Override
	protected void onPreExecute(){
		
	}

	@Override
	protected String doInBackground(String... params) {
	  this.subscriber.addMessageListener(new MessageListener<DiagnosticArray>() {
        @Override
        public void onNewMessage(DiagnosticArray message) {
        	publishProgress(message);
        }
      });
	  return null;
	}
	
	@Override
	protected void onProgressUpdate(DiagnosticArray... vals){
		displayArray(vals[0]);
	}
	
	@Override
	protected void onPostExecute(String msg){
		
	}

	@Override
	public void onStart(Node node) {
		this.subscriber = node.newSubscriber("/diagnostics_agg", "diagnostic_msgs/DiagnosticArray");
		this.execute();
	}

	@Override
	public void onShutdown(Node node) {
		this.cancel(true);
	}

	@Override
	public void onShutdownComplete(Node node) {

	}
	
	private void displayArray(DiagnosticArray msg){
		TableLayout tl = (TableLayout)this.activity.findViewById(R.id.maintable);
		tl.removeAllViews();
		
		// TODO Investigate why these icons can be wrong.
		Resources res = this.activity.getResources();
		Drawable error = res.getDrawable(R.drawable.error);
		Drawable warn = res.getDrawable(R.drawable.warn);
		Drawable ok = res.getDrawable(R.drawable.ok);
		//Drawable stale = res.getDrawable(R.drawable.stale);
		
		// Actually display each Status
		byte level = DiagnosticStatus.OK;
		for(final DiagnosticStatus ds : msg.status){
			Button b = new Button(this.activity);
			b.setText(ds.name);
			level = updateLevel(level, ds.level);
			if(ds.level == 3){ // STALE is not part of the message definitions
				b.setTextColor(Color.BLUE);
			} else if(ds.level == DiagnosticStatus.ERROR){
				b.setTextColor(Color.RED);
				b.setCompoundDrawablesWithIntrinsicBounds(error, null, null, null);
			} else if(ds.level == DiagnosticStatus.WARN){
				b.setTextColor(Color.YELLOW);
				b.setCompoundDrawablesWithIntrinsicBounds(warn, null, null, null);
			} else { // Is OK!
				b.setTextColor(Color.GREEN);
				b.setCompoundDrawablesWithIntrinsicBounds(ok, null, null, null);
			}
			b.setOnClickListener(new View.OnClickListener() {
	             public void onClick(View v) {
	            	 Intent myIntent = new Intent(activity, DetailsActivity.class);
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
	            	 activity.startActivity(myIntent);
	             }
	         });
			tl.addView(b);
		}
	}
	
	private byte updateLevel(byte oldLevel, byte newLevel){
		// OK < WARN < ERROR
		if(newLevel >= oldLevel){
			return newLevel;
		} else {
			return oldLevel;
		}
	}

	@Override
	public GraphName getDefaultNodeName() {
		Random rand = new Random();
		int random_int = Math.abs(rand.nextInt());
		String nodeName = "android_robot_monitor_" + random_int;
		GraphName name = new GraphName(nodeName);
		return name;
	}
}
