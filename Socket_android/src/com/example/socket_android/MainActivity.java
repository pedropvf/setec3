package com.example.socket_android;

import android.app.Activity;
import android.content.Context;
import android.view.View.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{

	private Button botao1;
	TextView received_textview;
	EditText EditText1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        botao1 = (Button)findViewById(R.id.button1);
        botao1.setOnClickListener(this);
        
        received_textview = (TextView)findViewById(R.id.received);
        EditText1 = (EditText)findViewById(R.id.edit1);
        
    }

    public void onClick(View v) {    	
    	
    	String str_send = EditText1.getText().toString();
    	NetThread t_net = new NetThread(str_send, received_textview);
    	t_net.start();
	}
    
    //check internet connection. still don't know where to put it. in a thread maybe?
    /*public boolean isOnline() {
    ConnectivityManager cm =
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();
    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
        return true;
    }
    return false;
	}*/

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */
	
}
