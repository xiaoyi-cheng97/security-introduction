package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
 
public class ServerCommands {
	private Map<String, User> usersCatalog;
	
	/**
	 * cria lista dos Users presente no servidor, e retaura as informacoes
	 */
	protected ServerCommands() {
		usersCatalog = new HashMap<>();
		
		try {
			File loginNovo = new File("Private/LogIn.txt");
			FileReader fileReader = new FileReader(loginNovo);
			BufferedReader fr = new BufferedReader(fileReader);
			String line;
			
			while((line = fr.readLine()) != null) { 
				String[] credential = line.split(":");
				User u = new User(credential[0], credential[1]);
				usersCatalog.put(credential[0], u);
				
			}
			fr.close();
			System.out.println("User Table created - credentials log");
				
		}catch (FileNotFoundException e) {
			e.getStackTrace();
			System.err.println("LogIn file not found");
		} catch (IOException e) {
			e.printStackTrace();

		}	
		
		populate();
	}
	
	/**
	 * verifica seum dado user é ou não utilizador do servidor
	 * @param user nome do utilizador que quer validar
	 * @return true user é utilizador do servidor
	 * 		   false caso contrario
	 */
	protected boolean validUser(String user) {
		return usersCatalog.containsKey(user);
		
	}
	
	/**
	 * Regista um user como utilizador do servidor
	 * @param userName nome do utilizador
	 * @param password password do utilizador
	 */
	protected void registUser(String userName, String password) {
		User u = new User(userName, password);
		usersCatalog.put(userName, u);
	}
	
	/**
	 * Autentificação do user e o proprio password
	 * @param username	nome do utilizador
	 * @param password	password do utilizador
	 * @return 	true se o user e passestá autenticado
	 * 			false caso contrario
	 */
	protected boolean autentification(String username, String password) {
		return usersCatalog.containsKey(username) && usersCatalog.get(username).tryLogin(password);
	}

	
	/**
	 * Adiciona file no user
	 * @param localUser nome do user
	 * @param files ficheiro paraadicionar
	 * @return 	true adição com sucesso
	 * 			false caso contrario
	 */
	protected boolean addFiles(String localUser, File files) {
		return usersCatalog.get(localUser).storeFiles(files);
		
	}

	/**
	 * Devolve a lista dos nomes que o utilizador tem na sua dir do servidor
	 * @param userName nome do utilizador
	 * @return lista dos ficheiros
	 */
	protected String listFiles(String userName){
		return usersCatalog.get(userName).listFiles();
	}
	
	/**
	 * Remove uma lista dos ficheiro no dir do utilizador
	 * @param userName nome do utilizador
	 * @param files ficheiros para remover
	 * @return informações sobre a remoção dos ficheiros
	 */
	protected String removeFile(String userName, ArrayList<String> files) {
		return usersCatalog.get(userName).removeFilesLocaly(files);
	}
	
	/**
	 * devolce a lista dos utilizadores
	 * @return lista dos utilizadores
	 */
	protected String users(){
		StringBuilder sb = new StringBuilder();
		for (String u : usersCatalog.keySet()) {
			sb.append("Client: " + u + "\n");
		}
		return sb.toString();
	}
	
	/**
	 * Adicionar um user como amigo
	 * @param localUser nome do user local
	 * @param friendID nome do utilizador para adicionar como amigo
	 * @return string que descreve a situação
	 */
	protected String trustedUser(String localUser, String friendID) {
		
		if(usersCatalog.get(localUser).addTrustedFriend(friendID)) {
			usersCatalog.get(friendID).addTrustedFriend(localUser);
			
			return "User trusted.";
		}else 
			
		return "User already added in your friend list.";
	}
	
	/**
	 * Remover um user como amigo
	 * @param localUser nome do user local
	 * @param friendID nome do utilizador para remover como amigo
	 * @return string que descreve a situação da remoção
	 */
	protected String unTrustedUser(String localUser, String friendID) {
		
		
		if(usersCatalog.get(localUser).removeTrustedFriend(friendID)) {
			usersCatalog.get(friendID).removeTrustedFriend(localUser);
			return "User untrusted.";
		}
		
		return "User does not exist in your friend list";
	}
	
	/**
	 * Metodo que permite o utilizador local download o ficheiro do um seu amigo trusted
	 * @param localUser utilizador local
	 * @param friendID utilizador amigo
	 * @param fileName nome do ficheiro
	 * @return string que descreve o download
	 */
	protected String download(String localUser,String friendID , String fileName) {
		File f = null;
		StringBuilder sb = new StringBuilder();
		if( !localUser.equals(friendID) ) {
			 if(usersCatalog.get(localUser).downloadFile(fileName, usersCatalog.get(friendID)))
				 sb.append("Downloaded.");
			 else 
				 sb.append("Download failed: " + fileName + " does not exist or " + friendID + " is not your friend");
			
		}else {
			sb.append("Download failed: You can not download your own file.");
		}
		return sb.toString();
	}
	
	/**
	 * Metodo que permite o utilizador local manda uma mensagem ao amigo
	 * @param localUser utilizador local
	 * @param friendID utilizador amigo
	 * @param msg mansagem para mandar
	 * @return String que descreve a informação 
	 */
	protected String msg(String localUser, String friendID , String msg) {
		StringBuilder sb = new StringBuilder();
		if(localUser.equals(friendID))
			sb.append("You can not send messages to yourself");
		else if(!usersCatalog.containsKey(friendID))
			sb.append("Friend " + friendID + " is not registred");
		else if(!usersCatalog.get(friendID).isFriend(localUser))
			sb.append(friendID + " did not add you as a friend");
		else {
			sb.append(usersCatalog.get(friendID).msg(localUser, msg));
			System.out.println("From " + localUser + ": " + msg );
		}
		return sb.toString();
	}
	
	/**
	 * Metodo que retorna as mensagens do user
	 * @param localUser utilizador local
	 * @return a String com todas as mensagens
	 */
	protected String collect(String localUser) {
		return usersCatalog.get(localUser).collect();
	}
	
	/**
	 * Fucao para Presistencia do servidor 
	 */
	protected void presistence() {
		for (String user : usersCatalog.keySet()) {
			usersCatalog.get(user).persistence();
		}
	}
	
	/**
	 * Fucao para Presistencia do servidor 
	 */
	private void populate() {
		for (String user : usersCatalog.keySet()) {
			usersCatalog.get(user).populate();
		}
		
	}
	
	/**
	 * Verifica que um ficheiro existe ou nao na lista dos ficheiros do utilizador
	 * @param localUser nome do utilizador
	 * @param f ficheito para verificar
	 * @return true se nao existe
	 * 		   false caso existe
	 */
	protected boolean HasNotFiles(String localUser, File f) {
		return !usersCatalog.get(localUser).hasFile(f);
	}
}
