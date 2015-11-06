package org.eclipse.californium.core.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.RawData;
import org.eclipse.californium.elements.RawDataChannel;
import org.eclipse.californium.elements.UDPConnector;


public class MyUDPConnector implements Connector{

	public final static Logger LOGGER = Logger.getLogger(MyUDPConnector.class.toString());
	
	public static final int UNDEFINED = 0;
	
	private boolean running;
	
	private MulticastSocket socket;
	
	private final InetSocketAddress localAddr;
	
	private List<Thread> receiverThreads;
	private List<Thread> senderThreads;

	/** The outbound message queue. */
	private final BlockingQueue<RawData> outgoing;
	
	/** The receiver of incoming messages. */
	private RawDataChannel receiver;
	
	private int receiveBufferSize = UNDEFINED;
	private int sendBufferSize = UNDEFINED;
	
	private int senderCount = 1;
	private int receiverCount = 1;
	
	private int receiverPacketSize = 2048;
	private boolean logPackets = false;
	private Set<String> groupSet;
	
	/**
	 * Creates a connector on the wildcard address listening on an
	 * ephemeral port, i.e. a port chosen by the system.
	 * 
	 * The effect of this constructor is the same as invoking
	 * <code>UDPConnector(null)</code>.
	 */
	public MyUDPConnector() {
		this(null);
	}
	
	/**
	 * Creates a connector bound to a given IP address and port.
	 *  
	 * @param address the IP address and port, if <code>null</code>
	 * the connector is bound to an ephemeral port on the wildcard address
	 */
	public MyUDPConnector(InetSocketAddress address) {
		//System.out.println("MyUDPConnector!!");
		groupSet = new HashSet<String>(0);
		if (address == null) {
			this.localAddr = new InetSocketAddress(0);
		} else {
			this.localAddr = address;
		}
		this.running = false;
		
		//TODO: think about restricting the outbound queue's capacity
		this.outgoing = new LinkedBlockingQueue<RawData>();
	}
	
	public void joinGroup(String group) throws IOException{
		if(!groupSet.contains(group)){
			socket.joinGroup(InetAddress.getByName(group));
			groupSet.add(group);
		}
		
	}
	
	public void leaveGroup(String group) throws IOException{
		if(groupSet.contains(group)){
			socket.leaveGroup(InetAddress.getByName(group));
			groupSet.remove(group);
		}
	}
	
	@Override
	public synchronized void start() throws IOException {
		if (running) return;
		
		// if localAddr is null or port is 0, the system decides
		socket = new MulticastSocket(localAddr.getPort());//new DatagramSocket(localAddr.getPort(), localAddr.getAddress());

		joinGroup("224.0.1.187");	// All CoAP Nodes group
		 
		this.running = true;
		
		if (receiveBufferSize != UNDEFINED) {
			socket.setReceiveBufferSize(receiveBufferSize);
		}
		receiveBufferSize = socket.getReceiveBufferSize();
		
		if (sendBufferSize != UNDEFINED) {
			socket.setSendBufferSize(sendBufferSize);
		}
		sendBufferSize = socket.getSendBufferSize();
		
		// start receiver and sender threads
		LOGGER.log(Level.CONFIG, "UDPConnector starts up {0} sender threads and {1} receiver threads",
				new Object[]{senderCount, receiverCount});
		
		receiverThreads = new LinkedList<Thread>();
		for (int i=0;i<receiverCount;i++) {
			receiverThreads.add(new Receiver("UDP-Receiver-"+localAddr+"["+i+"]"));
		}
		
		senderThreads = new LinkedList<Thread>();
		for (int i=0;i<senderCount;i++) {
			senderThreads.add(new Sender("UDP-Sender-"+localAddr+"["+i+"]"));
		}

		for (Thread t:receiverThreads)
			t.start();
		for (Thread t:senderThreads)
			t.start();
		
		/*
		 * Java bug: sometimes, socket.getReceiveBufferSize() and
		 * socket.setSendBufferSize() block forever when called here. When
		 * called up there, it seems to work. This issue occurred in Java
		 * 1.7.0_09, Windows 7.
		 */
		
		String startupMsg = new StringBuffer("UDPConnector listening on ")
			.append(socket.getLocalSocketAddress()).append(", recv buf = ")
			.append(receiveBufferSize).append(", send buf = ").append(sendBufferSize)
			.append(", recv packet size = ").append(receiverPacketSize).toString();
		LOGGER.log(Level.CONFIG, startupMsg);
	}

	@Override
	public synchronized void stop() {
		if (!running) return;
		this.running = false;
		// stop all threads
		if (senderThreads!= null)
			for (Thread t:senderThreads) {
				t.interrupt();
			}
		if (receiverThreads!= null)
			for (Thread t:receiverThreads) {
				t.interrupt();
			}
		outgoing.clear();
		String address = socket.getLocalSocketAddress().toString();
		groupSet.clear();
		if (socket != null)
			socket.close();
		socket = null;
		LOGGER.log(Level.CONFIG, "UDPConnector on [{0}] has stopped.", address);
	}

	@Override
	public synchronized void destroy() {
		stop();
	}
	
	@Override
	public void send(RawData msg) {
		if (msg == null)
			throw new NullPointerException();
		outgoing.add(msg);
	}

	@Override
	public void setRawDataReceiver(RawDataChannel receiver) {
		this.receiver = receiver;
	}
	
	public InetSocketAddress getAddress() {
		if (socket == null) return localAddr;
		else return new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort());
	}
	
	private abstract class Worker extends Thread {

		/**
		 * Instantiates a new worker.
		 *
		 * @param name the name
		 */
		private Worker(String name) {
			super(name);
			setDaemon(true);
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			LOGGER.log(Level.FINE, "Starting worker [{0}]", getName());
			while (running) {
				try {
					work();
				} catch (Throwable t) {
					if (running)
						LOGGER.log(Level.WARNING, "Exception occurred in Worker [" + getName() + "] (running="
								+ running + "): ", t);
					else
						LOGGER.log(Level.FINE, "Worker [{0}] has been stopped successfully", getName());
				}
			}
		}

		/**
		 * @throws Exception the exception to be properly logged
		 */
		protected abstract void work() throws Exception;
	}
	
	private class Receiver extends Worker {
		
		private DatagramPacket datagram;
		private int size;
		
		private Receiver(String name) {
			super(name);
			this.size = receiverPacketSize;
			this.datagram = new DatagramPacket(new byte[size], size);
		}
		
		protected void work() throws IOException {
			datagram.setLength(size);
			socket.receive(datagram);
			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER.log(Level.FINER, "UDPConnector ({0}) received {1} bytes from {2}:{3}",
						new Object[]{socket.getLocalSocketAddress(), datagram.getLength(),
							datagram.getAddress(), datagram.getPort()});
			}
			byte[] bytes = Arrays.copyOfRange(datagram.getData(), datagram.getOffset(), datagram.getLength());
			RawData msg = new RawData(bytes, datagram.getAddress(), datagram.getPort());
			
			receiver.receiveData(msg);
		}
		
	}
	
	private class Sender extends Worker {
		
		private DatagramPacket datagram;
		
		private Sender(String name) {
			super(name);
			this.datagram = new DatagramPacket(new byte[0], 0);
		}
		
		protected void work() throws InterruptedException, IOException {
			RawData raw = outgoing.take(); // Blocking
			datagram.setData(raw.getBytes());
			datagram.setAddress(raw.getAddress());
			datagram.setPort(raw.getPort());
			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER.log(Level.FINER, "UDPConnector ({0}) sends {1} bytes to {2}:{3}",
						new Object[]{socket.getLocalSocketAddress(), datagram.getLength(),
							datagram.getAddress(), datagram.getPort()});
			}
			socket.send(datagram);
		}
	}
	
	public void setReceiveBufferSize(int size) {
		this.receiveBufferSize = size;
	}
	
	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}
	
	public void setSendBufferSize(int size) {
		this.sendBufferSize = size;
	}
	
	public int getSendBufferSize() {
		return sendBufferSize;
	}
	
	public void setReceiverThreadCount(int count) {
		this.receiverCount = count;
	}
	
	public int getReceiverThreadCount() {
		return receiverCount;
	}
	
	public void setSenderThreadCount(int count) {
		this.senderCount = count;
	}
	
	public int getSenderThreadCount() {
		return senderCount;
	}
	
	public void setReceiverPacketSize(int size) {
		this.receiverPacketSize = size;
	}
	
	public int getReceiverPacketSize() {
		return receiverPacketSize;
	}
	
	/**
	 * Sets whether sent and received datagram packets should be logged.
	 * 
	 * @param b <code>true</code> if packets should be logged
	 * @deprecated Packets sent and received are always logged at {@link Level#FINER}
	 */
	public void setLogPackets(boolean b) {
		this.logPackets = b;
	}
	
	/**
	 * Checks whether sent and received datagram packets are logged.
	 * 
	 * @return <code>true</code> if packets are logged
	 * @deprecated Packets sent and received are always logged at {@link Level#FINER}
	 */
	public boolean isLogPackets() {
		return logPackets;
	}
}
