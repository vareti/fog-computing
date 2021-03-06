package com.utd.acn.project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class FogNodeUDPListener extends Thread { 
    private DatagramSocket udpSocket; 
    private final FogNode fogNode;
    
	public FogNodeUDPListener(FogNode fogNode) {
		this.fogNode = fogNode;
		try {
			udpSocket =  new DatagramSocket(fogNode.getUdpPort());
		} catch (SocketException e) {
			System.out.println("Error creating server socket: "+ e.getMessage());
		}
	}

	@Override
    public void run()  
    { 
        while (true)  
        { 
            try { 
                // receive response from fog/cloud node. 
            	byte[] buffer = new byte[1024];
            	DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            	udpSocket.receive(response);
            	byte[] data = response.getData();
            	ByteArrayInputStream baos = new ByteArrayInputStream(data);
                ObjectInputStream oos = new ObjectInputStream(baos);
                Request requestObj = (Request)oos.readObject();
                fogNode.processRequest(requestObj);
            } catch (IOException | ClassNotFoundException e) { 
            	udpSocket.close(); 
            	System.out.println("Error reading the request at fog node:" + fogNode.getIpAddress()); 
            } 
        }           
    } 
}
