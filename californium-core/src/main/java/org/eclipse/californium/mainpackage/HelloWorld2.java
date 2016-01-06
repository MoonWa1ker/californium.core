package org.eclipse.californium.mainpackage;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.MyUDPConnector;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.tools.resources.RDGroupResource;
import org.eclipse.californium.tools.resources.RDLookUpTopResource;
import org.eclipse.californium.tools.resources.RDResource;
import org.eclipse.californium.tools.resources.RDTagTopResource;

public class HelloWorld2 {

	public static void main(String[] args) {
		
		// SERVER 1
		CoapServer server1 = new CoapServer();
		CoAPEndpoint server1_cEP = new CoAPEndpoint(5683);
		server1.addEndpoint(server1_cEP);
		server1.add(new CoapResource("HelloWorld_Resource_Name") {
			public void handleGET(CoapExchange exchange) {
				System.out.println("Thelw na apadisw ston "+exchange.getSourceAddress().getHostAddress()+":"
						+ exchange.getSourcePort()+" me payload: ["+exchange.getRequestText()+"] !!! 1");
				exchange.respond(ResponseCode.CONTENT, "HelloWorld 1");
			}
		});
		server1.start();
		
//		System.out.println("[server1]Root resource: " + server1.getRoot().getName());
//		for(Resource r : server1.getRoot().getChildren()){
//			System.out.println("[server1]Child resource: " + r.getName());
//		}
		
//		// SERVER 2
//		CoapServer server2 = new CoapServer();
//		CoAPEndpoint server2_cEP = new CoAPEndpoint(5683);
//		server2.addEndpoint(server2_cEP);
//		server2.add(new CoapResource("HelloWorld_Resource_Name") {
//			public void handleGET(CoapExchange exchange) {
//				System.out.println("Thelw na apadisw ston "+exchange.getSourceAddress().getHostAddress()+":"
//						+ exchange.getSourcePort()+" me payload: ["+exchange.getRequestText()+"] !!! 2");
//				exchange.respond(ResponseCode.CONTENT, "HelloWorld 2");
//			}
//		});	
//		server2.start();
//		 
//		System.out.println("[server2]Root resource: " + server2.getRoot().getName());
//		for(Resource r : server2.getRoot().getChildren()){
//			System.out.println("[server2]Child resource: " + r.getName());
//		}
		
		// RESOURCE DIRECTORY SERVER
		CoapServer rdServer = new CoapServer();
		CoAPEndpoint rd_cEP = new CoAPEndpoint(5683);
		rdServer.addEndpoint(rd_cEP);
		RDResource rdResource = new RDResource(); 
        // add resources to the server
		rdServer.add(rdResource);
		rdServer.add(new RDLookUpTopResource(rdResource));
		rdServer.add(new RDGroupResource(rdResource));
		rdServer.add(new RDTagTopResource(rdResource));
		rdServer.start();
		
		// CLIENT
		CoapClient client = new CoapClient();
		client.useNONs();
		CoAPEndpoint client_cEP = new CoAPEndpoint(9999);
		client.setEndpoint(client_cEP);
		
		//CLIENT INITIALIZES RD WITH SOME DATA 1
		Request request = new Request(Code.POST);
		request.setURI("coap://localhost/rd/?ep=node1");
		request.setPayload("</sensors/temp>;ct=41;rt='temperature-c';if='sensor',"
				+ "</sensors/light>;ct=41;rt='light-lux';if='sensor'");
		CoapResponse response = client.advanced(request);
		String locationPath2 = response.getOptions().getLocationPathString();
		System.out.println("Initialize Sensors: CODE["+response.getCode().toString()+"] Location["+locationPath2+"]");
		
		//LOOK UP THE SENSOR RESOURCES SO FAR
		request = new Request(Code.GET);
		request.setURI("coap://localhost/rd-lookup/res");
		response = client.advanced(request);
		System.out.println("Lookup All resources: CODE["+response.getCode().toString()+"]"+" Payload[" + response.advanced().getPayloadString()+"]");
		
		// CREATE GROUP
		request = new Request(Code.POST);
		request.setURI("coap://localhost/rd-group?gp=group1&con='224.0.1.188'");
		request.setPayload("<>;ep='node1'");
		response = client.advanced(request);
		locationPath2 = response.getOptions().getLocationPathString();
		System.out.println("Initialize Group1 : CODE["+response.getCode().toString()+"] Location["+locationPath2+"]");
		
		// LOOKUP THE GROUP RESOURCES SO FAR
		request = new Request(Code.GET);
		request.setURI("coap://localhost/rd-lookup/gp");
		response = client.advanced(request);
		System.out.println("Group Lookup(/gp): CODE["+response.getCode().toString()+"]"+" Payload[" + response.advanced().getPayloadString()+"]");
		
		// CREATE GROUP
		request = new Request(Code.POST);
		request.setURI("coap://localhost/rd-group?gp=group1&con='224.0.1.189'");
		request.setPayload("<>;ep='node1'");
		response = client.advanced(request);
		locationPath2 = response.getOptions().getLocationPathString();
		System.out.println("Initialize Group2 : CODE["+response.getCode().toString()+"] Location["+locationPath2+"]");
//		request = new Request(Code.GET);
//		request.setConfirmable(false);
//		request.setURI("coap://224.0.1.187/HelloWorld_Resource_Name");
//		client.advanced(new CoapHandler() {
//			
//			@Override
//			public void onLoad(CoapResponse response) {
//				// TODO Auto-generated method stub
//				System.out.println("Response: CODE["+response.getCode().toString()+"]"+" Payload[" + response.advanced().getPayloadString()+"]");
//			}
//			
//			@Override
//			public void onError() {
//				// TODO Auto-generated method stub
//				
//			}
//		}, request);
//		System.out.println("Response: CODE["+response.getCode().toString()+"]"+" Payload[" + response.advanced().getPayloadString()+"]");
	
	}

}
