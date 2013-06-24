package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;



public class WebsocketServer extends Server {
	
	public WebsocketServer(int port) {
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(port);
		addConnector(connector);
		WebSocketHandler wsHandler = new WebSocketHandler() {
			public WebSocket doWebSocketConnect(HttpServletRequest request,	String protocol) {
				return new FaceDetectWebSocket();
			}
		};
		setHandler(wsHandler);
	}
 

	private static class FaceDetectWebSocket implements WebSocket,
			WebSocket.OnBinaryMessage, WebSocket.OnTextMessage {
 
		private Connection connection;
		
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		
		private FaceDetection faceDetection = new FaceDetection();
 
		public FaceDetectWebSocket() {
			super();  
		}
 
		
		public void onOpen(Connection connection) {
			this.connection = connection;
			this.connection.setMaxBinaryMessageSize(5242890);
		}
 
		
		public void onClose(int code, String message) {}
 
		
		public void onMessage(byte[] data, int offset, int length) {
			
			bOut.reset(); 
			bOut.write(data, offset, length);
			  
			try {		
				
				faceDetection.detect(bOut.toByteArray());
				
			} catch (IOException e) {
				System.out.println(" Exception in onMessage method");
			}
		}

		@Override
		public void onMessage(String arg0) {
		int result;
			if(arg0.equals("Compare"))
			{
				try {
					result = faceDetection.prediction();
					this.connection.sendMessage(Integer.toString(result));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(arg0.equals("Add"))
			{
				
			}
			else
			{
				
			}
			
		}
 
 
	}
 
	
	public static void main(String[] args) throws Exception {
		WebsocketServer server = new WebsocketServer(9999);
		server.start();
		server.join();
	}	
}