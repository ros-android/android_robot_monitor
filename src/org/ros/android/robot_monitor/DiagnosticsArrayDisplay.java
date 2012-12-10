package org.ros.android.robot_monitor;

/*import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import org.ros.address.InetAddressFactory;
import org.ros.android.MasterChooser;
import org.ros.message.MessageListener;
import org.ros.message.diagnostic_msgs.DiagnosticArray;
import org.ros.message.diagnostic_msgs.DiagnosticStatus;
import org.ros.node.NodeConfiguration;

public class DiagnosticsArrayDisplay extends Activity {
	
	private final DiagnosticsArrayDisplay dad;
	//private DisplayClass dc;
	
	private boolean saveNode;
	private boolean hasInitialized;
	
	public DiagnosticsArrayDisplay(){
		dad = this;
		saveNode = false;
		hasInitialized = false;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
    }

    @Override
    protected void onResume() {
    	super.onResume();
	  	MonitorApplication ma = (MonitorApplication)getApplicationContext();
		if(ma.getMasterURI() == null){
			hasInitialized = false;
			startActivityForResult(new Intent(this, MasterChooser.class), 0);
		} else {
			connect();
		}
    }
    
    private void connect(){
    	MonitorApplication ma = (MonitorApplication)getApplicationContext();
    	saveNode = false;
    	if(ma.getMasterURI() != null){
  		  NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
  		  nodeConfiguration.setMasterUri(ma.getMasterURI());
  		  ma.setNodeConfiguation(nodeConfiguration);
  	      	if(!hasInitialized){
  	      		hasInitialized = true;
  				ma.getNodeConfiguration().setNodeName(ma.getDefaultNodeName());
  				ma.getNodeMainExecutor().execute(ma, ma.getNodeConfiguration());
  	      	}
  	      	DisplayClass dc = new DisplayClass();
  			dc.execute();
      	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == 0 && resultCode == RESULT_OK) {
        try {
          MonitorApplication ma = (MonitorApplication)getApplicationContext();
          ma.setMasterURI(new URI(data.getStringExtra("ROS_MASTER_URI")));
          connect();
        } catch (URISyntaxException e) {
          throw new RuntimeException(e);
        }
      }
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
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      setContentView(R.layout.main);
    }

	private class DisplayClass extends AsyncTask<String, DiagnosticArray, String> {
		
		private MessageListener<DiagnosticArray> ml;
		private boolean buttonPressed;
		
		public DisplayClass(){
			buttonPressed = false;
		}
		
		@Override
		protected void onPreExecute(){
		}

		@Override
		protected String doInBackground(String... params) {
				MonitorApplication ma = (MonitorApplication)getApplicationContext();
				while(ma.getSubscriber() == null && !isCancelled()){
					try {
						Thread.sleep(10);
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
		
		@Override
		protected void onCancelled(){
		}
		
		void displayArray(DiagnosticArray msg){
			TableLayout tl = (TableLayout)findViewById(R.id.maintable);
			tl.removeAllViews();
			
			// TODO Investigate why these icons can be wrong.
			Resources res = getResources();
			Drawable error = res.getDrawable(R.drawable.error);
			Drawable warn = res.getDrawable(R.drawable.warn);
			Drawable ok = res.getDrawable(R.drawable.ok);
			Drawable stale = res.getDrawable(R.drawable.stale);
			
			// Actually display each Status
			byte level = DiagnosticStatus.OK;
			for(final DiagnosticStatus ds : msg.status){
				Button b = new Button(dad);
				b.setText(ds.name);
				level = updateLevel(level, ds.level);
				if(ds.level == 3){ // STALE is not part of the message definitions
					b.setTextColor(res.getColor(R.color.stale));
					b.setCompoundDrawablesWithIntrinsicBounds(stale, null, null, null);
				} else if(ds.level == DiagnosticStatus.ERROR){
					b.setTextColor(res.getColor(R.color.error));
					b.setCompoundDrawablesWithIntrinsicBounds(error, null, null, null);
				} else if(ds.level == DiagnosticStatus.WARN){
					b.setTextColor(res.getColor(R.color.warn));
					b.setCompoundDrawablesWithIntrinsicBounds(warn, null, null, null);
				} else { // Is OK!
					b.setTextColor(res.getColor(R.color.ok));
					b.setCompoundDrawablesWithIntrinsicBounds(ok, null, null, null);
				}
				b.setOnClickListener(new View.OnClickListener() {
		             public void onClick(View v) {
		            	 if(!buttonPressed){
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
		         });
				tl.addView(b);
			}
			// TODO This is where I would store the message into buttons that scroll to look back in time.
			TextView tv = (TextView)findViewById(R.id.global);
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
		
		private byte updateLevel(byte oldLevel, byte newLevel){
			// OK < WARN < ERROR
			if(newLevel >= oldLevel){
				return newLevel;
			} else {
				return oldLevel;
			}
		}
		
	}
}*/
