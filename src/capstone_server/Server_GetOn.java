package capstone_server;

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

public class Server_GetOn {
	public static void main(String[] args) throws MqttException {
		MqttClient client = new MqttClient("tcp://iot.eclipse.org:1883", MqttClient.generateClientId());
		client.setCallback(new MqttCallback() {

			@Override
			public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
				// TODO Auto-generated method stub
				String[] msg = arg1.toString().split("&");
				String selection = msg[0];
				String busSeq = msg[1]; // 30 
				String routeId = msg[2];
				String busNumber = msg[3]; // 12<sk 7888
				System.out.printf("Selection : %s\nSeq : %s\nRoute : %s\nPlate : %s\n", selection, busSeq, routeId, busNumber);
				String uri = "http://openapi.gbis.go.kr/ws/rest/buslocationservice?serviceKey=i%2FmgmkmoCSBv8EUR8Jv1%2FTOw767UUNZEI%2FSGQnCmnDSb4kM1Vty5Dsqlw%2Bcx%2B8o%2FtfNUzA7PNyaMnqVHCMqD8A%3D%3D&routeId="
						+ routeId;
				boolean waitArrived = true;
				
			
			
				if (selection.equals("geton")) {
					
					while (waitArrived) {
						
						org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
								.parse(uri);
						XPath xpath = XPathFactory.newInstance().newXPath();
						NodeList plateNoList = (NodeList) xpath.evaluate("//busLocationList/plateNo", document,
								XPathConstants.NODESET);
						NodeList stationSeqList = (NodeList) xpath.evaluate("//busLocationList/stationSeq", document,
								XPathConstants.NODESET);

						System.out.println("plate length: "+plateNoList.getLength());
						System.out.println("station length: "+stationSeqList.getLength());

						
						for (int i = 0; i < plateNoList.getLength(); i++) {
							if ((Integer.parseInt(stationSeqList.item(i).getTextContent()) == Integer.parseInt(busSeq)-1) && (plateNoList.item(i).getTextContent().equals(busNumber))) {
								System.out.println("Send to Raspberry On : " + busNumber);
								String res_msg = "geton&" + busNumber; 
								client.publish("bus_responses",res_msg.getBytes(),0,false);
								break;
							} 
						}
						
						for (int i = 0; i < plateNoList.getLength(); i++) {
							if((Integer.parseInt(stationSeqList.item(i).getTextContent()) >= Integer.parseInt(busSeq)) && (plateNoList.item(i).getTextContent().equals(busNumber))) {
								client.reconnect();
								System.out.println("Send to Raspberry On : " + busNumber);
								String res_msg = "geton_end&" + busNumber; 
								client.publish("bus_responses",res_msg.getBytes(),0,false);
								waitArrived = false;
							}
						}
					
						
						System.out.println("Get on");
						Thread.sleep(10000);
					}
				} else if(selection.equals("getoff")) {
					while (waitArrived) {
						org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
								.parse(uri);
						XPath xpath = XPathFactory.newInstance().newXPath();
						NodeList plateNoList = (NodeList) xpath.evaluate("//busLocationList/plateNo", document,
								XPathConstants.NODESET);
						NodeList stationSeqList = (NodeList) xpath.evaluate("//busLocationList/stationSeq", document,
								XPathConstants.NODESET);

						for (int i = 0; i < plateNoList.getLength(); i++) {
							if ((Integer.parseInt(stationSeqList.item(i).getTextContent()) == Integer.parseInt(busSeq) -1) && (plateNoList.item(i).getTextContent().equals(busNumber))) {
								System.out.println("Send to Raspberry Off : " + busNumber);
								String res_msg = "getoff&" + busNumber; 
								client.publish("bus_responses",res_msg.getBytes(),0,false);
					
							}else if((Integer.parseInt(stationSeqList.item(i).getTextContent()) == Integer.parseInt(busSeq)) && (plateNoList.item(i).getTextContent().equals(busNumber))) {
								System.out.println("Send to Raspberry On : " + busNumber);
								String res_msg = "getoff_end&" + busNumber; 
								client.publish("bus_responses",res_msg.getBytes(),0,false);			
								waitArrived = false;
							}
						}

						Thread.sleep(10000);
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
		System.out.printf("Connection Established - %s\n", client.getServerURI());
		client.subscribe("bus_request");

	}

}
