package org.eclipse.californium.core.rd;

import java.util.List;

import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

public class FunctionSet extends CoapResource{

	public FunctionSet(String name) {
		// TODO Auto-generated constructor stub
		super(name);
		
        // set display name
        getAttributes().addResourceType("core.rd");
	}
	
	@Override
	public void handleGET(CoapExchange exchange) {
		
		exchange.respond(ResponseCode.CONTENT, "hi this is /rd", MediaTypeRegistry.APPLICATION_LINK_FORMAT);
	}
	

}
