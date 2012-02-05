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

import org.ros.message.MessageListener;
import org.ros.message.diagnostic_msgs.DiagnosticArray;
import org.ros.message.diagnostic_msgs.DiagnosticStatus;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * @author chadrockey@gmail.com (Chad Rockey)
 */

public class DiagnosticsStatusDisplay extends Activity {
	private final DiagnosticsStatusDisplay dsd;
	private MessageListener<DiagnosticArray> ml;
	private String name;
	private boolean saveNode;

  public DiagnosticsStatusDisplay() {
	  dsd = this;
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
	TextView tv = (TextView)findViewById(R.id.global);
	byte level = intent.getByteExtra("level", (byte) 3);
	Resources res = getResources();
	if(level == 3){ // STALE is not part of the message definitions
		tv.setBackgroundColor(res.getColor(R.color.stale));
	} else if(level == DiagnosticStatus.ERROR){
		tv.setBackgroundColor(res.getColor(R.color.error));
	} else if(level == DiagnosticStatus.WARN){
		tv.setBackgroundColor(res.getColor(R.color.warn));
	} else { // Is OK!
		tv.setBackgroundColor(res.getColor(R.color.ok));
	}
  }

  @Override
  protected void onResume() {
    super.onResume();
    saveNode = false;
	DisplayClass dc = new DisplayClass();
    dc.execute();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if(!saveNode){
	    MonitorApplication ma = (MonitorApplication)getApplicationContext();
	    ma.clear();
		ma.getNodeMainExecutor().shutdownNodeMain(ma);
    }
  }
  
  @Override
  public void onBackPressed() {
	  saveNode = true;
	    MonitorApplication ma = (MonitorApplication)getApplicationContext();
	    ma.getSubscriber().removeMessageListener(ml);
	    super.onBackPressed();
  }
  
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setContentView(R.layout.details);
  }
  
	private class DisplayClass extends AsyncTask<String, DiagnosticArray, String> {
		
		@Override
		protected void onPreExecute(){
			
		}
	
		@Override
		protected String doInBackground(String... params) {
			MonitorApplication ma = (MonitorApplication)getApplicationContext();
			while(ma.getSubscriber() == null && !isCancelled()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			ml = new MessageListener<DiagnosticArray>() {
		        @Override
		        public void onNewMessage(DiagnosticArray message) {
		        	publishProgress(message);
		        }
			};
			ma.getSubscriber().addMessageListener(ml);
		  return null;
		}
		
		@Override
		protected void onProgressUpdate(DiagnosticArray... vals){
			displayArray(vals[0]);
		}
		
		@Override
		protected void onPostExecute(String msg){
			
		}
		
		public void displayArray(DiagnosticArray msg){
			// Find our current status
			for(final DiagnosticStatus ds : msg.status){
				if(ds.name.equals(name)){
					// TODO Figure out what marks a diagnostic group
					if(ds.values.size() == -1 && ds.hardware_id.length() == 0){// Aggregated Diagnostic
					} else { // Final level diagnostic
					    TextView tname = (TextView)findViewById(R.id.name);
					    if(tname != null){
						    tname.setText(ds.name);
						    TextView thard = (TextView)findViewById(R.id.hardware_id);
						    thard.setText(ds.hardware_id);
						    TextView tmess = (TextView)findViewById(R.id.message);
						    tmess.setText(ds.message);
						    TableLayout tl = (TableLayout)findViewById(R.id.keys);
						    tl.removeAllViews();
						    for(int i = 0; i < ds.values.size(); i++){
						    	TextView tv = new TextView(dsd);
						    	String keystring = ds.values.get(i).key + ": " + ds.values.get(i).value;
						    	tv.setText(keystring);
						    	tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
						    	tl.addView(tv);
						    }
					    }
					}
					TextView tv = (TextView)findViewById(R.id.global);
					byte level = ds.level;
					Resources res = getResources();
					if(level == 3){ // STALE is not part of the message definitions
						tv.setBackgroundColor(res.getColor(R.color.stale));
					} else if(level == DiagnosticStatus.ERROR){
						tv.setBackgroundColor(res.getColor(R.color.error));
					} else if(level == DiagnosticStatus.WARN){
						tv.setBackgroundColor(res.getColor(R.color.warn));
					} else { // Is OK!
						tv.setBackgroundColor(res.getColor(R.color.ok));
					}
				}
			}
		}
	}

}
