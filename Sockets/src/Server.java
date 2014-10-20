import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("iniciado servidor");
		
		try{
			ServerSocket serversocket = new ServerSocket(4000);
			
			while (true){
				Socket socket = serversocket.accept();
				
				//get client's ip
				InetAddress ip = socket.getInetAddress();
				System.out.println(ip.getHostAddress());
				
				//get client's port
				int port = socket.getPort();
				System.out.println(String.valueOf(port));
				
				DataInputStream input = new DataInputStream(socket.getInputStream()); 
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());
				
				String mensagem = input.readUTF();
				System.out.println("Mensagem: " + mensagem);
				output.writeUTF("OK");
									
				input.close();
				output.close();
				socket.close();
				
			}
			
			//serversocket.close(); 
			}catch(Exception e){
			  System.out.println("Erro:" + e);
			}
	}

}
