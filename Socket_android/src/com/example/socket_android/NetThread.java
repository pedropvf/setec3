package com.example.socket_android;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import android.util.Log;
import android.widget.TextView;

public class NetThread extends Thread{
	
	TextView answertxt;
	DataInputStream input;
	DataOutputStream output;
	Socket socket;
	String sendtxt;
	String answer;
	
	NetThread(String send, Socket sock){
		sendtxt = send;
		socket = sock;
	}

	public void run(){
		
		try{
			
			output = new DataOutputStream(socket.getOutputStream());
			
			Log.d("NetThread", socket.getInetAddress().toString());
			
			output.writeUTF(sendtxt);
			
		}catch (Exception e){
			Log.e("NetThread",e.toString());
		}
		
		
	}

}
/*
try{
	
	socket = new Socket("89.155.159.210",5132);
	
	input = new DataInputStream(socket.getInputStream());
	
	output = new DataOutputStream(socket.getOutputStream());
	
	Log.d("NetThread", socket.getInetAddress().toString());
	
	output.writeUTF(sendtxt);
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
	
	socket.close();
	input.close();
	output.close();
	
}catch (Exception e){
	Log.e("NetThread",e.toString());
}*/