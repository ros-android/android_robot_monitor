package org.ros.android.robot_monitor;

import java.net.URI;

import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import android.app.Application;

public class MonitorApplication extends Application {
	
	  private URI masterUri;
	  private final NodeMainExecutor nodeMainExecutor;
	  private NodeConfiguration nodeConfiguration;
	  
	  public MonitorApplication() {
		  nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
	  }
	  
	  public NodeMainExecutor getNodeMainExecutor(){
		  return nodeMainExecutor;
	  }
	  
	  public URI getMasterURI(){
		  return masterUri;
	  }
	  
	  public void setMasterURI(URI newURI){
		  masterUri = newURI;
	  }
	  
	  public NodeConfiguration getNodeConfiguration(){
		  return nodeConfiguration;
	  }
	  
	  public void setNodeConfiguation(NodeConfiguration nc){
		  nodeConfiguration = nc;
	  }

}
