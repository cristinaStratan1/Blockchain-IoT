package entity;

import java.security.SecureRandom;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import impl.CCMImpl;

public class IoT implements MqttCallback {

	public int id;
	static final String Km = "SymmKeyLongTerm";
	static String Kiks = "communicationInitValue";
	static String[] RandFirmSeqsiPlus1;
	MqttClient IoTclient;
	static String iv = "0";
	static String encryptedNonce1;
	static String nonce1 = secureRandom();
	static CCMImpl ccm;
	static String gatewayID;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	// publisher
	public void sendAuthetication() throws Exception {
		System.out.println("Sending Authentication ....");
		gatewayID=null;
		IoTclient = new MqttClient("tcp://broker.emqx.io:1883", "authenticate");
		IoTclient.connect();
		SecureRandom random = new SecureRandom();
		String s2;
		do {
			nonce1 = "";
			for (int i = 0; i < 13; i++) {
				char randomizedCharacter = (char) (random.nextInt(26) + 'a');
				nonce1 += randomizedCharacter;
			}
			encryptedNonce1 = ccm.ccmaes(true, nonce1, Km, iv);
			s2 = ccm.ccmaes(false, encryptedNonce1, Km, iv);
		} while (s2.equals("mac check in CCM failed"));
		String authRequest= "auth_request "+ Integer.toString(getId())+ " initialization " + encryptedNonce1;
		System.out.println(authRequest);
		String MAC= ccm.encode(Kiks, authRequest);
		MqttMessage message = new MqttMessage();
		message.setPayload((authRequest+" "+MAC).getBytes());
		IoTclient.publish("IoTDevice", message);
		System.out.println("Authentication request sent");
		listenEdgeDevice();
	}

	// subscriber
	public void listenEdgeDevice() throws MqttException {
		IoTclient.setCallback(this);
		IoTclient.subscribe("EdgeDevice");
	}

	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		System.out.println("Connection lost!");

	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("IoT device received: " + message);


		CCMImpl ccm = new CCMImpl();
		String s = message.toString();
		String[] arr=s.split(",");
		String[] arr2;
		if (arr.length>1) {
			arr2=arr[2].split(" ");
		}
		else
		{
			arr2=s.split(" ");
		}
		if (gatewayID==null){
			if( ccm.ccmaes(false,arr[1]+arr[3], Km,"0").equals(arr[arr.length-1]))
			{
				System.out.println("MAC check failed");
			}
			RandFirmSeqsiPlus1=new String[arr2.length];
			for (int i=0;i<arr2.length;i++)
			{

//				System.out.println("Saving " +ccm.ccmaes(false, arr2[i], Km,"0"));
				RandFirmSeqsiPlus1[i]= ccm.ccmaes(false, arr2[i], Km,"0");
			}
			System.out.println("Random sequence numbers saved");
//	        MqttClient client=new MqttClient("tcp://broker.emqx.io:1883", "senddeddd");
//			System.out.println("nonce1: "+arr[3]);
//			System.out.println("nonce2: "+arr[1]);
			gatewayID=arr[0];
			String nonce2 =ccm.ccmaes(false,arr[3], Km, iv);
			String nonce1 =ccm.ccmaes(false,arr[1], Km, iv);
			String encryptedMessage = ccm.ccmaes(true, nonce2, Km, iv) +" ";
			for (int i = 0; i < RandFirmSeqsiPlus1.length; i++) {
				encryptedMessage += ccm.ccmaes(true, RandFirmSeqsiPlus1[i], Km, iv) + ",";
			}
			encryptedMessage+=" ";
			String message3MAC= ccm.encode(encryptedMessage, Kiks);
			System.out.println("IoT device:"+getId() +" Sending auth request 2");
			MqttMessage msg1 = new MqttMessage();
			String reply="auth_request2 IoTid:"+ getId() +" "+encryptedMessage+" "+ message3MAC;
            msg1.setPayload(reply.getBytes());
            IoTclient.publish("IoTDevice",msg1);
    		listenEdgeDevice();
		}
		else if (arr2[0].equals(gatewayID) && ccm.ccmaes(false, arr2[1], Km, iv) .equals("Ack"))
		{
			System.out.println("SUCCESSFUL authentication");
		}
		else
		{
			System.out.println("FAILED to authenticate");
		}
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		System.out.println("Message delivered! from IoT");

	}
	static String secureRandom() {
		SecureRandom random = new SecureRandom();
		int max = 999999;
		int min = 100000;
		String number = "";
		for (int i = 0; i < 13; i++) {
			char randomizedCharacter = (char) (random.nextInt(26) + 'a');
			number += randomizedCharacter;
		}
		return number;
	}

}
