package capstone_server;

import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.w3c.dom.NodeList;

public class Server_GetOn{	
	public static void main(String[] args) throws MqttException {
		MqttClient client = new MqttClient("tcp://iot.eclipse.org:1883", MqttClient.generateClientId());
		client.setCallback(new MqttCallback() {
			
			@Override
			public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
				// TODO Auto-generated method stub
				String[] msg = arg1.toString().split("&");
				String busSeq = msg[0];
				String routeId = msg[1];
				String busNumber = msg[2];
				System.out.printf("Seq : %s\nRoute : %s\nPlate : %s\n",busSeq,routeId,busNumber);
				
				String uri = "http://openapi.gbis.go.kr/ws/rest/buslocationservice?serviceKey=i%2FmgmkmoCSBv8EUR8Jv1%2FTOw767UUNZEI%2FSGQnCmnDSb4kM1Vty5Dsqlw%2Bcx%2B8o%2FtfNUzA7PNyaMnqVHCMqD8A%3D%3D&routeId=" + routeId;
				
				Document document = (Document) DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(uri);
				XPath xpath = XPathFactory.newInstance().newXPath();
				
				NodeList plateNoList =  (NodeList) xpath.evaluate("//busLocationList/plateNo", document, XPathConstants.NODESET);
				NodeList stationSeqList =  (NodeList) xpath.evaluate("//busLocationList/stationSeq", document, XPathConstants.NODESET);
				
				for (int i = 0; i < plateNoList.getLength(); i++) {
                    if(Integer.parseInt(stationSeqList.item(i).getTextContent())== Integer.parseInt(busSeq)-1
                    		&& plateNoList.item(i).getTextContent().equals(busNumber)) {
                    	System.out.println("Send to Raspberry : " + busNumber);
                    }
                }
				
			}
			
			@Override
			public void deliveryComplete(IMqttDeliveryToken arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void connectionLost(Throwable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		client.connect();
		System.out.printf("Connection Established - %s\n",client.getServerURI());
		client.subscribe("geton");

		
	}

}
