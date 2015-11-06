package org.eclipse.californium.mainpackage.globaldata;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionSet;
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
//		client.useNONs();
		CoAPEndpoint client_cEP = new CoAPEndpoint();
		client.setEndpoint(client_cEP);
		
		
		// Notify Old Members
		for(GroupMemberCtx m : oldMembers){
			Request request = new Request(Code.POST);//allnodes 224.0.1.187 
			request.setURI("coap://"+m.getAddress());
			OptionSet options = request.getOptions();
			options.addOption(new Option(GlobalData.GROUPLEAVE_OPT, oldCtx));
			request.setOptions(options);
			client.advanced(new CoapHandler() {
				
				@Override
				public void onLoad(CoapResponse response) {
					// TODO Auto-generated method stub
					System.out.println("GROUP LEAVE SUCCESS!");
					
				}
				
				@Override
				public void onError() {
					// TODO Auto-generated method stub
					System.out.println("GROUP LEAVE ERROR(??)");
				}
			}, request);
			
			
		}
		
		for(GroupMemberCtx m : newMembers){
			Request request = new Request(Code.POST);//allnodes 224.0.1.187 
			System.out.println("ADDRESZZ["+m.getAddress()+"]");
			request.setURI("coap://"+m.getAddress());
			OptionSet options = request.getOptions();
			options.addOption(new Option(GlobalData.GROUPJOIN_OPT, newCtx));
			request.setOptions(options);
			client.advanced(new CoapHandler() {
				
				@Override
				public void onLoad(CoapResponse response) {
					// TODO Auto-generated method stub
					System.out.println("GROUP JOIN SUCCESS!");
				}
				
				@Override
				public void onError() {
					// TODO Auto-generated method stub
					System.out.println("GROUP JOIN ERROR(??)");
				}
			}, request);
		}
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
