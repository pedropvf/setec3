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
	
	ReadNet(TextView txtanswer, MainActivity _main){
		answertxt = txtanswer;
		main = _main;
	}

	public void run(){
		
		try {
			socket = new Socket("89.155.159.210",5132);
			main.socket = socket;
		} catch (Exception e) {
			Log.e("NetThread",e.toString());
			return;
		}
		
		while(true){
			
			try{
				
				input = new DataInputStream(socket.getInputStream());
				
				answer = input.readUTF();
				
				if(answer.equals("sair")) break;
				
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
			}
			
		}
		
		try {
			socket.close();
			input.close();
		} catch (Exception e) {
			Log.e("NetThread",e.toString());
		}
		
		
	}

}