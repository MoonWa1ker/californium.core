package org.eclipse.californium.core.rd;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class LookupFunctionSet extends CoapResource{

	public LookupFunctionSet(String name) {
		// TODO Auto-generated constructor stub
		super(name);
		
        // set display name
        getAttributes().addResourceType("core.rd-lookup");
	}
	
	@Override
	public void handleGET(CoapExchange exchange) {
		
		exchange.respond(ResponseCode.CONTENT, "hi this is /rd-lookup", MediaTypeRegistry.APPLICATION_LINK_FORMAT);
	}
}
