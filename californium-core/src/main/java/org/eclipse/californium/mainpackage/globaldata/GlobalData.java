package org.eclipse.californium.mainpackage.globaldata;

public class GlobalData {
	
	/** Multicast delimiter used so the receiver can tell if a message was multicast.  */
	public static final String PAYLOAD_MCAST_DELIMITER = "#McAsT#";
	
	/** Group Join delimiter. Addresses inside this delimiter are instructed to join a group. */
	public static final String PAYLOAD_GROUPJ_DELIMITER = "#GpJ#";
	
	/** Group Leave delimiter. Addresses inside this delimiter are instructed to leave a group. */
	public static final String PAYLOAD_GROUPL_DELIMITER = "#GpL#";
	
	/** Group New Context. Context(mcast address) of the group some endpoints must join. */
	public static final String PAYLOAD_GROUPNC_DELIMITER = "#GpNc#";
	
	/** Group Old Context. Context(mcast address) of the group some endpoints must leave. */
	public static final String PAYLOAD_GROUPOC_DELIMITER = "#GpOc#";

}
