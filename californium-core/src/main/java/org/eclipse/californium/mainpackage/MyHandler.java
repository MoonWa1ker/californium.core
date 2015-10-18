package org.eclipse.californium.mainpackage;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.network.Exchange;

public class MyHandler implements CoapHandler{

	private Thread t;
	private long timeout;
//	private int i;
	
	public MyHandler(long timeout) {
		t = null;
		this.timeout = timeout;
		
		MyRunnable myR = new MyRunnable(null, timeout);
		t = new Thread(myR, "Initial Response-Timeout Thread");
		t.start();
	}
	
	@Override
	public void onLoad(CoapResponse response) {
		// TODO Auto-generated method stub
		System.out.println("RECEIVED A RESPONSE!!!! SourceAddr: "+response.advanced().getSource());
		System.out.println(Utils.prettyPrint(response));
		if(t != null)
			t.interrupt();
		
		MyRunnable myR = new MyRunnable(response.advanced().getExchange(), timeout);
		t = new Thread(myR, "Response-Timeout Thread");
		t.start();
		
	}

	@Override
	public void onError() {
		// TODO Auto-generated method stub
		System.out.println("TIMEOUT OR REJECTED BY THE SERVER!");
	}
	
	
	private class MyRunnable implements Runnable{

		private Exchange exchange;
		private long timeout;
		
		public MyRunnable(Exchange exchange, long timeout){
			this.exchange = exchange;
			this.timeout = timeout;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				System.out.println(Thread.currentThread().getName() + ": Sleeping...");
				Thread.sleep(timeout);
				System.out.println(Thread.currentThread().getName() + ": Woke up normally, completing the exchange!");
				if(exchange != null)
					exchange.setComplete();
			} catch (InterruptedException e) {
				System.out.println(Thread.currentThread().getName() + ": Woke up unexpectedly..");
			}
			System.out.println(Thread.currentThread().getName() + ": Returing...");
		}
		
	}
	
	
}
