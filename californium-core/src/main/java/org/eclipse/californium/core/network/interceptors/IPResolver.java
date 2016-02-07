package org.eclipse.californium.core.network.interceptors;

import java.util.logging.Logger;

import org.eclipse.californium.core.coap.EmptyMessage;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.mainpackage.globaldata.GlobalData;

/**
 * 
 *
 */
public class IPResolver implements MessageInterceptor {
	
	private final static Logger LOGGER = Logger.getLogger(IPResolver.class.getCanonicalName());
	
	private String epName;
	
	public IPResolver (String epName){
		this.epName = epName;
	}
	
	@Override
	public void sendRequest(Request request) {
		LOGGER.info(String.format("%s:%d <== req %s", request.getDestination(), request.getDestinationPort(), request));
	}
	
	@Override
	public void sendResponse(Response response) {
		LOGGER.info(String.format("%s:%d <== res %s", response.getDestination(), response.getDestinationPort(), response));
	}
	
	@Override
	public void sendEmptyMessage(EmptyMessage message) {
		LOGGER.info(String.format("%s:%d <== emp %s", message.getDestination(), message.getDestinationPort(), message));
	}
	
	@Override
	public void receiveRequest(Request request) {
		// If it is a group join/leave request inspect the payload to tell the destination of the message.
		// Since different sensors have the same IP this is a workaround solution.
		// (to put the endpoint_name in the payload so the destination coapserver can tell if 
		// the message is destined for it) 1 server = 1 epName.
		if(request.getOptions().hasOption(GlobalData.GROUPJOIN_OPT) || 
				request.getOptions().hasOption(GlobalData.GROUPLEAVE_OPT) ){
			String payload = request.getPayloadString();
			if(payload != null && payload != ""){
				System.out.println("EP["+epName+"]: Message is a group join/leave with payload: "+ payload);
				if(!payload.equals(epName))
					request.setCanceled(true);
			}
		}
	}
	
	@Override
	public void receiveResponse(Response response) {
		LOGGER.info(String.format("%s:%d ==> res %s", response.getSource(), response.getSourcePort(), response));
	}	

	@Override
	public void receiveEmptyMessage(EmptyMessage message) {
		LOGGER.info(String.format("%s:%d ==> emp %s", message.getSource(), message.getSourcePort(), message));
	}
}
