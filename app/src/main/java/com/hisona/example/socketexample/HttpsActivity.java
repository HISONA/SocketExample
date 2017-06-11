package com.hisona.example.socketexample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class HttpsActivity extends Activity { 

	TextView title;
	EditText sendText;
	Button sendButton;
    TextView textResult;
    ProgressDialog progDialog;
 	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.socket_example);

        title = (TextView)findViewById(R.id.TitleView);
        sendText = (EditText)findViewById(R.id.SendText);
        sendButton = (Button)findViewById(R.id.SendButton);
        textResult = (TextView)findViewById(R.id.ResultView);
        
        title.setText("HTTP/HTTPS Example :");
        sendText.setText("https://docs.google.com");
        
        sendButton.setOnClickListener(new Button.OnClickListener() {
        	
        	public void onClick(View v) {

        		String strURL = sendText.getText().toString();
       		
        		String strHtml = HttpUtil.DownloadHtml(strURL);
        		
        		textResult.setText(strHtml);
        	}
        });
    
    }
    
}
