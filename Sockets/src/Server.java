import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	static Socket socket;
	
	public static void main(String[] args) {
		
		//coments
		
		System.out.println("iniciado servidor");
		ServerSocket serversocket = null;
		
		try{
			serversocket = new ServerSocket(4000);
			socket = serversocket.accept();
			
			Write_thread send_t = new Write_thread();
	    	send_t.start();
			
			//get client's ip
			InetAddress ip = socket.getInetAddress();
			System.out.println(ip.getHostAddress());
			
			//get client's port
			int port = socket.getPort();
			System.out.println(String.valueOf(port));
			
		}catch(Exception e){
			  System.out.println("Erro:" + e);
			  return;
		}
		
		DataInputStream input=null;
		
		try{
			while(true){
				
			input = new DataInputStream(socket.getInputStream()); 
			
			String mensagem = input.readUTF();
			System.out.println("Mensagem recebida: " + mensagem);
			if(mensagem.equals("sair")) break;
	
			}	
		//serversocket.close(); 
		}catch(Exception e){
		  System.out.println("Erro:" + e);
		  return;
		}
			
		try {
			input.close();
			socket.close();
			serversocket.close();
		} catch (Exception e) {
			System.out.println("Erro:" + e);
			return;
		}
		
		System.exit(0);
		
	}

	public static class Write_thread extends Thread {
	
		public void run(){
			
		try{
			
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			
			while(true){
			
				BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
				String linha = teclado.readLine();
				
				output.writeUTF(linha);
			}
			
		}catch (Exception e){
			System.out.println("Erro:" + e);
		}
		
		}
		
	}

}
