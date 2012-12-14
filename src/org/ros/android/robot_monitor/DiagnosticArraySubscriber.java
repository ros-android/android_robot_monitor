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

import org.ros.node.ConnectedNode;
import org.ros.android.MessageCallable;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import diagnostic_msgs.DiagnosticArray;

/**
 * @author chadrockey@gmail.com (Chad Rockey)
 */
public class DiagnosticArraySubscriber implements NodeMain
{
  MessageCallable<DiagnosticArray, DiagnosticArray> callable;
  
  DiagnosticArray last_message;
  
  public DiagnosticArraySubscriber()
  {
  }
  
  public void setMessageCallable(MessageCallable<DiagnosticArray, DiagnosticArray> callable){
	  this.callable = callable;
  }
  
  public void clearCallable(){
	  this.callable = null;
  }

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("android_gingerbread/ros_text_view");
  }
  
  public DiagnosticArray getLastMessage(){
	  return this.last_message;
  }

  @Override
  public void onStart(ConnectedNode connectedNode) {
    Subscriber<DiagnosticArray> subscriber = connectedNode.newSubscriber("/diagnostics_agg", "diagnostic_msgs/DiagnosticArray");
    subscriber.addMessageListener(new MessageListener<DiagnosticArray>() {
      @Override
      public void onNewMessage(final DiagnosticArray message) {
    	if(callable != null){
    		callable.call(message);
    	}
    	DiagnosticArraySubscriber.this.last_message = message;
      }
    });
  }

  @Override
  public void onShutdown(Node node) {
  }

  @Override
  public void onShutdownComplete(Node node) {
  }

  @Override
  public void onError(Node node, Throwable throwable) {
  }

}