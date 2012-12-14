/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.robot_monitor;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TextView;

import org.ros.address.InetAddressFactory;
import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import diagnostic_msgs.DiagnosticArray;

///< @TODO REMOVE
import java.net.URI;

/**
 * @author chadrockey@gmail.com (Chad Rockey)
 */


public class MonitorApplication extends RosActivity
{
	
  private DiagnosticArraySubscriber sub;
  private DiagnosticsArrayDisplay dad;

  public MonitorApplication()
  {
	  super("ROS Robot Monitor", "ROS Robot Monitor");
  }
  
  @Override
  protected void onPause()
  {
	  super.onPause();
  }
  
  protected void onCreate(Bundle savedInstanceState)
  {
	  super.onCreate(savedInstanceState);
	  
	  setContentView(R.layout.main);
	  this.dad = new DiagnosticsArrayDisplay(this);
	  TableLayout tl = (TableLayout)findViewById(R.id.maintable);
	  this.dad.setTableLayout(tl);
	  TextView tv = (TextView)findViewById(R.id.global);
	  this.dad.setTextView(tv);
	  
	  // TODO Investigate why these icons can be wrong.
	  Resources res = getResources();
	  Drawable error = res.getDrawable(R.drawable.error);
	  Drawable warn = res.getDrawable(R.drawable.warn);
	  Drawable ok = res.getDrawable(R.drawable.ok);
	  Drawable stale = res.getDrawable(R.drawable.stale);
	  this.dad.setDrawables(error, warn, ok, stale);
	  this.dad.setColors(getResources().getColor(R.color.error), getResources().getColor(R.color.warn), getResources().getColor(R.color.ok), getResources().getColor(R.color.stale));
	  
	  this.sub = (DiagnosticArraySubscriber)getLastNonConfigurationInstance();
	  if(this.sub != null){
		  if(this.sub.getLastMessage() != null){
			  this.dad.displayArray(this.sub.getLastMessage());
		  }
	  } else {
		  this.sub = new DiagnosticArraySubscriber();
	  }
	  this.sub.setMessageCallable(new MessageCallable<DiagnosticArray, DiagnosticArray>(){
		  @Override
		  public DiagnosticArray call(DiagnosticArray message){
			  MonitorApplication.this.dad.displayArray(message);
			  return message;
		  }
	  });
  }
  
  @Override
  protected void onResume()
  {
		super.onResume();
  }
  
  @Override
  public Object onRetainNonConfigurationInstance(){
	  this.sub.clearCallable();
	  return this.sub;
  }

  @Override
  protected void init(NodeMainExecutor nodeMainExecutor)
  {
    boolean staticMaster = true;
    NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
    if(staticMaster){
      nodeConfiguration.setMasterUri(URI.create("http://192.168.15.247:11311/"));
    } else {
      nodeConfiguration.setMasterUri(getMasterUri());
    }
    nodeConfiguration.setNodeName("android_robot_monitor");
    nodeMainExecutor.execute(this.sub, nodeConfiguration);
  }
}
