package org.eclipse.californium.mainpackage;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.mainpackage.globaldata.GlobalData;

public class MainClass {

	private static List<SensorData> sensorData = new ArrayList<SensorData>(0);
	
	public static void main(String[] args) {
		CaliforniumLogger.disableLogging();
		
		CoapClient client = new CoapClient();
		CoAPEndpoint client_cEP = new CoAPEndpoint();
		client.setEndpoint(client_cEP);
		client.useCONs();
		
		// Lookup resources in RD of Floor 1
		Request request = new Request(Code.GET);
		System.out.println("Looking up for all resources at Floor 1.");
		request.setURI("coap://192.168.42.37/rd-lookup1/res");
		CoapResponse response = client.advanced(request);
		storeSensorData(response, "1");
		
		// Lookup resources in RD of Floor 2
		request = new Request(Code.GET);
		System.out.println("Looking up for all resources at Floor 2.");
		request.setURI("coap://192.168.42.88/rd-lookup2/res");
		response = client.advanced(request);
		storeSensorData(response, "2");
		
		System.out.println("Printing what i have so far...");
		printSensorData();
		
		// Create groups based on resource types. (3 types = 3 groups in each RD)
		createGroupsByRT();
		
		// Lookup groups in RD of Floor 1
		request = new Request(Code.GET);
		System.out.println("Looking up for all groups at Floor 1.");
		request.setURI("coap://192.168.42.37/rd-lookup1/gp");
		response = client.advanced(request);
		System.out.println(Utils.prettyPrint(response));
		
		// Lookup groups in RD of Floor 2
		request = new Request(Code.GET);
		System.out.println("Looking up for all groups at Floor 2.");
		request.setURI("coap://192.168.42.88/rd-lookup2/gp");
		response = client.advanced(request);
		System.out.println(Utils.prettyPrint(response));
		
		// Lookup resources in RD of Floor 1
		request = new Request(Code.GET);
		client.useNONs();
		request.setMulticast(true);
		request.setConfirmable(false);
		System.out.println("Requesting the location value of all endpoints.");
		request.setURI("coap://224.0.1.187/location");
		client.advanced(new CoapHandler() {
			
			@Override
			public void onLoad(CoapResponse response) {
				String payload = response.getResponseText();
				String[] payloadParts = payload.split(";");//[0] - loc , [1] - epName
				for(SensorData d : sensorData){
					if(d.getEpName().equals(payloadParts[1])){
						d.setLoc(payloadParts[0]);
						break;
					}
						
				}
				
			}
			
			@Override
			public void onError() {
				// TODO Auto-generated method stub
				
			}
		},request);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Printing what i have so far...");
		printSensorData();
				
		createScenarioGP();
		
		client.useCONs();
		// Lookup endpoints in RD of Floor 1 that belong to scgp1
		request = new Request(Code.GET);
		System.out.println("Looking up for all endpoints of scgp at Floor 1.");
		request.setURI("coap://192.168.42.37/rd-lookup1/ep?gp=scgp1");
		response = client.advanced(request);
		System.out.println(Utils.prettyPrint(response));
		
		// Lookup endpoints in RD of Floor 2 that belong to scgp2
		request = new Request(Code.GET);
		System.out.println("Looking up for all endpoints of scgp at Floor 2.");
		request.setURI("coap://192.168.42.88/rd-lookup2/ep?gp=scgp2");
		response = client.advanced(request);
		System.out.println(Utils.prettyPrint(response));
		
		// Lookup resources in RD of Floor 1
		request = new Request(Code.GET);
		client.useNONs();
		request.setMulticast(true);
		request.setConfirmable(false);
		System.out.println("Requesting the temperature value of all endpoints of scgp.");
		request.setURI("coap://224.0.1.200/temp");
		client.advanced(new CoapHandler() {
			
			@Override
			public void onLoad(CoapResponse response) {
				String payload = response.getResponseText();
				String[] payloadParts = payload.split(";");//[0] - temp , [1] - epName
				if(payloadParts[0].equals("40"))
					System.out.println("EP["+payloadParts[1]+"] temp["+payloadParts[0]+"]");
				
			}
			
			@Override
			public void onError() {
				// TODO Auto-generated method stub
				
			}
		},request);
		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		client.useNONs();
//		request = new Request(Code.GET);//allnodes 224.0.1.187   HelloWorld_Resource_Name 
//		request.setURI("coap://224.0.1.188/temp");
//		request.setMulticast(true);
//		request.setConfirmable(false);
//		TimedHandler myHandler = new TimedHandler(2000);
//		client.advanced(myHandler, request);
		
		
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		t.start();
	}
	
	private static void createScenarioGP(){
		List<String> endpoints1 = new ArrayList<String>(0);
		List<String> endpoints2 = new ArrayList<String>(0);
		for(SensorData d : sensorData){
			for(SensorResource r : d.getResources()){
				if(r.getType().equals("temp") && 
						(d.getLoc().equals("W") || 
								d.getLoc().equals("NW") || 
								d.getLoc().equals("SW"))){
					if(d.getFloor().equals("1"))
						endpoints1.add(d.getEpName());
					else
						endpoints2.add(d.getEpName());
					break;
				}
			}
		}
		
		// Create the client that will make the request to create the groups
		CoapClient client = new CoapClient();
		CoAPEndpoint client_cEP = new CoAPEndpoint();
		client.setEndpoint(client_cEP);
		client.useCONs();
		
		// Create the request to floor 1
		if(!endpoints1.isEmpty()){
			
			Request request = new Request(Code.POST);	
			request.setURI("coap://192.168.42.37/rd-group1/?gp=scgp1&con='224.0.1.200'");
			StringBuilder sb = new StringBuilder("");
			for(int i = 0; i < endpoints1.size(); i++){
				sb.append("<>;ep='"+endpoints1.get(i)+"'");
				if(i != endpoints1.size() - 1)
					sb.append(", ");
			}
			
			request.setPayload(sb.toString());
			CoapResponse response = client.advanced(request);
			
			System.out.print("GP[scgp1] IP[224.0.1.200]" );
			if(response.getCode() == ResponseCode.CREATED)
				System.out.println("SUCCESS!! LOC["+response.getOptions().getLocationPathString()+"]");
			else
				System.out.println("ERROR!!");
		}
		
		// Create the request to floor 2
		if(!endpoints2.isEmpty()){
			
			Request request = new Request(Code.POST);	
			request.setURI("coap://192.168.42.88/rd-group2/?gp=scgp2&con='224.0.1.200'");
			StringBuilder sb = new StringBuilder("");
			for(int i = 0; i < endpoints2.size(); i++){
				sb.append("<>;ep='"+endpoints2.get(i)+"'");
				if(i != endpoints2.size() - 1)
					sb.append(", ");
			}
			
			request.setPayload(sb.toString());
			CoapResponse response = client.advanced(request);
			
			System.out.print("GP[scgp2] IP[224.0.1.200]" );
			if(response.getCode() == ResponseCode.CREATED)
				System.out.println("SUCCESS!! LOC["+response.getOptions().getLocationPathString()+"]");
			else
				System.out.println("ERROR!!");
		}
	}
	
	private static void createGroupsByRT(){
		int mcastIP = 188; // 224.0.1.188 first available mcast ip
		for(String resourceType : GlobalData.res){
//			if(!resourceType.equals("temp"))
//				continue;
			// Gather the endpointNames of the sensors that have a resource of type resourceType
			List<String> endpoints1 = new ArrayList<String>(0);
			List<String> endpoints2 = new ArrayList<String>(0);
			for(SensorData d : sensorData){
				for(SensorResource r : d.getResources()){
					if(r.getType().equals(resourceType)){
						if(d.getFloor().equals("1"))
							endpoints1.add(d.getEpName());
						else
							endpoints2.add(d.getEpName());
						break;
					}
				}
			}
//			if(endpoints.isEmpty())
//				continue;
			// Create the client that will make the request to create the groups
			CoapClient client = new CoapClient();
			CoAPEndpoint client_cEP = new CoAPEndpoint();
			client.setEndpoint(client_cEP);
			client.useCONs();
			
			
			String groupIP = "224.0.1."+mcastIP;
			String groupName = "group"+resourceType;
			
			// Create the request to floor 1
			if(!endpoints1.isEmpty()){
				
				Request request = new Request(Code.POST);	
				request.setURI("coap://192.168.42.37/rd-group1/?gp="+groupName+"1"+"&con='"+groupIP+"'");
				StringBuilder sb = new StringBuilder("");
				for(int i = 0; i < endpoints1.size(); i++){
					sb.append("<>;ep='"+endpoints1.get(i)+"'");
					if(i != endpoints1.size() - 1)
						sb.append(", ");
				}
				
				request.setPayload(sb.toString());
				CoapResponse response = client.advanced(request);
				
				System.out.print("GP["+groupName+"1"+"] IP["+groupIP+"]" );
				if(response.getCode() == ResponseCode.CREATED)
					System.out.println("SUCCESS!! LOC["+response.getOptions().getLocationPathString()+"]");
				else
					System.out.println("ERROR!!");
			}
			
			// Create the request to floor 2
			if(!endpoints2.isEmpty()){
				
				Request request = new Request(Code.POST);	
				request.setURI("coap://192.168.42.88/rd-group2/?gp="+groupName+"2"+"&con='"+groupIP+"'");
				StringBuilder sb = new StringBuilder("");
				for(int i = 0; i < endpoints2.size(); i++){
					sb.append("<>;ep='"+endpoints2.get(i)+"'");
					if(i != endpoints2.size() - 1)
						sb.append(", ");
				}
				
				request.setPayload(sb.toString());
				CoapResponse response = client.advanced(request);
				
				System.out.print("GP["+groupName+"2"+"] IP["+groupIP+"]" );
				if(response.getCode() == ResponseCode.CREATED)
					System.out.println("SUCCESS!! LOC["+response.getOptions().getLocationPathString()+"]");
				else
					System.out.println("ERROR!!");
			}
			
			mcastIP++;
		}
	}
	
	private static void storeSensorData(CoapResponse response, String floor){
		String[] rdResources = response.getResponseText().replaceAll(",,", ",").split(",");
		System.out.println("Printing rd resources...");
		for(String resource : rdResources){
			System.out.println(resource);
			String[] resParts = resource.split(";");
			// Locate the endpoint name
			String ep = "";
			for(String p : resParts){
				String[] tempVar = p.split(LinkFormat.END_POINT+"=");
				if(tempVar[0].equals("")) {
					ep = tempVar[1].replaceAll("\"", "");
					break;
				}
			}
			
			//Create new SensorData if it doesnt exist yet
			SensorData data = null;
			for(SensorData d : sensorData) {
				if(d.epName.equals(ep)){
					data = d;
					break;
				}
			}
			if(data == null) {
				data = new SensorData(ep, floor);
				sensorData.add(data);
			}
			
			// Find the resource type 
			String rt = "";
			for(String p : resParts){
				String[] tempVar = p.split(LinkFormat.RESOURCE_TYPE+"=");
				if(tempVar[0].equals("")) {
					rt = tempVar[1].replaceAll("\"", "");
					// change the type slightly to match the GlobalData.res
					for(String t : GlobalData.res)
						if(rt.contains(t)) {
							rt = t;
							break;
						}
					break;
				}
			}
			// Find the IP address
			String path = "";
			path = resParts[0].replaceAll("<", "");
			path = path.replaceAll(">", "");
			
			//Add the newly created resource
			data.addResource(new SensorResource(path, rt));
			
		}
	}
	
	private static void printSensorData(){
		for(SensorData d : sensorData){
			System.out.print(d.getEpName());
			for(int i = 0; i < d.getResources().size() ; i++){
				if(i == 0)
					System.out.print("\t");
				else
					System.out.print("\t\t");
				SensorResource r = d.getResources().get(i);
				System.out.print(r.getPath());
				if(r.getType().equals("temp"))
					System.out.print("\t\t");
				else
					System.out.print("\t");
				System.out.println(r.getType());
			}
			System.out.println("\t\t"+d.getLoc());
			System.out.println("");
		}
	}
	
	private static class SensorData {
		private String epName;
		private String loc;
		private String floor;
		private List<SensorResource> resources;
		
		public SensorData(String epName, String floor){
			this.epName = epName;
			this.floor = floor;
			this.loc = "";
			this.resources = new ArrayList<SensorResource>(0);
		}
		
		public void addResource(SensorResource resource){
			resources.add(resource);
		}

		public String getEpName() {
			return epName;
		}

		public List<SensorResource> getResources() {
			return resources;
		}

		public String getFloor() {
			return floor;
		}

		public String getLoc() {
			return loc;
		}

		public void setLoc(String loc) {
			this.loc = loc;
		}
		
		
	}
	
	private static class SensorResource{
		private String path;
		private String type;
		
		public SensorResource(String path, String type){
			this.path = path;
			this.type = type;
		}

		public String getPath() {
			return path;
		}

		public String getType() {
			return type;
		}
		
		
	}

}
