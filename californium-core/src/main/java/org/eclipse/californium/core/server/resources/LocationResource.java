package org.eclipse.californium.core.server.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class LocationResource extends CoapResource{
	
	private String location;
	private String epName;
	
	public LocationResource(String name, String location, String epName) {
		super(name);
		this.location = location;
		this.epName = epName;
		
		getAttributes().addResourceType("location");
		getAttributes().addContentType(41);
		getAttributes().addInterfaceDescription("sensor");
		
	}

	@Override
	public void handleGET(CoapExchange exchange) {
//		System.out.println("Thelw na apadisw ston "+exchange.getSourceAddress().getHostAddress()+":"
//				+ exchange.getSourcePort()+" me payload: ["+exchange.getRequestText()+"] !!! 1");
//		int i = GlobalData.random.nextInt(2000) + 1;
//		if(i <= 1000)
//			resourceVal++;
		exchange.respond(ResponseCode.CONTENT, String.valueOf(location)+";"+epName);
	}

}
