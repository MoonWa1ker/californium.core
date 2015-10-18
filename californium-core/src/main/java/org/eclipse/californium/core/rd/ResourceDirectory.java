package org.eclipse.californium.core.rd;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoAPEndpoint;

/**
 * Implementation of a resource directory.
 * @author andrianeshsg@aueb.gr
 *
 */
public class ResourceDirectory{
	
	private RDServer server;
	
	public ResourceDirectory(){
		server = new RDServer();
		
		CoapResource rdResource = new FunctionSet("rd");
		server.add(rdResource);
		
		CoapResource rdLookupResource = new LookupFunctionSet("rd-lookup");
		server.add(rdLookupResource);
		
		CoapResource rdGroupResource = new GroupFunctionSet("rd-group");
		server.add(rdGroupResource);
	}
	
	public void startServer(){
		server.addEndpoint(new CoAPEndpoint(5683));
		server.start();
	}
}
