package org.eclipse.californium.mainpackage.globaldata;

import java.util.Random;

public class GlobalData {
	
	/** Workaround to check if a request is multicast. We pass an option(MULTICAST_OPT, 1)
	 *  if a request is multicast or option(MULTICAST_OPT, 0) otherwise. */
	public static final int MULTICAST_OPT = 256;
	
	/** option(GROUPJOIN_OPT, GROUP_ADDRESS_TO_JOIN)*/
	public static final int GROUPJOIN_OPT = 257;
	
	/** option(GROUPLEAVE_OPT, GROUP_ADDRESS_TO_LEAVE)*/
	public static final int GROUPLEAVE_OPT = 258;
	
	/** Random generator*/
	public static final Random random = new Random(1234567890);

}
