package com.utd.acn.project;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class Node {
	private String ipAddress;
	private int tcpPort;
	private int udpPort;
	
	public Node(String ipAddress, int tcpPort, int udpPort) {
		this.ipAddress = ipAddress;
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public int getUdpPort() {
		return udpPort;
	}
	public void setUdpPort(int udpPort) {
		this.udpPort = udpPort;
	}
	public int getTcpPort() {
		return tcpPort;
	}
	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}
	
	public void appendAuditInfo(Request request, String audit) {
		request.appendAuditTrail(audit);
	}
	
	public void send(Object object, String destIP, int destPort, String protocol) {
		if(protocol.equalsIgnoreCase("TCP")) {
			try {
				Socket s = new Socket(destIP, destPort);
				OutputStream os = s.getOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(os);
				oos.writeObject(object);
				oos.close();
				os.close();
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if(protocol.equalsIgnoreCase("UDP")) {
			try{
				DatagramSocket socket = new DatagramSocket();
				InetAddress IPAddress = InetAddress.getByName(destIP);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				ObjectOutputStream os = new ObjectOutputStream(outputStream);
				os.writeObject(object);
				byte[] data = outputStream.toByteArray();
				DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, destPort);
				socket.send(sendPacket);
				os.close();
				socket.close();
			}catch(Exception e){
				System.out.println(e);
			}
		}
	}
}
