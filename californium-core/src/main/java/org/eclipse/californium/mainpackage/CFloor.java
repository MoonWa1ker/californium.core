package org.eclipse.californium.mainpackage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.RDInterfaceContext;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.server.resources.CustomResource;
import org.eclipse.californium.core.server.resources.LocationResource;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.core.server.resources.ResourceAttributes;
import org.eclipse.californium.mainpackage.globaldata.GlobalData;
import org.eclipse.californium.tools.resources.RDGroupResource;
import org.eclipse.californium.tools.resources.RDLookUpTopResource;
import org.eclipse.californium.tools.resources.RDResource;
import org.eclipse.californium.tools.resources.RDTagTopResource;

public class CFloor {

	private static List<CoapServer> sensors;
	private static CoapServer rdServer;
	private static int RESNUM = 2;
	private static int RESINITVAL = 10;
	private static int SENSORNUM;
	
	public static void main(String[] args) {
		if(args.length == 2) {
			GlobalData.FloorNum = String.valueOf(Integer.parseInt(args[1]));
			SENSORNUM = Integer.parseInt(args[0]);
			System.out.println("This is floor ["+GlobalData.FloorNum+"]. "
					+ "Creating ["+SENSORNUM+"] sensors!");
		}
		else{
			System.out.println("Few arguments. Expecting two(2) arguments!(SENSORNUM, FLOORNUM)");
			System.exit(1);
		}
		CaliforniumLogger.disableLogging();
		
		sensors = new ArrayList<CoapServer>(0);
		
		initSensors(SENSORNUM);
		initRD();
		
		CoapClient client = new CoapClient();
		client.useNONs();
		CoAPEndpoint client_cEP = new CoAPEndpoint();
		client.setEndpoint(client_cEP);
		client.discoverRD("coap://224.0.1.187", "rt=core.rd*");
//		for(RDInterfaceContext c : client.rdList)
//			System.out.println("Found RD["+c.getRdPath()+"], "
//					+ "["+c.getRdGroupPath()+"], ["+c.getRdLookupPath()+"], "
//							+ "["+c.getAddress()+"]");
		RDInterfaceContext rdCtx = getFloorRDCtx(client.rdList);
		
//		if(client.rdList.size() >= 2) {
//			System.out.println("Found more than one RD. Exiting...");
//			System.exit(1);
//		}
		
		if(rdCtx == null){
			System.out.println("No RD Context found. Exiting...");
			System.exit(1);
		}
		
		registerResources(rdCtx);
	}
	
	private static void initSensors(int n){
		for(int s = 0; s < n; s++){
			CoapServer server = new CoapServer();
			server.epName = GlobalData.FloorNum+"sensor"+s;
			CoAPEndpoint server_cEP = new CoAPEndpoint(5683);
			server.addEndpoint(server_cEP);
			
			//Add all the resources except one chosen at random.
			int resPos = GlobalData.random.nextInt(GlobalData.res.length);
			for(int j = 0;j < GlobalData.res.length; j++)
				if(j != resPos)
					server.add(new CustomResource(GlobalData.res[j], RESINITVAL, server.epName));
			
			//Add the location resource
			int locPos = GlobalData.random.nextInt(5);//GlobalData.locRes.length);
			server.add(new LocationResource("location", GlobalData.locRes[locPos], server.epName));
			
			server.start();
			sensors.add(server);
		}
	}
	
	private static void initRD(){
		rdServer = new CoapServer();
		CoAPEndpoint rd_cEP = new CoAPEndpoint(5683);
		rdServer.addEndpoint(rd_cEP);
		RDResource rdResource = new RDResource("rd"+GlobalData.FloorNum); 
        // add resources to the server
		rdServer.add(rdResource);
		rdServer.add(new RDLookUpTopResource("rd-lookup"+GlobalData.FloorNum, rdResource));
		rdServer.add(new RDGroupResource("rd-group"+GlobalData.FloorNum, rdResource));
		rdServer.add(new RDTagTopResource("tags"+GlobalData.FloorNum, rdResource));
		rdServer.start();
	}

	private static void registerResources(RDInterfaceContext RDctx){
		for(int i = 0; i < sensors.size();i ++){
			CoapServer s = sensors.get(i);
			String serverEPName = s.epName;
			StringBuilder sB = new StringBuilder("");
			Collection<Resource> resources = s.getRoot().getChildren();
			
			int resCounter = resources.size() - 1; // subtract the .well-known
			for(Resource r : resources){
				if(r.getName().equals(".well-known"))
					continue;
				ResourceAttributes attributes = r.getAttributes();
				
				sB.append("<" + r.getURI() + ">;");	
				sB.append(LinkFormat.CONTENT_TYPE+"="+attributes.getContentTypes().get(0)+";");
				sB.append(LinkFormat.RESOURCE_TYPE+"='"+attributes.getResourceTypes().get(0)+"';");
				sB.append(LinkFormat.INTERFACE_DESCRIPTION+"='"+attributes.getInterfaceDescriptions().get(0)+"'");
				
				resCounter--;
				if(resCounter != 0)
					sB.append(",");
			}

			Request request = new Request(Code.POST);
			request.setURI("coap://"+RDctx.getAddress()
			+ RDctx.getRdPath()+"?ep="+serverEPName);
			request.setPayload(sB.toString());
			
			CoapClient client = new CoapClient();
			client.useNONs();
			CoapResponse response = client.advanced(request);
//			String locationPath2 = response.getOptions().getLocationPathString();
//			System.out.println("Initialize1: CODE["+response.getCode().toString()+"] Location["+locationPath2+"]");
			
			if(response.getCode() == ResponseCode.CREATED)
				System.out.println("EP["+serverEPName+"] SUCCESS!!");
			else
				System.out.println("EP["+serverEPName+"] ERROR!!");
			
		}
	}
	
	private static RDInterfaceContext getFloorRDCtx(List<RDInterfaceContext> list){
		for(RDInterfaceContext c : list){
			if(c.getRdPath().equals("/rd"+GlobalData.FloorNum))
				return c;
		}
		
		return null;
	}
}
