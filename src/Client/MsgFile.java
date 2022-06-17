package Client;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.Scanner;

public class MsgFile {

	private static ObjectInputStream in;
	private static ObjectOutputStream out;
	
	
	public static void main(String[] args) {
		
	 	
		try {
			
			connection(args[0]);
			
			logIn(args[1]);
			
			whileCicle();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
			
		
				
			
		
	}


	/**
	 * Conexão ao servidor
	 * @param address endreço ip
	 */
	private static void connection(String address) {
		try {
			
			String [] adr = address.split(":");
			InetAddress ip = InetAddress.getByName(adr[0]);
			int port =  Integer.parseInt(adr[1]);
			
			Socket echoSocket = new Socket(ip, port);
			in = new ObjectInputStream(echoSocket.getInputStream()); //recebe
			out = new ObjectOutputStream(echoSocket.getOutputStream()); //envia
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void logIn(String userPass) throws IOException, ClassNotFoundException {
		String user = userPass.split(":")[0];
		String password = userPass.split(":")[1];
		
			
			out.writeObject(user);
			out.writeObject(password);
			
			if(!(boolean) in.readObject()) 
				System.err.println("Incorect credentials");
			else { 
				System.out.println("LogIn Success");
				System.out.println(in.readObject()); // recebe lista de comandos
			}
		
	}
	/**
	 * ciclo while responsavel para mandar os pedidos ao servidor ate quando o client
	 * não sai (quit)
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private static void whileCicle() throws IOException, ClassNotFoundException {
		
		
		Scanner sc = new Scanner(System.in);
		boolean finished = false;
		while( !finished ) {
			
			String command = sc.nextLine();
			String[] c =  command.split(" ");
			String operation = c[0];
			
			out.writeObject(command);			
			
			if(operation.equalsIgnoreCase("quit")) {
				finished = true;
				System.out.println("Client disconected");
				break;
			}else if (operation.equalsIgnoreCase("store")) {
				if(c.length > 1)
					store(c);
				else
					System.err.println(" parameters does not correspond to command " + operation +"/n");
			
			}else if (operation.equalsIgnoreCase("download")){
				if(c.length == 3)
					download(c[1], c[2] );
				else
				System.err.println("parameters does not correspond to command " + operation +"/n");
					
			}	
			System.out.println(in.readObject());
	
		}
		
		sc.close();
		
	}
	/**
	 * Metodo responsavel para mandar os conteudo dos ficheiro,uma vez que o pedido do client é store
	 * @param opFiles liha comando (nome dos ficheiros)
	 * @throws IOException
	 */
	private static void store(String[] opFiles) throws IOException {
		for (int i = 1; i < opFiles.length; i++) {
			File file = new File(opFiles[i]);
			if(file.exists()) {
				
			
					byte [] byteArray= new byte[(int) file.length()];
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
					
					bis.read(byteArray, 0, byteArray.length);
					out.write(byteArray, 0, byteArray.length);
					bis.close();
				
				
				
			} else {
				
				System.out.println("File " + file + " does not exists.");
			}
			
			out.flush();
				
		}
	}
	
	/**
	 * Metodo responsavel para mandar os conteudo dos ficheiro,uma vez que o pedido do client é download
	 * @param userID nome do utilizador
	 * @param file nome do ficheiro
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void download(String userID, String file) throws FileNotFoundException, IOException {
		if(in.readBoolean()) {
			File f = new File(file);
			
			byte [] byteArray = new byte[ 1024 ];				
			FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			
			int bytesRead = in.read(byteArray, 0, byteArray.length);
			
			bos.write(byteArray, 0, bytesRead);
			bos.close();

			System.out.println("File " + file + " added successufully. \n");
			
		}else
			System.out.println(userID + " does not have that file" );
	}
	
}

