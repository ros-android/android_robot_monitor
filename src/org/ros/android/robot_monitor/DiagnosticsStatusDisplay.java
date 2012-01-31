/*
 * Copyright (c) 2012, Chad Rockey
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Android Robot Monitor nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.ros.android.robot_monitor;

import java.util.Random;

import org.ros.message.MessageListener;
import org.ros.message.diagnostic_msgs.DiagnosticArray;
import org.ros.message.diagnostic_msgs.DiagnosticStatus;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * @author chadrockey@gmail.com (Chad Rockey)
 */
public class DiagnosticsStatusDisplay extends Activity {
	
	private Activity activity;
	private DisplayClass dc;
	private String name;

  public DiagnosticsStatusDisplay() {
	  activity = this;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.details);
    Intent intent = getIntent();
    TextView tname = (TextView)findViewById(R.id.name);
    name = intent.getStringExtra("name");
    tname.setText(name);
    TextView thard = (TextView)findViewById(R.id.hardware_id);
    thard.setText(intent.getStringExtra("hardware_id"));
    TextView tmess = (TextView)findViewById(R.id.message);
    tmess.setText(intent.getStringExtra("message"));
    TableLayout tl = (TableLayout)findViewById(R.id.keys);
    String[] keys = intent.getStringArrayExtra("keys");
    String[] values = intent.getStringArrayExtra("values");
    for(int i = 0; i < keys.length; i++){
    	TextView tv = new TextView(this);
    	String keystring = keys[i] + ": " + values[i];
    	tv.setText(keystring);
    	tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    	tl.addView(tv);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    MonitorApplication ma = (MonitorApplication)getApplicationContext();
    NodeConfiguration nodeConfiguration = ma.getNodeConfiguration();

    dc = new DisplayClass();
    nodeConfiguration.setNodeName(dc.getDefaultNodeName());
    ma.getNodeMainExecutor().execute(dc, nodeConfiguration);
  }

  @Override
  protected void onPause() {
    super.onPause();
    MonitorApplication ma = (MonitorApplication)getApplicationContext();
	ma.getNodeMainExecutor().shutdownNodeMain(dc);
  }
  
	private class DisplayClass extends AsyncTask<String, DiagnosticArray, String> implements NodeMain {
		
		private Subscriber<DiagnosticArray> subscriber;
		
		@Override
		protected void onPreExecute(){
			
		}
	
		@Override
		protected String doInBackground(String... params) {
		  subscriber.addMessageListener(new MessageListener<DiagnosticArray>() {
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
			subscriber = node.newSubscriber("/diagnostics_agg", "diagnostic_msgs/DiagnosticArray");
			this.execute();
		}
	
		@Override
		public void onShutdown(Node node) {
			this.cancel(true);
		}
	
		@Override
		public void onShutdownComplete(Node node) {
	
		}
		
		public void displayArray(DiagnosticArray msg){
			// TODO Investigate why these icons can be wrong, add them in this panel?
			/*Resources res = getResources();
			Drawable error = res.getDrawable(R.drawable.error);
			Drawable warn = res.getDrawable(R.drawable.warn);
			Drawable ok = res.getDrawable(R.drawable.ok);
			Drawable stale = res.getDrawable(R.drawable.stale);*/
			
			// Find our current status
			for(final DiagnosticStatus ds : msg.status){
				if(ds.name.equals(name)){
					// TODO Figure out what marks a diagnostic group
					if(ds.values.size() == -1 && ds.hardware_id.length() == 0){// Aggregated Diagnostic
					} else { // Final level diagnostic
					    TextView tname = (TextView)findViewById(R.id.name);
					    tname.setText(ds.name);
					    TextView thard = (TextView)findViewById(R.id.hardware_id);
					    thard.setText(ds.hardware_id);
					    TextView tmess = (TextView)findViewById(R.id.message);
					    tmess.setText(ds.message);
					    TableLayout tl = (TableLayout)findViewById(R.id.keys);
					    tl.removeAllViews();
					    for(int i = 0; i < ds.values.size(); i++){
					    	TextView tv = new TextView(activity);
					    	String keystring = ds.values.get(i).key + ": " + ds.values.get(i).value;
					    	tv.setText(keystring);
					    	tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
					    	tl.addView(tv);
					    }
					}
				}
			}
		}
	
		@Override
		public GraphName getDefaultNodeName() {
			// Set an "anonymous" node name with a random suffix.
			Random rand = new Random();
			int random_int = Math.abs(rand.nextInt());
			String nodeName = "android_robot_monitor_" + random_int;
			GraphName name = new GraphName(nodeName);
			return name;
		}
		
	}

}
