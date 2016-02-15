package org.eclipse.californium.tools.resources;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.mainpackage.globaldata.RDGroupUtils;

/**
 * Node that holds information about a group resource.
 * @author andrianeshsg
 *
 */
public class RDGroupNodeResource extends CoapResource {

	private static final Logger LOGGER = Logger.getLogger(RDGroupNodeResource.class.getCanonicalName());
	
	private String groupIdentifier;
	private String domain;
	
	/** Members of the group. */
	private List<GroupMemberCtx> members;
	//private String endpointType;
	private String context;
	
	private RDResource rdResource = null;
	
	public RDGroupNodeResource(String groupID, String domain, RDResource rd) {
		super(groupID);		
		this.groupIdentifier = groupID;
		this.domain = domain;
		this.rdResource = rd;
		context = "";
		members = new ArrayList<GroupMemberCtx>(0);
	}

	/**
	 * Updates the endpoint parameters from POST and PUT requests.
	 *
	 * @param request A POST or PUT request with a {?con} URI Template query
	 * 			and a Link Format payload.
	 * 
	 */
	public boolean setParameters(Request request) {

		LinkAttribute attr;
		String newContext = "";

		List<String> query = request.getOptions().getUriQuery();
		for (String q : query) {
			// FIXME Do not use Link attributes for URI template variables
			attr = LinkAttribute.parse(q);
			
			if (attr.getName().equals(LinkFormat.CONTEXT)){
				newContext = attr.getValue();
			}
		}
		
		try {
			URI check;
			if (newContext.equals("")) {
				check = new URI("coap", "", request.getSource().getHostAddress(), request.getSourcePort(), "", "", ""); // required to set port
				context = check.toString().replace("@", "").replace("?", "").replace("#", ""); // URI is a silly class
			} else {
//				check = new URI(context);
				context = newContext;
//				System.out.println("To context mou einai: "+context);
			}
		} catch (Exception e) {
			LOGGER.warning(e.toString());
			return false;
		}
		
		return updateGroupResources(request.getPayloadString());
	}

//	/*
//	 * add a new resource to the node. E.g. the resource temperature or
//	 * humidity. If the path is /readings/temp, temp will be a subResource
//	 * of readings, which is a subResource of the node.
//	 */
//	public CoapResource addNodeResource(String path) {
//		Scanner scanner = new Scanner(path);
//		scanner.useDelimiter("/");
//		String next = "";
//		boolean resourceExist = false;
//		Resource resource = this; // It's the resource that represents the endpoint
//		
//		CoapResource subResource = null;
//		while (scanner.hasNext()) {
//			resourceExist = false;
//			next = scanner.next();
//			for (Resource res : resource.getChildren()) {
//				if (res.getName().equals(next)) {
//					subResource = (CoapResource) res;
//					resourceExist = true;
//				}
//			}
//			if (!resourceExist) {
////				subResource = new RDTagResource(next,true, this);
//				resource.add(subResource);
//			}
//			resource = subResource;
//		}
//		subResource.setPath(resource.getPath());
//		subResource.setName(next);
//		scanner.close();
//		return subResource;
//	}

	/*
	 * GET only debug return endpoint identifier
	 */
	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(ResponseCode.FORBIDDEN, "RD update handle");
	}
	
	/*
	 * PUTs content to this resource.
	 * andrianeshsg: After handling the request this endpoint must inform all the group members(old/new). 
	 */
	@Override
	public void handlePOST(CoapExchange exchange) {
		
		LOGGER.info("Updating group: "+getContext());
		List<GroupMemberCtx> oldMembers = members;
		String oldCtx = context;
		
		setParameters(exchange.advanced().getRequest());
		
		List<GroupMemberCtx> newMembers = members;
		String newCtx = context;
		
		// complete the request
		exchange.respond(ResponseCode.CHANGED);
		
		RDGroupUtils.notifyGroupMembers(oldMembers, newMembers, oldCtx, newCtx);
		
	}
	
	/*
	 * DELETEs this node resource
	 * andrianeshsg: After handling the request this endpoint must inform all the group members(old/new). 
	 */
	@Override
	public void handleDELETE(CoapExchange exchange) {
		delete();
		
		List<GroupMemberCtx> oldMembers = members;
		String oldCtx = context;
		
		exchange.respond(ResponseCode.DELETED);
		
		RDGroupUtils.notifyGroupMembers(oldMembers, new ArrayList<GroupMemberCtx>(0), oldCtx, "");
	}
		
	/**
	 * Creates a new subResource for each resource the node wants
	 * register. Each resource is separated by ",". E.g. A node can
	 * register a resource for reading the temperature and another one
	 * for reading the humidity.
	 */
	private boolean updateGroupResources(String linkFormat) {

		Scanner scanner = new Scanner(linkFormat);
		
		scanner.useDelimiter(",");
		List<String> pathResources = new ArrayList<String>();
		while (scanner.hasNext()) {
			pathResources.add(scanner.next());
		}
		for (String p : pathResources) {
			scanner = new Scanner(p);
//			System.out.println("Path["+p+"]");
			/*
			 * get the path of the endpoint's resource. E.g. from
			 * </readings/temp> it will select /readings/temp.
			 */
			String path = "", pathTemp = "";
			if ((pathTemp = scanner.findInLine("<.*?>")) != null) {
				path = pathTemp.substring(1, pathTemp.length() - 1);
//				System.out.println("(Close1)To path einai["+path+"]");
				if(path.equals("")){ //path is <>
					String epName = getEPName(p);
//					System.out.println("To epName einai["+epName+"]");
					String epContext = getEPAddress(epName);
					if(epContext == null){
						scanner.close();
						return false;
					}else {
//						System.out.println("To epContext einai["+epContext+"]");
						path = epContext;
					}
				}
			} else {
//				System.out.println("(Close2)To path einai["+path+"]");
				scanner.close();
				return false;			
			}
			
//			System.out.println("To path einai["+path+"]");
			scanner.useDelimiter(";");
			GroupMemberCtx memberCtx = new GroupMemberCtx();
			memberCtx.setAddress(path);
			while (scanner.hasNext()) {
				LinkAttribute attr = LinkAttribute.parse(scanner.next());
				memberCtx.addAttribute(attr);
			}
			//memberCtx.setEndpointName(getGroupIdentifier());
//			System.out.println("MEMBER: "+memberCtx.toString());
			members.add(memberCtx);
		}
		scanner.close();
		
		return true;
	}
	
	/**
	 * Returns the endpoint name from a given path. Expects {ep} argument.
	 * @param path the path to get the ep attribute from.
	 * @return the EP name, or null if it isnt included in the path.
	 */
	private String getEPName(String path){
		Scanner scanner = new Scanner(path);
		scanner.useDelimiter(";");
		if(scanner.hasNext())
			scanner.next();
		String epName = null;
		while(scanner.hasNext()){
			LinkAttribute attr = LinkAttribute.parse(scanner.next());
			if(attr.getName().equals(LinkFormat.END_POINT)){
				epName = attr.getValue();
				break;
			}
		}
		scanner.close();
		return epName;
	}
	
	/**
	 * Gets the endpoints address given a name by searching all nodes on this RD.
	 * @param epName the endpoints name.
	 * @return the address in case of success, or null otherwise. 
	 */
	private String getEPAddress(String epName){
		if(epName == null)
			return null;
		
		Collection<Resource> resources = rdResource.getChildren();
		
		Iterator<Resource>  resIt = resources.iterator();
		
		while (resIt.hasNext()){
			Resource res = resIt.next();
			if (res.getClass() == RDNodeResource.class){
				RDNodeResource node = (RDNodeResource) res;
				if (epName.equals(node.getEndpointIdentifier())){
					//FIXME This node belongs to a server. A client entity of the server must have
					//registered this node to this rd. So node.getContext() holds the address(IP:PORT) of the client 
					//that registered this node not the address that this node really listens to. Logically the IP is the
					//same but the PORT cant be the same. So when a Manager registers this node in a group as <>;ep=thisnode 
					//if we
					//save it like <IP:CLIENT_PORT> as mentioned above we will have serious problem when ie. we need to
					//tell that node to leave or join a particular group. Since the PORT we would be refering would be 
					//the ephemeral port of the client. Workaround is to always assume that <> means 
					//<IP:DEFAULT_COAP_PORT>.
					String fullAddress = node.getContext().replace("/", ""); //fullAddress = coap://ip:port
					String[] addressParts = fullAddress.split(":");
					fullAddress = addressParts[1]+":"+CoAP.DEFAULT_COAP_PORT;
					
					return fullAddress;
				}
			}
		}
		
		return null;
	}

	/*
	 * the following three methods are used to print the right string to put in
	 * the payload to respond to the GET request.
	 */
	public String toLinkFormat(List<String> query) {

		// Create new StringBuilder
		StringBuilder builder = new StringBuilder();
		
		// Build the link format
		buildLinkFormat(this, builder, query);

		// Remove last delimiter
		if (builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}

		return builder.toString();
	}

	public String toLinkFormatItem(Resource resource) {
		StringBuilder linkFormat = new StringBuilder();
		
		linkFormat.append("<"+getContext());
		linkFormat.append(resource.getURI().substring(this.getURI().length()));
		linkFormat.append(">");
		
		return linkFormat.append( LinkFormat.serializeResource(resource).toString().replaceFirst("<.+>", "") ).toString();
	}
	

	private void buildLinkFormat(Resource resource, StringBuilder builder, List<String> query) {
		if (resource.getChildren().size() > 0) {

			// Loop over all sub-resources
			for (Resource res : resource.getChildren()) {
				if (LinkFormat.matches(res, query)) {

					// Convert Resource to string representation and add
					// delimiter
					builder.append(toLinkFormatItem(res));
					builder.append(',');
				}
				// Recurse
				buildLinkFormat(res, builder, query);
			}
		}
	}
	
	
	
	/*
	 * Setter And Getter
	 */

	public String getGroupIdentifier() {
		return groupIdentifier;
	}

	public String getDomain() {
		return domain;
	}

//	public String getEndpointType() {
//		return endpointType;
//	}
//
//	public void setEndpointType(String endpointType) {
//		this.endpointType = endpointType;
//	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
	
	public List<GroupMemberCtx> getMembers() {
		return Collections.unmodifiableList(members);
	}



	public class GroupMemberCtx{
		private String address;
		private String endpointName;
		private String domain;
		private String resourceType;
		private String endpointType;
		
		public GroupMemberCtx() {
		}
		
		public GroupMemberCtx(String address, String endpointName, String domain, String resourceType, String endpointType) {
			this.address = address;
			this.endpointName = endpointName;
			this.endpointType = endpointType;
			this.domain = domain;
			this.resourceType = resourceType;
		}
		
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public String getEndpointName() {
			return endpointName;
		}
		public void setEndpointName(String endpointName) {
			this.endpointName = endpointName;
		}
		public String getDomain() {
			return domain;
		}
		public void setDomain(String domain) {
			this.domain = domain;
		}
		public String getResourceType() {
			return resourceType;
		}
		public void setResourceType(String resourceType) {
			this.resourceType = resourceType;
		}
		public String getEndpointType() {
			return endpointType;
		}
		public void setEndpointType(String endpointType) {
			this.endpointType = endpointType;
		}
		
		public void addAttribute(LinkAttribute attr){
			String attrValue = "";
			if(attr.getValue() != null)
				attrValue = attr.getValue();
//			System.out.println("VAzw attribute N["+attr.getName()+"] V["+attr.getValue()+"]");
			switch(attr.getName()){
				case LinkFormat.DOMAIN:
					domain = attrValue;
					break;
				case LinkFormat.RESOURCE_TYPE:
					resourceType = attrValue;
					break;
				case LinkFormat.END_POINT:
					endpointName = attrValue;
					break;
				case LinkFormat.END_POINT_TYPE:
					endpointType = attrValue;
					break;
				default:
					LOGGER.severe("LINKFORMAT NOT SUPPORTED!!!");
					System.exit(1);
			}
		}

		@Override
		public String toString() {

			return "Address["+address+"] Domain["+domain+"] RT["+resourceType+"] EP["+endpointName+"] ET["+endpointType+"]";
		}
		
		
	}
	
}

