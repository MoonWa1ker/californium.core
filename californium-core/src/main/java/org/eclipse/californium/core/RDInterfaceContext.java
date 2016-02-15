package org.eclipse.californium.core;

/**
 * Context of a resource directory.
 * @author andrianeshsg
 *
 */
public class RDInterfaceContext {

	/** IP address of the resource directory. */
	private String address;
	
	/** Port of the resource directory (often 5683). */
	private int port;
	
	/** Path to the RD Function Set. */
	private String rdPath;
	
	/** Path to the RD Lookup Function Set. */
	private String rdLookupPath;
	
	/** Path to the RD Group Function Set. */
	private String rdGroupPath;
	
	public RDInterfaceContext(String address, int port) {
		this.address = address;
		this.port = port;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getRdPath() {
		return rdPath;
	}

	public void setRdPath(String rdPath) {
		this.rdPath = rdPath;
	}

	public String getRdLookupPath() {
		return rdLookupPath;
	}

	public void setRdLookupPath(String rdLookupPath) {
		this.rdLookupPath = rdLookupPath;
	}

	public String getRdGroupPath() {
		return rdGroupPath;
	}

	public void setRdGroupPath(String rdGroupPath) {
		this.rdGroupPath = rdGroupPath;
	}

	@Override
	public String toString() {
		
		return "DestAddr["+address+"]" + " DestPort["+port+"] " + "RD["+rdPath+"]" + " LOOKUP["+rdLookupPath+"]" + " GROUP["+rdGroupPath+"]";
	}
	
	
	
	
}
