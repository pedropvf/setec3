package com.example.socket_android;

import java.io.DataInputStream;
import java.net.Socket;
import android.util.Log;
import android.widget.TextView;

public class ReadNet extends Thread{
	
	TextView answertxt;
	DataInputStream input;
	Socket socket;
	String sendtxt;
	String answer;
	MainActivity main;
	
	//comment
	
	ReadNet(TextView txtanswer, MainActivity _main){
		answertxt = txtanswer;
		main = _main;
	}

	public void run(){
		
		try {
			socket = new Socket("192.168.1.101",4000);
			main.socket = socket;
		} catch (Exception e) {
			Log.e("NetThread",e.toString());
			return;
		}
		
		while(true){
			
			try{
				
				input = new DataInputStream(socket.getInputStream());
				
				answer = input.readUTF();
				
				answertxt.post(new Runnable(){ // 
	
					public void run() {
						try {
							answertxt.setText(answer);
						} catch (Exception e) {
							answertxt.setText(e.toString());
						}
					}	
				});
				
			}catch (Exception e){
				Log.e("NetThread",e.toString());
				return;				
			}
			
		}
		
		/*try {
			socket.close();
			input.close();
		} catch (Exception e) {
			Log.e("NetThread",e.toString());
		}*/
		
		
	}

}