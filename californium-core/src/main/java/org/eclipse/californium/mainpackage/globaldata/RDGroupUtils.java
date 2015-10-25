package org.eclipse.californium.mainpackage.globaldata;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.tools.resources.RDGroupNodeResource.GroupMemberCtx;

/**
 * Utility functions used at servers when handling a request involving Group(/rd-group) requests.
 * @author andrianeshsg
 *
 */
public class RDGroupUtils {
	
	/**
	 * 
	 * Notifies the old group members to leave the previous group and the new group members to join the new group.
	 * @param oldMembers members that are no longer in the group.
	 * @param newMembers members that are currently in the group.
	 * @param oldCtx old group context(address).
	 * @param newCtx new group context(address).
	 */
	public static void notifyGroupMembers(List<GroupMemberCtx> oldMembers, List<GroupMemberCtx> newMembers, String oldCtx, String newCtx){
		oldMembers = removeDuplicates(oldMembers);
		newMembers = removeDuplicates(newMembers);
		
		CoapClient client = new CoapClient();
		client.useNONs();
		CoAPEndpoint client_cEP = new CoAPEndpoint();
		client.setEndpoint(client_cEP);
		
		Request request = new Request(Code.GET);//allnodes 224.0.1.187 
		request.setURI("coap://224.0.1.187/");	
			
		String payload = buildPayload(oldMembers, newMembers, oldCtx, newCtx);
		System.out.println("THE_PAYLOAD["+payload+"]");
		request.setPayload(payload);
		request.setMulticast(true);
		request.setConfirmable(false);
		client.advanced(new CoapHandler() {
			
			@Override
			public void onLoad(CoapResponse response) {
				// TODO Auto-generated method stub
				System.out.println("DAFUQ A RESPONSE???");
				
			}
			
			@Override
			public void onError() {
				// TODO Auto-generated method stub
				System.out.println("DAFUQ AN ERROR???");
			}
		}, request);
	}
	
	/**
	 * Builds the payload in a way that can be understood by the receiving endpoint. Contains the old members of the group, 
	 * the old group, the new members and the new group.
	 * @param oldMembers members that must leave the oldCtx group.
	 * @param newMembers members that must join the newCtx group.
	 * @param oldCtx address of the old group.
	 * @param newCtx address of the new group.
	 * @return the payload.
	 */
	private static String buildPayload(List<GroupMemberCtx> oldMembers, List<GroupMemberCtx> newMembers, String oldCtx, String newCtx){
		StringBuilder payload = new StringBuilder("");
		for(int i = 0;i < oldMembers.size();i++){
			if(i == 0)
				payload.append(GlobalData.PAYLOAD_GROUPL_DELIMITER);
			
			payload.append(oldMembers.get(i).getAddress());
			
			if(i == oldMembers.size() - 1){
				payload.append(GlobalData.PAYLOAD_GROUPL_DELIMITER);
				if(oldCtx != null && !oldCtx.equals(""))
					payload.append(GlobalData.PAYLOAD_GROUPOC_DELIMITER+oldCtx+GlobalData.PAYLOAD_GROUPOC_DELIMITER);
			}
			else
				payload.append(";");
		}
		
		for(int i = 0;i < newMembers.size();i++){
			if(i == 0)
				payload.append(GlobalData.PAYLOAD_GROUPJ_DELIMITER);
			
			payload.append(newMembers.get(i).getAddress());
			
			if(i == newMembers.size() - 1){
				payload.append(GlobalData.PAYLOAD_GROUPJ_DELIMITER);
				if(newCtx != null && !newCtx.equals(""))
					payload.append(GlobalData.PAYLOAD_GROUPNC_DELIMITER+newCtx+GlobalData.PAYLOAD_GROUPNC_DELIMITER);
			}
			else
				payload.append(";");
		}
		
		
		return payload.toString();
	}
	
	/**
	 * Removes duplicate entries that the sourceList may contain.
	 * @param sourceList the source list.
	 * @return a list without duplicate entries.
	 */
	private static List<GroupMemberCtx> removeDuplicates(List<GroupMemberCtx> sourceList){
		if(sourceList.size() == 0)
			return new ArrayList<GroupMemberCtx>(0);
		List<GroupMemberCtx> targetList = new ArrayList<GroupMemberCtx>(0);
		for(GroupMemberCtx sourceMember : sourceList){
			boolean foundIt = false;
			for(GroupMemberCtx targetMember : targetList){
				if(targetMember.getAddress().equals(sourceMember.getAddress())){
					foundIt = true;
					break;
				}
			}
			if(!foundIt){
				targetList.add(sourceMember);
			}
		}
		
		return targetList;
	}

}
