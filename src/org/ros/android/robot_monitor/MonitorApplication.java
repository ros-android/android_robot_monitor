package org.ros.android.robot_monitor;

import java.net.URI;
import java.util.Random;

import org.ros.message.diagnostic_msgs.DiagnosticArray;
import org.ros.namespace.GraphName;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Subscriber;

import android.app.Application;

public class MonitorApplication extends Application implements NodeMain {
	
	  private URI masterUri;
	  private NodeConfiguration nodeConfiguration;
	  private Subscriber<DiagnosticArray> subscriber;
	  private final NodeMainExecutor nodeMainExecutor;
	  //private Node node;
	  
	  public MonitorApplication() {
		  nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
	  }
	  
	  public NodeMainExecutor getNodeMainExecutor(){
		  return nodeMainExecutor;
	  }
	  
	  public URI getMasterURI(){
		  return masterUri;
	  }
	  
	  public void clear(){
		  masterUri = null;
		  subscriber = null;
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
	  
	  public Subscriber<DiagnosticArray> getSubscriber(){
		  return subscriber;
	  }
	  
	  public void setSubscriber(Subscriber<DiagnosticArray> sub){
		  this.subscriber = sub;
	  }
	  
	  /*public Node getNode(){
		  return node;
	  }*/
	  
	  /*public NodeMain getNodeMain(){
		  return this;
	  }*/
	  
	@Override
	public void onStart(Node node) {
		//this.node = node;
		subscriber = node.newSubscriber("/diagnostics_agg", "diagnostic_msgs/DiagnosticArray");
	}

	@Override
	public void onShutdown(Node node) {
		//this.subscriber.shutdown();
	}

	@Override
	public void onShutdownComplete(Node node) {
		
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
