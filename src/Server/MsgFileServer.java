package Server;
/***************************************************************************
*   Seguranca e Confiabilidade 2016/17
*
*
***************************************************************************/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.cert.CRLReason;
import java.util.*;

import Server.Manager.UserManager;

//Servidor myServer

public class MsgFileServer{
	 
	private ServerCommands serverCommands;
	
	public static void main(String[] args) throws IOException {
		
		System.out.println("servidor: main");
		
		int port = Integer.parseInt(args[0]);
		MsgFileServer server = new MsgFileServer();
		server.startServer(port);
	}
	
	
	/**
	 * Metodo que inicializa o servidor
	 * @param port
	 */
	public void startServer (int port){
		ServerSocket sSoc = null;
        
		try {
			sSoc = new ServerSocket(port);
			serverCommands = new ServerCommands();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
         
		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		}
		//sSoc.close();
	}



	//Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {

		private Socket socket = null;
		private ObjectInputStream in;
		private ObjectOutputStream out;

		ServerThread(Socket inSoc) {
			socket = inSoc;
		}
		/**
		 * Metodo que autentica um user e fica responsavel para atender o qual user 
		 */
		public void run(){
			try {
				out = new ObjectOutputStream(socket.getOutputStream()); //envia dados
				in = new ObjectInputStream(socket.getInputStream()); // recebe dados

				
				String user = client();
				String command = "quit";
				try {
					command = (String) in.readObject();
				
				}catch (IOException e) {
					System.out.println(user + " has no request. Disconecting");
				}
				
				while(!command.equals("quit")) {
					
					String anwser = clientRequests(command, user);
					out.writeObject( anwser );
					
					try {
						command = (String) in.readObject();
					} catch (IOException e) {
						command = "quit";
						System.out.println(user + " has no request. Disconecting");
					}
					
					

				}
							
				System.out.println(user + " is Disconected");
				serverCommands.presistence();
					
				out.close();
				in.close();
	 			
				socket.close();
				
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch ( IOException e){
				e.printStackTrace();
			}
	
		} 
		
		/**
		 * Metodo que valida um cient exist já ou não,regita caso nao exist
		 * autentifica caso já existe
		 * @return nome do utilizador
		 * @throws ClassNotFoundException
		 * @throws IOException
		 */
		private String client() throws ClassNotFoundException, IOException {
		
		String user = (String)in.readObject();
		String password = (String)in.readObject();	
			
			// verifica se user estah no LogIn
			if(!serverCommands.validUser(user)) { //se nao tiver adiciona
				serverCommands.registUser(user, password);
				addNewUser(user, password);
				System.out.println(user + " conected\n");
				out.writeObject(new Boolean(true));
				out.writeObject( menu());
			
			}else if(!serverCommands.autentification(user, password)) {
				//se tiver, e mandar pass errada , termina cliente
				out.writeObject(new Boolean(false));
				System.err.println("Incorrect Password from " + user);
	
			}else {
				System.out.print(user + " conected\n");
				
				out.writeObject(new Boolean(true)); // validacao do user 
				out.writeObject( menu());
			}
			
			return user;
	}
	
		/**
		 * Metodo que distingue o camando do utilizador que trabalha com ele
		 * @param command linha do comando
		 * @param localUser nome do utilizador
		 * @return string que descreve o resultado do comando
		 * @throws IOException
		 * @throws ClassNotFoundException
		 */
		private String clientRequests(String command, String localUser) throws IOException, ClassNotFoundException {
			String[] c = command.split(" ");
			String anwser = null;
			
			switch (c[0]) {
			case "store" :
				StringBuilder sb = new StringBuilder();
				if(c.length >= 2 ) {
					for (int i = 1; i < c.length; i++) {
						
						
								File f = new File("Clients/"+localUser + '/' + c[i]);
								
								if(serverCommands.HasNotFiles(localUser, f) ) {
										
										byte [] byteArray = new byte[ 1024 ];				
										FileOutputStream fos = new FileOutputStream(f);
										BufferedOutputStream bos = new BufferedOutputStream(fos);
										
										int bytesRead = in.read(byteArray, 0, byteArray.length);
										
										bos.write(byteArray, 0, bytesRead);
										bos.close();
										serverCommands.addFiles(localUser, f);
										sb.append("File " + c[i] + " added successufully. \n");
										
								}else {
									
									sb.append("File " + c[i] + " already exists. \n");
								}
							
					}
				
					sb.append("Store completed.");
				}else
				
					sb.append("Store failed");
				anwser = sb.toString();			
				break;
			case "list":
				anwser = serverCommands.listFiles(localUser);
				break;
			case "remove":
				ArrayList<String> filesToRemove = new ArrayList<String>();
		
				for (int i = 1; i < c.length; i++)
					filesToRemove.add("Clients/"+ localUser+ '/' + c[i]);
				
				anwser = serverCommands.removeFile(localUser, filesToRemove );
				break;
			case "users":
				anwser = serverCommands.users();
				break;
			case "trusted":
				anwser = serverCommands.trustedUser(localUser, c[1]);
				break;
			case "untrusted":
				anwser = serverCommands.unTrustedUser(localUser, c[1]);
				break;
			case "download":
				StringBuilder sb1 = new StringBuilder();
				
				if(c.length == 3) {
					String user = c[1];
					
					if(serverCommands.validUser(user) ) {
											
						File file = new File("Clients/"+user + "/" + c[2] );
						
						if(!serverCommands.HasNotFiles(user, file ) ) {
								
								out.writeBoolean(true);
								byte [] byteArray= new byte[(int) file.length()];
								BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
								
								bis.read(byteArray, 0, byteArray.length);
								out.write(byteArray, 0, byteArray.length);
								bis.close();
								serverCommands.addFiles(localUser, file);
								sb1.append("Sucess in download File");
						}else {
							out.writeBoolean(false);
							sb1.append("User " + user + " does not have the file");
						}
					}else {
						out.writeBoolean(false);
						sb1.append(user + " is not a valid user");
					}
				}else 
					sb1.append("Download Fail");
					
				anwser = sb1.toString();

				break;
			case "msg":
				StringBuilder sb2 = new StringBuilder();
				for (int i = 2; i < c.length; i++) {
					sb2.append(c[i] + " ");
				}
				anwser = serverCommands.msg(localUser, c[1], sb2.toString());
				break;
			case "collect":
				anwser = serverCommands.collect(localUser);
				break;

			default:
				anwser = "Does not exist the command " + c[0] + "\n" + menu();
				break;
			}
			return anwser;
		}

		/**
		 * Sets the menu for the client to choose his next action
		 * @return String list of commands 
		 */
		private String menu() {
			StringBuilder sb = new StringBuilder("Choose your commands: \n");
			sb.append("$ store <file> - to send files to the server \n");
			sb.append("$ list - to display your files \n" ) ;
			sb.append("$ remove <file> - to delete the file from server\n");
			sb.append("$ users - to list all the users in server\n");
			sb.append("$ trusted <trustedUserIDs> - add trusted user as a friend\n");
			sb.append("$ untrusted <untrustedUserIDs> - remove user as a friend\n");
			sb.append("$ download <userID> <file> - obtain file from server, saved in UserID account\n");
			sb.append("$ msg <userID> <msg> - send a mensage to UserID\n");
			sb.append("$ collect - receive all your mensages \n");
			sb.append("$ quit - exit \n");
			return sb.toString();
		}

		/**
		 * Add a new user to the logIn file, for further identification
		 * @param user New user name
		 * @param password new password
		 */
		private void addNewUser(String user, String password) {
			
			try {
				File loginNovo = new File("Private/LogIn.txt");
				FileWriter fileWriter = new FileWriter(loginNovo,true);
				BufferedWriter fw = new BufferedWriter(fileWriter);
				
				String line = user + ":" + password;
				
				fw.append("\n"+ line);
				fw.close();
				System.out.println("User add: user- " + user);
					
			}catch (FileNotFoundException e) {
				e.getStackTrace();
				System.err.println("LogIn file not found");
			} catch (IOException e) {
				e.printStackTrace();

			}	
		}
	}
				
		
}