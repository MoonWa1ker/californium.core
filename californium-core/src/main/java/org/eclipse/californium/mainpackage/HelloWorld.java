package org.eclipse.californium.mainpackage;

import java.io.IOException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.MyUDPConnector;
import org.eclipse.californium.core.network.interceptors.MessageTracer;
//import org.eclipse.californium.core.rd.ResourceDirectory;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.tools.resources.RDGroupResource;
import org.eclipse.californium.tools.resources.RDLookUpTopResource;
import org.eclipse.californium.tools.resources.RDResource;
import org.eclipse.californium.tools.resources.RDTagTopResource;

public class HelloWorld {
	private static final long CLIENT_TIMEOUT = 2000;
	
	public static void main(String[] args) {
		// SERVER 1
		CoapServer server1 = new CoapServer();
		CoAPEndpoint server1_cEP = new CoAPEndpoint(5683);
		server1.addEndpoint(server1_cEP);
		server1.add(new CoapResource("HelloWorld_Resource_Name") {
			public void handleGET(CoapExchange exchange) {
				System.out.println("Thelw na apadisw ston "+exchange.getSourceAddress().getHostAddress()+":"
						+ exchange.getSourcePort()+" me payload: ["+exchange.getRequestText()+"] !!! 1");
				exchange.respond(ResponseCode.CONTENT, "Hello - World!!!1");
			}
		});
		server1.start();
		 
		System.out.println("[server1]Root resource: " + server1.getRoot().getName());
		for(Resource r : server1.getRoot().getChildren()){
			System.out.println("[server1]Child resource: " + r.getName());
		}
		
		// SERVER 2
		CoapServer server2 = new CoapServer();
		CoAPEndpoint server2_cEP = new CoAPEndpoint(5683);
		server2.addEndpoint(server2_cEP);
		server2.add(new CoapResource("HelloWorld_Resource_Name") {
			public void handleGET(CoapExchange exchange) {
				System.out.println("Thelw na apadisw ston "+exchange.getSourceAddress().getHostAddress()+":"
						+ exchange.getSourcePort()+" me payload: ["+exchange.getRequestText()+"] !!! 2");
				exchange.respond(ResponseCode.CONTENT, "Hello - World!!!2");
			}
		});	
		server2.start();
		 
		System.out.println("[server2]Root resource: " + server2.getRoot().getName());
		for(Resource r : server2.getRoot().getChildren()){
			System.out.println("[server2]Child resource: " + r.getName());
		}

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
		//client.setTimeout(2000);
		client.discoverRD("coap://224.0.1.187", "rt=core.rd*");
		
		//CLIENT INITIALIZES RD WITH SOME DATA 1
		Request request = new Request(Code.POST);
		request.setURI("coap://"+client.rdList.get(0).getAddress()
		+ client.rdList.get(0).getRdPath()+"?ep=node1");
		request.setPayload("</sensors/temp>;ct=41;rt='temperature-c';if='sensor',"
				+ "</sensors/light>;ct=41;rt='light-lux';if='sensor'");
		CoapResponse response = client.advanced(request);
		String locationPath2 = response.getOptions().getLocationPathString();
		System.out.println("Initialize1: CODE["+response.getCode().toString()+"] Location["+locationPath2+"]");
		
		//LOOK UP THE RESOURCES SO FAR
		request = new Request(Code.GET);
		request.setURI("coap://"+client.rdList.get(0).getAddress()
				+ client.rdList.get(0).getRdLookupPath() +"/res");
		response = client.advanced(request);
		System.out.println("Lookup: CODE["+response.getCode().toString()+"]"+" Payload[" + response.advanced().getPayloadString()+"]");

		//CLIENT INITIALIZES RD WITH SOME DATA 2
		request = new Request(Code.POST);
		request.setURI("coap://"+client.rdList.get(0).getAddress()
		+ client.rdList.get(0).getRdPath()+"?ep=node2");
		request.setPayload("</sensors/humid>;ct=41;rt='humidity';if='sensor'");
		response = client.advanced(request);
		locationPath2 = response.getOptions().getLocationPathString();
		System.out.println("Initialize2: CODE["+response.getCode().toString()+"] Location["+locationPath2+"]");
		
		//REGISTERING A GROUP(group1[node1, node2])
		request = new Request(Code.POST);
		request.setURI("coap://"+client.rdList.get(0).getAddress()
		+ client.rdList.get(0).getRdGroupPath()+"?gp=group1&con='224.0.1.188'");
		System.out.println("["+request.getURI()+"]");
		request.setPayload("<>;ep='node1', <>;ep='node2'");
		response = client.advanced(request);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		request = new Request(Code.GET);//allnodes 224.0.1.187   HelloWorld_Resource_Name 
		request.setURI("coap://224.0.1.188/HelloWorld_Resource_Name");
		request.setMulticast(true);
		request.setConfirmable(false);
		TimedHandler myHandler = new TimedHandler(CLIENT_TIMEOUT);
		client.advanced(myHandler, request);
		
		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		//REGISTERING A GROUP(group1[node1, node2])
//		request = new Request(Code.DELETE);
//		request.setURI("coap://"+client.rdList.get(0).getAddress()
//		+ client.rdList.get(0).getRdGroupPath()+"/group1");
//		System.out.println("["+request.getURI()+"]");
//		//request.setPayload("<>;ep='node1', <>;ep='node2'");
//		response = client.advanced(request);
//		
//		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		request = new Request(Code.GET);//allnodes 224.0.1.187   HelloWorld_Resource_Name 
//		request.setURI("coap://224.0.1.188/HelloWorld_Resource_Name");
//		request.setMulticast(true);
//		request.setConfirmable(false);
//		myHandler = new TimedHandler(CLIENT_TIMEOUT);
//		client.advanced(myHandler, request);
		
/*
		//REGISTERING A GROUP(group2[node1, node3])
		request = new Request(Code.POST);
		request.setURI("coap://"+client.rdList.get(0).getAddress()
		+ client.rdList.get(0).getRdGroupPath()+"?gp=group2");
		System.out.println("["+request.getURI()+"]");
		request.setPayload("<>;ep='node1', <111.111.111.110:5683>;ep='node3'");
		response = client.advanced(request);
		
		//REGISTERING A GROUP(group3[node1, node2])
		request = new Request(Code.POST);
		request.setURI("coap://"+client.rdList.get(0).getAddress()
		+ client.rdList.get(0).getRdGroupPath()+"?gp=group3");
		System.out.println("["+request.getURI()+"]");
		request.setPayload("<>;ep='node1', <>;ep='node2'");
		response = client.advanced(request);
		
		if(response != null){
//			//System.out.println(response.advanced().getPayloadString());
			String locationPath = response.getOptions().getLocationPathString();
			System.out.println("Group reg: CODE["+response.getCode().toString()+"] Location["+locationPath+"]");
//			request = new Request(Code.GET);
//			request.setURI("coap://"+client.rdList.get(0).getAddress()
//					+ "/rd-lookup/res?rt=temperature");
//			System.out.println("["+request.getURI()+"]");
//			response = client.advanced(request);
//			if(response != null)
//				System.out.println("CODE["+response.getCode().toString()+"]"+" Payload[" + response.advanced().getPayloadString()+"]");
//			else
//				System.out.println("NULL RESPONSE2");
		}
		
		//LOOK UP THE GROUP RESOURCES SO FAR /rd-lookup/res
		request = new Request(Code.GET);
		request.setURI("coap://"+client.rdList.get(0).getAddress()
				+ client.rdList.get(0).getRdLookupPath() +"/ep?gp=group1");
		response = client.advanced(request);
		System.out.println("Group Lookup(/ep): CODE["+response.getCode().toString()+"]"+" Payload[" + response.advanced().getPayloadString()+"]");
		
		//LOOK UP THE GROUP RESOURCES SO FAR /rd-lookup/gp
		request = new Request(Code.GET);
		request.setURI("coap://"+client.rdList.get(0).getAddress()
				+ client.rdList.get(0).getRdLookupPath() +"/gp");
		response = client.advanced(request);
		System.out.println("Group Lookup(/gp): CODE["+response.getCode().toString()+"]"+" Payload[" + response.advanced().getPayloadString()+"]");
*/
//		else
//			System.out.println("NULL RESPONSE1");
		
////		"coap://224.0.1.187:5685/.well-known/core?rt=core.rd"
//		Request request = new Request(Code.GET);//allnodes 224.0.1.187   HelloWorld_Resource_Name 
//		request.setURI("coap://224.0.1.187/.well-known/core?rt=core.rd");
//		request.setPayload("Hi, i am the client!");
//		request.setMulticast(true);
//		request.setConfirmable(false);
//		TimedHandler myHandler = new TimedHandler(CLIENT_TIMEOUT);
//		client.advanced(myHandler, request);
		
		
		
		
		
//		if(response == null)
//			System.out.println("GURISE NULL RESPONSE( EKANE TIMEOUT TO WAITFORRESPONSE!!!)");
//		else
//			System.out.println("MOU GURISE RESPONSE KANONIKO..... :/");
		
//		CoAPEndpoint cEP = (CoAPEndpoint)server1.getEndpoint(5685);
//		MyUDPConnector myUDPc = (MyUDPConnector)cEP.getConnector();
//		
//		try {
//			myUDPc.joinGroup("228.5.6.7");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//		
//		
//		
//		response = null;
//		request = new Request(Code.GET);
//		request.setURI("coap://228.5.6.7:5684/HelloWorld_Resource_Name/");
//		request.setPayload("Hi, i am the client!");
//		request.setMulticast(true);
//		response = client.advanced(request);
//		if(response == null)
//			System.out.println("GURISE NULL RESPONSE( EKANE TIMEOUT TO WAITFORRESPONSE!!!)");
//		else
//			System.out.println("MOU GURISE RESPONSE KANONIKO..... :/");
//		Response response = null;
//		request = request.send();
//		while(true){
//			try {
//				response = request.waitForResponse(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			if (response!=null) {
//					
//				System.out.println(response.getCode());
//				System.out.println(response.getOptions());
//				System.out.println(response.getPayloadString());
//					
//				System.out.println("\nADVANCED\n");
//				// access advanced API with access to more details through .advanced()
//				System.out.println(Utils.prettyPrint(response));
//				
//			} else {
//				System.out.println("No response received.");
//				//Otan mpei edw einai timeout. Prepei edw na exw to exchange
//				//wste na kanw exchange.setComplete kai na ginei xamos.
//				
//				//Prepei na vrw tropo na kanw sto UDPConnector join se group 
//				//kai leave apo group gia to multicast. Wste na dokimasw
//				// na kanw kai tous 2 servers join se ena group. Na kanw
//				// leave me ton enan kai na dw an o allos tha sunexisei n 
//				// pairnei ta multicast minimata!
//			}
//		}
		//System.exit(0);
	}

}
