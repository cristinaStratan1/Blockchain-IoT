package impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.Before;
import org.junit.Test;

import entity.Gateway;
import entity.IoT;

public class IoTTest implements MqttCallback {

	MqttClient client;

	@Before
	public void connect() throws MqttSecurityException, MqttException {
		client = new MqttClient("tcp://broker.emqx.io:1883", "IoTDevice");
		client.connect();
	}

	@Test
	public void IoTTest() throws Exception {
		IoT iot = new IoT();
		iot.sendAuthetication();
		client.setCallback(this);
		client.subscribe("IoTDevice");
	}

	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// TODO Auto-generated method stub
		String[] digestedMessage = message.toString().split(" ");
		assertEquals("delivered", digestedMessage);

	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}

}
