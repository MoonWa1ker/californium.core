package org.eclipse.californium.core.rd;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class GroupFunctionSet extends CoapResource{

	public GroupFunctionSet(String name) {
		// TODO Auto-generated constructor stub
		super(name);
		
        // set display name
        getAttributes().addResourceType("core.rd-group");
	}
	
	@Override
	public void handleGET(CoapExchange exchange) {
		
		exchange.respond(ResponseCode.CONTENT, "hi this is /rd-group", MediaTypeRegistry.APPLICATION_LINK_FORMAT);
	}
}
