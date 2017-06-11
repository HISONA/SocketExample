package com.hisona.example.socketexample;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {

	class Example {
		Example(Class<?> aClass, String aTitle) {
			mClass = aClass;
			mTitle = aTitle;
		}
		String mTitle;
		Class<?> mClass;
	}
	Example[] arExample = {
			new Example(LocalSocketActivity.class, "LocalSocket Example"),
			new Example(TCPSocketActivity.class, "TCPSocket Example"),
			new Example(UDPSocketActivity.class, "UDPSocket Example"),
			new Example(HttpsActivity.class, "HTTP/HTTPS Example")
	};
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		ListView listview = new ListView(this);
		ArrayList<String> arTitle = new ArrayList<String>();
		for (Example e : arExample) {
			arTitle.add(e.mTitle);
		}

		ArrayAdapter<String> Adapter;
		Adapter = new ArrayAdapter<String>(this, R.layout.mainlist, arTitle);
		listview.setAdapter(Adapter);

		final Context ctx = this;
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(ctx, arExample[position].mClass);
				startActivity(intent);
			}
		});

		setContentView(listview);
    }
}

