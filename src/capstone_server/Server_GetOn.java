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
		MqttClient subClient = new MqttClient("tcp://iot.eclipse.org:1883", MqttClient.generateClientId());
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
				
				boolean goRas_geton = false; //  subscribe message duplication prohibition
				boolean goRas_getend = false; //  subscribe message duplication prohibition
				boolean goRas_getoff = false; //  subscribe message duplication prohibition
				boolean goRas_getoffend = false; //  subscribe message duplication prohibition
			
				if (selection.equals("geton")) {
					
					while (waitArrived) {
						
						org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
								.parse(uri);
						XPath xpath = XPathFactory.newInstance().newXPath();
						NodeList plateNoList = (NodeList) xpath.evaluate("//busLocationList/plateNo", document,
								XPathConstants.NODESET);
						NodeList stationSeqList = (NodeList) xpath.evaluate("//busLocationList/stationSeq", document,
								XPathConstants.NODESET);

						System.out.println("geton_plate length: "+plateNoList.getLength());
						System.out.println("geton_station length: "+stationSeqList.getLength());

						
						for (int i = 0; i < plateNoList.getLength(); i++) {
							if ((Integer.parseInt(stationSeqList.item(i).getTextContent()) == Integer.parseInt(busSeq)-1) && (plateNoList.item(i).getTextContent().equals(busNumber))) {
								if(goRas_geton == false) {
									System.out.println("geton_Send to Raspberry On1 : " + busNumber);
									String res_msg = "geton&" + busNumber; 
									client.publish("bus_responses",res_msg.getBytes(),1,false);
									System.out.println("Blocked");
									goRas_geton = true;
									break;
								}
							} 
						}
						
						for (int i = 0; i < plateNoList.getLength(); i++) {
							System.out.println("2nd for");
							if((Integer.parseInt(stationSeqList.item(i).getTextContent()) >= Integer.parseInt(busSeq)) && (plateNoList.item(i).getTextContent().equals(busNumber))) {
								if(goRas_getend == false) {
									System.out.println("geton_Send to Raspberry On2 : " + busNumber);
									String res_msg = "geton_end&" + busNumber; 
									client.publish("bus_responses",res_msg.getBytes(),1,false);
									waitArrived = false;
									goRas_getend = true;
								}
							}
						}
						Thread.sleep(10000);
					}
					System.out.println("geton_While escaped");
				} else if(selection.equals("getoff")) {
					while (waitArrived) {
						org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
								.parse(uri);
						XPath xpath = XPathFactory.newInstance().newXPath();
						NodeList plateNoList = (NodeList) xpath.evaluate("//busLocationList/plateNo", document,
								XPathConstants.NODESET);
						NodeList stationSeqList = (NodeList) xpath.evaluate("//busLocationList/stationSeq", document,
								XPathConstants.NODESET);
						
						
						System.out.println("getoff_plate length: "+plateNoList.getLength());
						System.out.println("getoff_station length: "+stationSeqList.getLength());
						
						for (int i = 0; i < plateNoList.getLength(); i++) {
							if ((Integer.parseInt(stationSeqList.item(i).getTextContent()) == Integer.parseInt(busSeq)-1) && (plateNoList.item(i).getTextContent().equals(busNumber))) {
								if(goRas_getoff == false) {
									System.out.println("getoff_Send to Raspberry On1 : " + busNumber);
									String res_msg = "getoff&" + busNumber; 
									client.publish("bus_responses",res_msg.getBytes(),1,false);
									System.out.println("Blocked");
									goRas_getoff = true;
									break;
								}
							} 
						}
						
						for (int i = 0; i < plateNoList.getLength(); i++) {
							System.out.println("2nd for");
							if((Integer.parseInt(stationSeqList.item(i).getTextContent()) >= Integer.parseInt(busSeq)) && (plateNoList.item(i).getTextContent().equals(busNumber))) {
								if(goRas_getoffend == false) {
									System.out.println("getoff_Send to Raspberry On2 : " + busNumber);
									String res_msg = "getoff_end&" + busNumber; 
									client.publish("bus_responses",res_msg.getBytes(),1,false);
									waitArrived = false;
									goRas_getoffend = true;
								}
							}
						}
						

						Thread.sleep(10000);
					}
					System.out.println("getoff_While escaped");
				}
				
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken arg0) {
				// TODO Auto-generated method stub paho10742599539225

			}

			@Override
			public void connectionLost(Throwable arg0) {
				// TODO Auto-generated method stub

			}
		});
		System.out.println("client.getClientId()=="+client.getClientId());
		client.connect();
		System.out.printf("Connection Established - %s\n", client.getServerURI());
		client.subscribe("bus_request");

	}

}
