package com.utd.acn.project;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IoTNode extends Node{
	
	private int interval;
	private ArrayList<FogNode> neighborFogNodes = new ArrayList<FogNode>();
	
	private IoTNode(String ipAddress, int tcpPort, int udpPort, int interval, ArrayList<FogNode> fogNodeList) {
		super(ipAddress, tcpPort, udpPort);
		this.interval = interval;
		this.neighborFogNodes = fogNodeList;
		
		//start listener to listen on any responses from fog/cloud nodes.
		//udpSocket = new DatagramSocket(udpPort);
		listenForResponse();
		
		//scheduler to keep generating requests periodically.
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(); 
		scheduler.scheduleAtFixedRate(new RequestsScheduler(this), 5, interval, TimeUnit.SECONDS);
	}

	public IoTNode(String ipAddress, int tcpPort, int udpPort) {
		super(ipAddress, tcpPort, udpPort);
	}
	
	public Request prepareRequest() {
		int randomIndex = getRandomIntegerBetweenRange(0, neighborFogNodes.size()-1);
		FogNode destinationNode = getNeighborFogNodes().get(randomIndex);
		//TO-DO: generate seq no.
		RequestHeader header = new RequestHeader(getIpAddress(), getUdpPort(), destinationNode.getIpAddress(), destinationNode.getUdpPort(), 
				"UDP", getRandomIntegerBetweenRange(0,Integer.MAX_VALUE), 2, 3);
		Request request = new Request(header, "["+header.getSequenceNumber()+"]IoT NODE:"+ getIpAddress()+ ":"+ getUdpPort()+": Request has been generated and being sent to "+destinationNode.getIpAddress()+": "+destinationNode.getUdpPort());
		return request;
	}
	
	public void sendRequest() {
		Request request = prepareRequest();
		send(request, request.getHeader().getDestinationIP(), request.getHeader().getDestinationPort(), "UDP");
//		try{
//			DatagramSocket socket = new DatagramSocket();
//			InetAddress IPAddress = InetAddress.getByName(request.getHeader().getDestinationIP());
//			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//			ObjectOutputStream os = new ObjectOutputStream(outputStream);
//			os.writeObject(request);
//			byte[] data = outputStream.toByteArray();
//			DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, request.getHeader().getDestinationPort());
//			socket.send(sendPacket);
//			//System.out.println("Request sent to: "+request.getHeader().getDestinationPort());
//			os.close();
//			socket.close();
//		}catch(Exception e){
//			System.out.println(e);
//		}
	}
	
	public void listenForResponse() {
		IoTNodeListener listener = new IoTNodeListener(this);
		listener.start();
	}

	public void printResponse(Response response) {
		response.appendAuditTrail("["+response.getHeader().getSequenceNumber()+"]IoT NODE:"+ getIpAddress()+ ":"+ getUdpPort()+": Response has been received.");
		System.out.println(response.getAuditTrail());
	}
	
	public static void main(String args[]) {
		//java IoTNode interval MY_UDP IP1 UDP1 IP2 UDP2
		//String cmd = "3 9882 127.0.0.1 9876 127.0.0.1 9879";
		//args = cmd.split(" ");
		
		IoTNode iotNode = null;
		int interval;
		int udpPort;
		if(args.length > 0) {
			interval = Integer.parseInt(args[0]);
			udpPort = Integer.parseInt(args[1]);
			ArrayList<FogNode> tempFogNodeList = new ArrayList<FogNode>();
			for(int i=2; i<args.length;i++) {
				FogNode n = new FogNode(args[i], 0, Integer.parseInt(args[++i]));
				tempFogNodeList.add(n);
			}
			iotNode = new IoTNode("127.0.0.1", 0, udpPort, interval, tempFogNodeList);
		} else {
			System.out.println("Improper arguments passed!");
		}
	}
	
	public static int getRandomIntegerBetweenRange(double min, double max){
	    double x = (int)(Math.random()*((max-min)+1))+min;
	    return (int) x;
	}
	
	public ArrayList<FogNode> getNeighborFogNodes() {
		return neighborFogNodes;
	}

	public void setNeighborFogNodes(ArrayList<FogNode> neighborFogNodes) {
		this.neighborFogNodes = neighborFogNodes;
	}
	
	public void addNeighborFogNode(FogNode neighborFogNode) {
		this.neighborFogNodes.add(neighborFogNode);
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
}