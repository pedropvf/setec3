import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Client {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try{
			//create object from class Socket (TCP) on name xx and port xx
			Socket socket = new Socket("192.168.1.70",4000);
			
			//create object from class DataInputStream 
			DataInputStream input = new DataInputStream(socket.getInputStream());
			
			//create object from class DataOutputStream 
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			
			//#1
			//send to the server - method write
			output.writeUTF("primeira mensagem");
			//receive from the server - method read (blocked)
			String answer = input.readUTF();
			//print answer
			System.out.println(answer);
						
			socket.close();
			input.close();
			output.close();
						
		}catch (Exception e){
			System.out.println("erro " + e);
		}

	}

}
