package entity;

import java.security.SecureRandom;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import impl.CCMImpl;

public class Gateway implements MqttCallback {

	public int id;
//	 a shared long-term symmetric master key, called Km; 
	static final String Km = "SymmKeyLongTerm";
	MqttClient thisClient;
	//	and a second, shared short-term symmetric session key, called Ks
//	The specific initial value of Ks, that is added manually is denoted by Kiks.
	static String Kiks = "communicationInitValue";

	static String nonce1 = secureRandom();

	static String IDg = "01";
	static String nonce2 = secureRandom();
	static String iv = "0";
	static CCMImpl ccm;
	static String encryptedNonce1;
//	 Random set of sequence numbers of session (i - 1) frames to be used in session i’s session key update
//	static String RandFirmSeqsi = "randfirm";
	static String[] RandFirmSeqsi;

//	 Random set of sequence numbers of session i frames to be used in session (i + 1)’s session key update
	static String[] RandFirmSeqsiPlus1;
	static String RandomSequence = "";

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	// periodically check latest firmware update from blockhain
	public void checkUpdate() {

	}

	// if there is new update
	// request update from blockchain with preshared key
	public void sendAuthToBlockchain() {

	}

	// get latest firmware update
	public void getUpdate() {

	}

	// subscriber
	public void listenIoTDevices() throws MqttException {
		thisClient.setCallback(this);
		thisClient.subscribe("IoTDevice");
	}

	public void listentoFirmwareUpdate() throws MqttException {
		thisClient = new MqttClient("tcp://broker.emqx.io:1883", "firmware_reciever");
		thisClient.connect();
		thisClient.setCallback(this);
		thisClient.subscribe("firmwareUpdate");
	}

	// publisher
	public void sendAuthetication(MqttClient client, String idEdge) throws MqttPersistenceException, MqttException {
		MqttMessage message = new MqttMessage();
		message.setPayload(idEdge.getBytes());
		client.publish("EdgeDevice", message);
	}

	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		System.out.println("Connection lost!");

	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("Edge device received:" + message);
		String[] digestMessage= message.toString().split(" ");
		if (digestMessage[0].equals("new_update")) {
//			startAuth();
		}
		else if(digestMessage[0].equals("auth_request"))
		{
			String decodedMAC= ccm.encode(Kiks, digestMessage[0]+" "+digestMessage[1]+ " "+digestMessage[2]+ " "+digestMessage[3]);
			if(digestMessage[digestMessage.length-1].equals(decodedMAC))
			{
				startAuth( ccm.ccmaes(false, digestMessage[3], Km, iv));
			}
			else
			{
				System.out.println("Auth request2 mac check fail");
			}
		}
		else if(digestMessage[0].equals("auth_request2"))
		{
			System.out.println("Auth request2 recieved");
			// msg4 When the Gateway receives Message3
			String message4 = IDg +" " + ccm.ccmaes(true, "Ack", Km, iv) +" ";
			String  MAC =ccm.encode(message4 ,Kiks);
			MqttMessage lastMessage = new MqttMessage();
			lastMessage.setPayload((message4+ MAC).getBytes());
			thisClient.publish("EdgeDevice",lastMessage );
			// msg 3 When the IoT device receives Message 2
//			startAuth();
		}
		
	}

	private void startAuth(String nonce1) throws Exception {
		// TODO Auto-generated method stub
		RandFirmSeqsi = generateSetOfSeqNumbers(10);
		RandFirmSeqsiPlus1 = generateSetOfSeqNumbers(10);
		SecureRandom random = new SecureRandom();
		
		for (int i = 0; i < RandFirmSeqsiPlus1.length; i++) {
			RandomSequence += RandFirmSeqsiPlus1[i] + " ";
		}
		nonce2 = secureRandom();
		String encryptednonce2=  ccm.ccmaes(true, nonce2, Km, iv);
		String MAC = ccm.encode(Kiks, nonce2 + nonce1);
		String encryptedSequence = "";
		String encryptedMessage = "";

		encryptedMessage += ccm.ccmaes(true,nonce2, Km, iv) + ",";
		for (int i = 0; i < RandFirmSeqsiPlus1.length; i++) {
			encryptedMessage += ccm.ccmaes(true, RandFirmSeqsiPlus1[i], Km, iv) + " ";
		}
		encryptedMessage += "," + ccm.ccmaes(true,nonce1, Km, iv);
		String message2 = IDg + "," + encryptedMessage + "," + MAC;
		MqttMessage message = new MqttMessage();
		message.setPayload(message2.getBytes());
		thisClient.publish("EdgeDevice", message);
		listenIoTDevices();
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		System.out.println("Message delivered! from gateway");

	}
	
	static String[] generateSetOfSeqNumbers(int randoms) {
		ccm = new CCMImpl();
//		SecureRandom random = new SecureRandom();
//		int[] arrayInt = new int[randoms];
//		String[] arrayString = new String[randoms];
//		arrayInt[0] = Math.abs(random.nextInt());
//		arrayString[0] = Integer.toString(arrayInt[0]);
//		for (int i = 1; i < randoms; i++) {
//			int number;
//			String encryptedNumber = "";
//			do {
//				arrayInt[i] = Math.abs(random.nextInt());
//				encryptedNumber = ccm.ccmaes(true, Integer.toString(arrayInt[i]), Km, iv);
//			} while (ccm.ccmaes(false, encryptedNumber, Km, iv).equals("mac check in CCM failed"));
//			arrayString[i] = Integer.toString(arrayInt[i]);
//		}
		String arrayInt[] = {"234365208","247058986","859950640","886209246","1758019480","1959642116","1161690206","1406042517","734704895","640922405"};
		return arrayInt;
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
