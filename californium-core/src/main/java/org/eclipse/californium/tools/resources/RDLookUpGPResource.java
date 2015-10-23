package org.eclipse.californium.tools.resources;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.tools.resources.RDGroupNodeResource.GroupMemberCtx;

/**
 * Look up based on /gp.
 * @author andrianeshsg
 *
 */
public class RDLookUpGPResource extends CoapResource {

	private RDResource rdResource = null;
	
	public RDLookUpGPResource(String resourceIdentifier, RDResource rd) {
		super(resourceIdentifier);
		this.rdResource = rd;
	}

	
	@Override
	public void handleGET(CoapExchange exchange) {
		Collection<Resource> topResources = rdResource.getParent().getChildren();
		List<String> query = exchange.getRequestOptions().getUriQuery();
		String result = "";
		String domainQuery = "";
		String endpointQuery = "";
		String groupQuery = "";
		TreeSet<String> endpointTypeQuery = new TreeSet<String>();
		
		for (String q:query) {
			LinkAttribute attr = LinkAttribute.parse(q);
			if(attr.getName().equals(LinkFormat.DOMAIN)){
				domainQuery = attr.getValue();
			}
			if(attr.getName().equals(LinkFormat.END_POINT)){
				endpointQuery = attr.getValue();
				
			}
			if(attr.getName().equals(LinkFormat.END_POINT_TYPE)){
				Collections.addAll(endpointTypeQuery, attr.getValue().split(" "));
			}
			
			if(attr.getName().equals(LinkFormat.GROUP)){
				groupQuery = attr.getValue();
			}
		}
		
		Iterator<Resource>  topResIt = topResources.iterator();

		while (topResIt.hasNext()){
			Resource topRes = topResIt.next();
			Collection<Resource> resources = topRes.getChildren();
			Iterator<Resource>  resIt = resources.iterator();
			while(resIt.hasNext()){
				Resource res = resIt.next();
//				System.out.println("Chekarw to resource me name["+res.getName()+"]. Psaxnw group["+groupQuery+"]");
				if (res.getClass() == RDGroupNodeResource.class){
//					System.out.println("HEY!");
					RDGroupNodeResource node = (RDGroupNodeResource) res;
					List<GroupMemberCtx> groupMembers = node.getMembers();
					for(GroupMemberCtx member : groupMembers){
						if((domainQuery.isEmpty() || domainQuery.equals(member.getDomain())) && 
								(endpointQuery.isEmpty() || endpointQuery.equals(member.getEndpointName())) &&
								(endpointTypeQuery.isEmpty() || endpointTypeQuery.contains(member.getEndpointType())) &&
								(groupQuery.isEmpty() || groupQuery.equals(node.getGroupIdentifier())) ){
							
							result += "<"+node.getURI()+">;"+LinkFormat.GROUP+"=\""+node.getGroupIdentifier()+"\"";
							result += ";"+LinkFormat.DOMAIN+"=\""+node.getDomain()+"\"";
							if(member.getEndpointType() != null && !member.getEndpointType().isEmpty()){
								result += ";"+LinkFormat.RESOURCE_TYPE+"=\""+member.getEndpointType()+"\"";
							}
							if(endpointQuery.equals(member.getEndpointName()))
								result += ";"+LinkFormat.END_POINT+"=\""+member.getEndpointName()+"\"" + ",";
							else{
								result += ",";
								break;
							}
						}
						
						
					}
				}
			}
		}
		if(result.isEmpty()){
			exchange.respond(ResponseCode.NOT_FOUND);
		}
		else{
			exchange.respond(ResponseCode.CONTENT, result.substring(0,result.length()-1), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
		}
		
	}
}