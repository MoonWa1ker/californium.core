/*******************************************************************************
 * Copyright (c) 2015 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 ******************************************************************************/
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


public class RDLookUpEPResource extends CoapResource {

	private RDResource rdResource = null;
	
	public RDLookUpEPResource(String resourceIdentifier, RDResource rd) {
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
		//andrianeshsg: Alterations to support lookup with gp argument
		// ( rd-lookup/ep?gp=mygroup )
		while (topResIt.hasNext()){
			Resource topRes = topResIt.next();
			Collection<Resource> resources = topRes.getChildren();
			Iterator<Resource>  resIt = resources.iterator();
			while(resIt.hasNext()){
				Resource res = resIt.next();
				if (groupQuery.isEmpty() && res.getClass() == RDNodeResource.class){
					RDNodeResource node = (RDNodeResource) res;
					if ( (domainQuery.isEmpty() || domainQuery.equals(node.getDomain())) && 
						 (endpointQuery.isEmpty() || endpointQuery.equals(node.getEndpointIdentifier())) &&
						 (endpointTypeQuery.isEmpty() || endpointTypeQuery.contains(node.getEndpointType()))) {
					
						result += "<"+node.getContext()+">;"+LinkFormat.END_POINT+"=\""+node.getEndpointIdentifier()+"\"";
						result += ";"+LinkFormat.DOMAIN+"=\""+node.getDomain()+"\"";
						if(node.getEndpointType() != null && !node.getEndpointType().isEmpty()){
							result += ";"+LinkFormat.RESOURCE_TYPE+"=\""+node.getEndpointType()+"\"";
						}
								
						result += ",";
					}
				}else if (res.getClass() == RDGroupNodeResource.class){
					RDGroupNodeResource node = (RDGroupNodeResource) res;
					List<GroupMemberCtx> groupMembers = node.getMembers();
					for(GroupMemberCtx member : groupMembers){
						if((domainQuery.isEmpty() || domainQuery.equals(member.getDomain())) && 
								(endpointQuery.isEmpty() || endpointQuery.equals(member.getEndpointName())) &&
								(endpointTypeQuery.isEmpty() || endpointTypeQuery.contains(member.getEndpointType())) &&
								(groupQuery.isEmpty() || groupQuery.equals(node.getGroupIdentifier())) ){
							
							result += "<"+node.getContext()+">;"+LinkFormat.END_POINT+"=\""+member.getEndpointName()+"\"";
							result += ";"+LinkFormat.DOMAIN+"=\""+node.getDomain()+"\"";
							result += ";"+LinkFormat.GROUP+"=\""+node.getGroupIdentifier()+"\"";
							if(member.getEndpointType() != null && !member.getEndpointType().isEmpty()){
								result += ";"+LinkFormat.RESOURCE_TYPE+"=\""+member.getEndpointType()+"\"";
							}
									
							result += ",";
							
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
