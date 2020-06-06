package iot.unipi.cloudapp;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;

public class Server {

	private static ServerSocket serverSocket = null;
	private static Socket socket = null;
	private static JSONParser parser = new JSONParser();

	static{
		try{
            		serverSocket = new ServerSocket(5000);
	    	}
	    	catch(IOException ex){
	        	ex.printStackTrace();
	    	}
	}
	
	private static void send(Socket socket, JSONObject obj){
		try{
			BufferedWriter bwrite = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		    	String toSend = obj.toJSONString();
		    	bwrite.write(toSend);
		    	bwrite.newLine();
		    	bwrite.flush();
		}
		catch(IOException ex){
		    System.err.println("Client disconnected");
		    //ex.printStackTrace();
		}
	}

	private static JSONObject receive(Socket socket){
		try{
		    	BufferedReader bread = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		    	String server_ans = bread.readLine();
		    	return (JSONObject) parser.parse(server_ans);
		}
		catch(IOException ex){
		    	System.err.println("Client disconnected");
		    	//ex.printStackTrace();
		}
		catch(ParseException ex) {
          		System.err.println("Client disconnected");
        	}
        	return null;
    	}

	public static void main(String[] args) {
	    	//Server cicle for accepting new connections on the socket
	    	while(true){
	        	try{
	            		Socket sock = serverSocket.accept();
	            		//DA IMPEMENTARE CODICE PER LA REGISTRAZIONE DEL MOTE
	            		messageHandler(sock);
	        	}
			catch(IOException ex){
			    	ex.printStackTrace();
			}
	    	}
	}

	private static void messageHandler(Socket socket) {
	    	JSONObject jobj = receive(socket);

        	if (jobj.containsKey("MoteInfo")){
        		JSONObject moteInfo = (JSONObject) jobj.get("MoteInfo");
	        	String moteType = (String) moteInfo.get("MoteType");
	        	String moteResource = (String) moteInfo.get("MoteResource");
	        	coapClient(socket.getInetAddress(), moteResource);
        	}
        	//send(socket, JSON PER DIRE AL SENSORE A CHE STANZA/PRESA è STATO ASSEGNATO)
  	}
	
	public static void coapClient(InetAddress inetAddress, String resource) {
		String ip = inetAddress.toString();
	    	CoapClient client = new CoapClient("coap://" + ip + "/" + resource);
	    	CoapObserveRelation relation = client.observe(
	        	new CoapHandler() {
	        
		        	public void onLoad(CoapResponse response) {
		        		String content = response.getResponseText();
		            		System.out.println(content);
		            		//SALVARE IL VALORE DEL SENSORE IN UN ARRAY
	        		}
				public void onError() {
		            		System.err.println("Failed");
		        	}
	        	}
        	);
	}
}
