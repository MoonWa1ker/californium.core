package org.eclipse.californium.tools.resources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.mainpackage.globaldata.GlobalData;
import org.eclipse.californium.mainpackage.globaldata.RDGroupUtils;
import org.eclipse.californium.tools.resources.RDGroupNodeResource.GroupMemberCtx;

/**
 * Root group resource.
 * @author andrianeshsg
 *
 */
public class RDGroupResource extends CoapResource {

	private RDResource rdResource = null;
	
	public RDGroupResource(RDResource rd){
		this("rd-group", rd);
	}

	public RDGroupResource(String resourceIdentifier, RDResource rd) {
		super(resourceIdentifier);
		this.rdResource = rd;
		getAttributes().addResourceType("core.rd-group");
	}
	
	/*
	 * POSTs a new sub-resource to this resource. The name of the new
	 * sub-resource is a random number if not specified in the Option-query.
	 */
	@Override
	public void handlePOST(CoapExchange exchange) {
		
		// get name and lifetime from option query
		LinkAttribute attr;
		String groupIdentifier = "";
		String domain = "local";
		RDGroupNodeResource resource = null;
		List<GroupMemberCtx> oldMembers = new ArrayList<GroupMemberCtx>();
		List<GroupMemberCtx> newMembers = new ArrayList<GroupMemberCtx>();
		String oldCtx = "";
		String newCtx = "";
		ResponseCode responseCode;

		LOGGER.info("Group Registration request: "+exchange.getSourceAddress());
		
		List<String> query = exchange.getRequestOptions().getUriQuery();
		for (String q:query) {
			// FIXME Do not use Link attributes for URI template variables
			attr = LinkAttribute.parse(q);
			
			if (attr.getName().equals(LinkFormat.GROUP)) {
				groupIdentifier = attr.getValue();
			}
			
			if (attr.getName().equals(LinkFormat.DOMAIN)) {
				domain = attr.getValue();
			}
		}

		if (groupIdentifier.equals("")) {
			exchange.respond(ResponseCode.BAD_REQUEST, "Missing group (?gp)");
			LOGGER.info("Missing group: "+exchange.getSourceAddress());
			return;
		}
		
		for (Resource node : getChildren()) {
			if(node instanceof RDGroupNodeResource){
				if (((RDGroupNodeResource) node).getGroupIdentifier().equals(groupIdentifier) 
						&& ((RDGroupNodeResource) node).getDomain().equals(domain)) {
					resource = (RDGroupNodeResource) node;
					
				}
			}
		}
		
		if (resource==null) {
			
			String randomName;
			do {
				randomName = Integer.toString((int) (Math.random() * 10000));
			} while (getChild(randomName) != null);
			
			resource = new RDGroupNodeResource(groupIdentifier, domain, rdResource);
			add(resource);
			
			responseCode = ResponseCode.CREATED;
		} else {
			oldMembers = resource.getMembers();
			oldCtx = resource.getContext();
			responseCode = ResponseCode.CHANGED;
		}
		
		// set parameters of resource
		if (!resource.setParameters(exchange.advanced().getRequest())) {
			resource.delete();
			exchange.respond(ResponseCode.BAD_REQUEST);
			return;
		}
		newMembers = resource.getMembers();
		newCtx = resource.getContext();
		
		LOGGER.info("Adding new group: "+resource.getContext());

		// inform client about the location of the new resource
		exchange.setLocationPath(resource.getURI());

		// complete the request
		exchange.respond(responseCode);
		
		//Notify all the group members (old/new)
		RDGroupUtils.notifyGroupMembers(oldMembers, newMembers, oldCtx, newCtx);
	}
	

}
