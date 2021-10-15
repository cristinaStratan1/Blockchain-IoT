package impl;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Scanner;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import entity.Gateway;
import entity.IoT;

public class Communication {

	static IoT iot;
	static Gateway edge;
	static CCMImpl ccm;
	
	public static void main(String[] args) throws Exception {
		edge = new Gateway();
		edge.listentoFirmwareUpdate();
		edge.listenIoTDevices();
		iot = new IoT();
		iot.setId(100);
		iot.sendAuthetication();
	}
	
	
}