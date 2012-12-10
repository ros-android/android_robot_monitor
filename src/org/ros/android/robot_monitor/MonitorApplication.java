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

import android.os.Bundle;
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
	  this.sub = new DiagnosticArraySubscriber(this);
	  this.sub.setTextView((TextView)findViewById(R.id.text));
  }
  
  @Override
  protected void onResume()
  {
		super.onResume();
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
