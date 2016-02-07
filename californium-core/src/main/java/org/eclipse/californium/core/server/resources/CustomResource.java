package org.eclipse.californium.core.server.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.mainpackage.globaldata.GlobalData;

public class CustomResource extends CoapResource{
	
	private volatile int resourceVal;
	private int VALCHANGINGDELAY = 3000;
	private String epName;
	
	public CustomResource(String name, int resourceVal, String epName) {
		super(name);
		//this.resourceVal = resourceVal;
		this.epName = epName;
		
		if(name.equals(GlobalData.res[0])) 
			getAttributes().addResourceType("temperature-c");
		else if(name.equals(GlobalData.res[1]))
			getAttributes().addResourceType("light-lux");
		else 
			getAttributes().addResourceType("pressure-atm");

		
		getAttributes().addContentType(41);
		getAttributes().addInterfaceDescription("sensor");
		
		scheduleResChanging(VALCHANGINGDELAY);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
//		System.out.println("Thelw na apadisw ston "+exchange.getSourceAddress().getHostAddress()+":"
//				+ exchange.getSourcePort()+" me payload: ["+exchange.getRequestText()+"] !!! 1");
//		int i = GlobalData.random.nextInt(2000) + 1;
//		if(i <= 1000)
//			resourceVal++;
		exchange.respond(ResponseCode.CONTENT, String.valueOf(resourceVal)+";"+epName);
	}
	
	public void scheduleResChanging(final int interval){
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(interval);
					changeRes();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
	
	public void changeRes(){
		if(epName.equals("1sensor2") || epName.equals("1sensor5"))
			resourceVal = 40;
		this.changed();
		scheduleResChanging(VALCHANGINGDELAY);
	}

}
