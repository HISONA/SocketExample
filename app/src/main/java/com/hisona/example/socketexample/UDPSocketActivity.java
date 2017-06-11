package com.hisona.example.socketexample;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UDPSocketActivity extends Activity {

	TextView title;
	EditText sendText;
	Button sendButton;
    TextView textResult;
    ProgressDialog progDialog;
    
    final static int SEND_SUCCESS =	1;
    final static int RECV_SUCCESS =	2;
    final static int SOCK_TIMEOUT =	3;
    final static int SOCK_ERROR =	4;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.socket_example);
        
        title = (TextView)findViewById(R.id.TitleView);
        sendText = (EditText)findViewById(R.id.SendText);
        sendButton = (Button)findViewById(R.id.SendButton);
        textResult = (TextView)findViewById(R.id.ResultView);

        title.setText("UDPSocket Example : ");
        
        Thread svrThread = new ServerThread(12345);
        svrThread.setDaemon(true);
        svrThread.start();
        
        sendButton.setOnClickListener(new Button.OnClickListener() {
        	
        	public void onClick(View v) {

		        sendButton.setEnabled(false);

		        progDialog = new ProgressDialog(UDPSocketActivity.this);
                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDialog.setCancelable(true);
        		
        		progDialog.setMessage("Waiting for message ...");
        		progDialog.show();

        		String strSend = sendText.getText().toString();
       		
        		new UDPSocketRunnable(strSend);
        	}
        });
    }

    class ServerThread extends Thread {

    	private int mPort;
    	
    	public ServerThread(int port) {
    		mPort = port;
    	}

    	public void sendMessage(int type, String text)
    	{
			Message msg = new Message();
			Bundle bundle = new Bundle();
			
			bundle.putByteArray("text", text.getBytes());

			msg.what = type;
			msg.setData(bundle);
			
			socketHandler.sendMessage(msg);
    	}
    	
    	public void run() {

			try {
				DatagramSocket udpsvr = new DatagramSocket(mPort);
				
    			byte[] rbuffer = new byte[1024];
    			DatagramPacket r_packet = new DatagramPacket(rbuffer, rbuffer.length);

    			while(true) {

    				try {
        				udpsvr.receive(r_packet);
        				        
        				if(r_packet.getLength() > 0)
        				{
            				String str = r_packet.getAddress().getHostAddress() + ":" + 
        					r_packet.getPort();
        				
            				sendMessage(RECV_SUCCESS, str);

        					udpsvr.send(r_packet);
        				}
        				
        			} catch(IOException e) {
						// TODO Auto-generated catch block
	        			e.printStackTrace();
        			}		
    			}
				
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
    }
    
    class UDPSocketRunnable implements Runnable {
    	
    	String m_sbuffer;
    	
    	public UDPSocketRunnable(String msg)
    	{
    		m_sbuffer = msg;
    		new Thread(this).start();
    	}

    	public void sendMessage(int type)
    	{
    		socketHandler.sendEmptyMessage(type);
    	}
    	
    	public void sendMessage(int type, String text)
    	{
			Message msg = new Message();
			Bundle bundle = new Bundle();
			
			bundle.putByteArray("text", text.getBytes());

			msg.what = type;
			msg.setData(bundle);
			
			socketHandler.sendMessage(msg);
    	}
    	
    	public void sendMessage(int type, byte[] text)
    	{
			Message msg = new Message();
			Bundle bundle = new Bundle();
			
			bundle.putByteArray("text", text);

			msg.what = type;
			msg.setData(bundle);
			
			socketHandler.sendMessage(msg);
    	}
    	
    	public void run() {
    		
    		int len = m_sbuffer.length();
    		byte sbuffer[] = m_sbuffer.getBytes();
			
			try 
			{
    			DatagramSocket udp = new DatagramSocket();
        		try 
				{
        			InetAddress server = InetAddress.getByName("127.0.0.1");
        			int port = 12345;

	        		DatagramPacket s_packet = new DatagramPacket(sbuffer, len, server, port);
	        		
	        		try {
	        			udp.send(s_packet);
	        		} catch (IOException e) {
						// TODO Auto-generated catch block
	        			e.printStackTrace();
	        			
	        			sendMessage(SOCK_ERROR);
	        			return;
	        		}
        			
        			udp.setSoTimeout(5000);
        			
        			byte[] rbuffer = new byte[1024];
        			DatagramPacket r_packet = new DatagramPacket(rbuffer, rbuffer.length);
        			
        			try {
        				udp.receive(r_packet);
        				        
        				if(r_packet.getLength() > 0)
        				{
        					byte[] buffer = new byte[r_packet.getLength()];
        					System.arraycopy(r_packet.getData(), 0, buffer, 0, r_packet.getLength());
    	        			sendMessage(SEND_SUCCESS, buffer);
        					return;	
        				}
        				
        			} catch(IOException e) {
						// TODO Auto-generated catch block
	        			e.printStackTrace();
	        			sendMessage(SOCK_TIMEOUT);
	        			return;
        			}		
        			
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			sendMessage(SOCK_ERROR);
    	}
    }
    
    Handler socketHandler = new Handler()
	{
		public void handleMessage( Message msg )
		{
			switch(msg.what)
			{
			case SEND_SUCCESS: 
		        sendButton.setEnabled(true);
		        if( !msg.getData().isEmpty() ) {
		        	String str = new String(msg.getData().getByteArray("text"));
		        	textResult.append("Echo from Server : " + str + "\n");
		        }
		        break;

			case RECV_SUCCESS: 
		        sendButton.setEnabled(true);
		        if( !msg.getData().isEmpty() ) {
		        	String str = new String(msg.getData().getByteArray("text"));
		        	textResult.append("Recv from Client: " + str + "\n");
		        }
		        break;

			case SOCK_TIMEOUT: 
		        sendButton.setEnabled(true);
		        if( !msg.getData().isEmpty() ) {
		        	String str = new String(msg.getData().getByteArray("text"));
		        	textResult.append("Socket Timeout: " + str + "\n");
		        } else {
			        textResult.append("Socket Timeout.\n");
		        }	
		        break;

			case SOCK_ERROR: 
		        sendButton.setEnabled(true);
		        if( !msg.getData().isEmpty() ) {
		        	String str = new String(msg.getData().getByteArray("text"));
		        	textResult.append("Socket Error: " + str + "\n");
		        } else {
			        textResult.append("Socket Timeout.\n");
		        }	
		        break;
			}

			progDialog.cancel();
		}
	};
    
}
