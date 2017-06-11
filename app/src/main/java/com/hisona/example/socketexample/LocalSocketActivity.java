package com.hisona.example.socketexample;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class LocalSocketActivity extends Activity {

	TextView title;
	EditText sendText;
	Button sendButton;
	TextView textResult;
	ProgressDialog progDialog;

	final static int SEND_SUCCESS =	1;
	final static int SOCK_TIMEOUT =	2;
	final static int SOCK_ERROR =	3;

	final static int CLIENT_CONNECTED = 1;
	final static int CLIENT_CLOSED =    2;
	final static int CLIENT_DATA =      3;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.socket_example);

		title = (TextView)findViewById(R.id.TitleView);
		sendText = (EditText)findViewById(R.id.SendText);
		sendButton = (Button)findViewById(R.id.SendButton);
		textResult = (TextView)findViewById(R.id.ResultView);

		title.setText("LocalSocket Example :");

		Thread svrThread = new ServerThread("test_socket");
		svrThread.setDaemon(true);
		svrThread.start();

		sendButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {

				sendButton.setEnabled(false);

				progDialog = new ProgressDialog(LocalSocketActivity.this);
				progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progDialog.setCancelable(true);

				progDialog.setMessage("Waiting for message ...");
				progDialog.show();

				String strSend = sendText.getText().toString();

				new LocalSocketRunnable(strSend);
			}
		});
	}

	// *********************************************************************************
	//		Server Socket Implementation.
	// *********************************************************************************
	class ServerThread extends Thread {

		String mAddress;

		public ServerThread(String address) {
			mAddress = address;
		}

		public void sendMessage(int type)
		{
			serverHandler.sendEmptyMessage(type);
		}

		public void sendMessage(int type, String text)
		{
			Message msg = new Message();
			Bundle bundle = new Bundle();

			bundle.putByteArray("text", text.getBytes());

			msg.what = type;
			msg.setData(bundle);

			serverHandler.sendMessage(msg);
		}

		public void sendMessage(int type, byte[] text)
		{
			Message msg = new Message();
			Bundle bundle = new Bundle();

			bundle.putByteArray("text", text);

			msg.what = type;
			msg.setData(bundle);

			serverHandler.sendMessage(msg);
		}

		public void run() {
			try {
				LocalServerSocket localsvr = new LocalServerSocket(mAddress);
				while(true) {
					LocalSocket client = localsvr.accept();

					String str = client.toString();

					sendMessage(CLIENT_CONNECTED, str);

					new ClientThread(client).start();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class ClientThread extends Thread {

		private LocalSocket client;

		public ClientThread(LocalSocket socket) {
			client = socket;
		}

		public void sendMessage(int type)
		{
			serverHandler.sendEmptyMessage(type);
		}

		public void sendMessage(int type, String text)
		{
			Message msg = new Message();
			Bundle bundle = new Bundle();

			bundle.putByteArray("text", text.getBytes());

			msg.what = type;
			msg.setData(bundle);

			serverHandler.sendMessage(msg);
		}

		public void sendMessage(int type, byte[] text)
		{
			Message msg = new Message();
			Bundle bundle = new Bundle();

			bundle.putByteArray("text", text);

			msg.what = type;
			msg.setData(bundle);

			serverHandler.sendMessage(msg);
		}

		public void run() {

			try {
				InputStream inputStream = client.getInputStream();
				OutputStream outputStream = client.getOutputStream();

				byte[] rbuffer = new byte[1024];
				int read;

				while(true) {

					read = inputStream.read(rbuffer, 0, 1024);

					if(read > 0)
					{
						byte[] buffer = new byte[read];
						System.arraycopy(rbuffer, 0, buffer, 0, read);

						outputStream.write(buffer);

						sendMessage(CLIENT_DATA, buffer);
					}
					else
					{
						sendMessage(CLIENT_CLOSED);
						break;
					}

				}

				inputStream.close();
				outputStream.close();
				client.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	Handler serverHandler = new Handler()
	{
		public void handleMessage( Message msg )
		{
			switch(msg.what)
			{
				case CLIENT_CONNECTED:
					if( !msg.getData().isEmpty() ) {
						String str = new String(msg.getData().getByteArray("text"));
						textResult.append("Client Connected: " + str + "\n");
					} else {
						textResult.append("Client Connected.\n");
					}
					break;
				case CLIENT_CLOSED:
					if( !msg.getData().isEmpty() ) {
						String str = new String(msg.getData().getByteArray("text"));
						textResult.append("Client Closed: " + str + "\n");
					} else {
						textResult.append("Client Closed.\n");
					}
					break;
				case CLIENT_DATA:
					if( !msg.getData().isEmpty() ) {
						String str = new String(msg.getData().getByteArray("text"));
						textResult.append("Client Send: " + str + "\n");
					} else {
						textResult.append("Client Send Data.\n");
					}
					break;
			}
		}
	};


	// *********************************************************************************
	//	Client Socket Implementation.
	//*********************************************************************************

	class LocalSocketRunnable implements Runnable {

		String m_sbuffer;

		LocalSocket socket = null;

		public LocalSocketRunnable(String msg)
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

			byte sbuffer[] = m_sbuffer.getBytes();

			try {
				socket = new LocalSocket();

				LocalSocketAddress addr = new LocalSocketAddress("test_socket");

				socket.connect(addr);

				InputStream inputStream = socket.getInputStream();
				OutputStream outputStream = socket.getOutputStream();

				outputStream.write(sbuffer);

				byte[] rbuffer = new byte[1024];
				int read;

				socket.setSoTimeout(3000);
				read = inputStream.read(rbuffer, 0, 1024);
				if(read > 0)
				{
					byte[] buffer = new byte[read];
					System.arraycopy(rbuffer, 0, buffer, 0, read);
					sendMessage(SEND_SUCCESS, buffer);
				}
				else
				{
					sendMessage(SOCK_TIMEOUT);
				}

				socket.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				try {
					socket.close();
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}

				sendMessage(SOCK_ERROR);
			}
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
						textResult.append(str + "\n");
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

