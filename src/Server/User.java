package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
 
class User {
	private final String name;
	private String password;
	private ArrayList<String> friends;
	private ArrayList<String> msgs;
	private ArrayList<File> files;
	
	/**
	 * metodo que inicializa o tipo User com todas as suas funções
	 * @param name nome do utilizador
	 * @param pass password do utilizador (versão que não tem metodo para mudar)
	 */
	protected User(String name, String pass) {
		this.name = name;
		this.password = pass;
		this.friends = new ArrayList<>();
		this.msgs = new ArrayList<>();
		this.files = new ArrayList<>();
		
		//creat dir with user name se nao exist
		File dir = new File("Clients/" + name);
		if (!dir.exists()) {
		    System.out.print("creating directory for " + dir.getName() + "... \n");
		    

		    try{
		        dir.mkdir();
		        System.out.println("* DIR created *");  
		    } 
		    catch(SecurityException se){
		        System.err.println(" mkdir failed ");
		    }        
		  
		}
		
		//preenche listaFile com file que exist no diretoria do user
		File local = new File ("Clients/" + name);
		File[] files = local.listFiles();
		
	
		for (File file : files) {
			this.files.add(file);
		}
		
		
	}
	
	/**
	 * Metodo responsável para retirar todas as informaçõe da amizade e da caixa
	 * de mensagem que estao guardados no dir private
	 */
	protected void populate() {
		
		try {
			//populate friends list with FriendList information
			File friend = new File("Private/" + name + "++listFriend++");
			FileReader fileReader = new FileReader(friend);
			BufferedReader fr = new BufferedReader(fileReader);
			String line;
			
			while((line = fr.readLine()) != null) { 
				String[] friendList = line.split(" ");
				for (String l : friendList) {
					friends.add(l);
				}
				
			}
			fr.close();
			
			//populate msgs with msgs in the msgCase
			File msg = new File("Private/" + name + "++msgCase++");
			FileReader mReader = new FileReader(msg);
			BufferedReader mr = new BufferedReader(mReader);
			String lineM;
			
			while((lineM = mr.readLine()) != null) { 
				msgs.add(lineM);
			}
			mr.close();
			
		}catch (FileNotFoundException e) {
			e.getStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

		}	


		
		
	}
	
	/**
	 * Metodo que indica se um dado user (friend) é/ não o amigo do this
	 * @param friend nome do amigo que quer saber se é amigo do this
	 * @return true se friend é amigo do this
	 * 		   false se não é amigo
	 */
	protected boolean isFriend(String friend) {
		return this.friends.contains(friend);
	}
	
	/**
	 * metodo que verifica o password inserito seja correto do user ou não
	 * @param pass password
	 * @return true password correto
	 * 		   false password incorreto
	 */
	protected boolean tryLogin(String pass) {
		return password.equals(pass);
	}
	
	/**
	 * Metodo que guarda a file na lista de file
	 * @param file ficheiro para guardar
	 * @return true se o ficheiro foi guardado com successo
	 * 		   false caso contrario
	 */
	protected boolean storeFiles(File file ) {
		
			if(! this.files.contains(file)) {
				this.files.add(file);
				return true;
								
			}else
				return false;
	}

	/**
	 * metodo que lista todos os ficheiro quer o cliente user(this)
	 * tem na sua pasta do drive
	 * @return String que lista os nomes dos ficheiros
	 */
	protected String listFiles() {
		StringBuilder fileNames = new StringBuilder();
		
		for (File f : files) {
			fileNames.append("File: " + f.getName() + "\n");
		}
		
		if (fileNames.toString().equals("")) {
			fileNames.append("The client has no files stored.");
		}
		return fileNames.toString();
	}

	/**
	 * Metodo que dado uma sequencia dos nomes dos ficheiros, 
	 * verifica se o tal existe na pasta do utilizador
	 * e remove caso existe
	 * @param f lista dos ficheiros
	 * @return string que descreve a situação do remove para cada ficheiro
	 */
	protected String removeFilesLocaly(ArrayList<String> f) {
		StringBuilder sb = new StringBuilder();
		for (String fileName : f) {
			
			File name = new File(fileName);
			if(files.contains(name)) {
			
				if(name.exists() && name.delete()){
					sb.append("File: " + fileName + " successfully removed" );
				} else if(!name.exists()){
					sb.append("Delete File " + fileName + " failed." );
				}
					
			}else
				sb.append("File: " + fileName+ " does not exist");
		}
		return sb.toString();
	}
	
	/**
	 * Metodo que dado um nome de um utilizado, adiciona na lista dos amigos
	 * caso este ainda não está
	 * @param friendName nome do utilizador que quee adicionar como amigo
	 * @return true caso sucesso
	 *  	   false caso o amigo já existe na lista dos amigos
	 */
	protected boolean addTrustedFriend(String friendName) {
		if(friends.contains(friendName)) {
			System.err.println("User already added in user friend list");
			return false;
		} else {
			friends.add(friendName);
			System.out.println( this.name + " added " + friendName+ " as a Friend successfully");
			return true;
		}
	}
	
	/**
	 * MEtodo que dado um nome de um utilizador, remove da lista dos amigos do this,
	 * caso este existe
	 * @param friendName nome do utilizador que quer remover como amigo
	 * @return true remoção de amizade com suceso
	 * 		   false caso contrario
	 */
	protected boolean removeTrustedFriend(String friendName) {
		if(!friends.contains(friendName)) {
			System.err.println("User does not exist in user friend list");
			return false;
		} else {
			friends.remove(friendName);
			System.out.println(this.name + " removed " + friendName + " as a Friend successfully");
			return true;
		}
	}

	/**
	
	 * Download a file from a friend 
	 * @param fileName the path to the file to download
	 * @param friendName userName to the friend that have the file to be downloaded
	 * @return false if the FriendName is not a friend, and true is the download was successful
	 */
	protected boolean downloadFile(String fileName, User friendName) {

		if (friends.contains(friendName) ) {

			try {
				File f = new File( friendName.name + "/" + fileName);
				f.createNewFile();
				Path source = Paths.get( name + "/" + fileName);
				Path dest = Paths.get( friendName.name + "/" + fileName);
				if(f.exists()) {
					if(f.delete())
						System.out.println("File " + fileName + " will replaced with " + name + "'s version for " + friendName.name);
				}
				Files.copy(source, dest);
			} catch (FileAlreadyExistsException e1) {
				
			} catch (IOException e1) {
				e1.printStackTrace();
			} 
			return true;
		}
		return false;
	}

	/**
	 * Stores a messages from a friend in the user mail box
	 * @param friendID the friend that sends the message
	 * @param msg the content of the message
	 * @return An acknowledge message
	 */
	protected String msg(String friendID, String msg) {
		StringBuilder sb = new StringBuilder(friendID + ": ");
		sb.append(msg +"\n");
		msgs.add(sb.toString());
		
		return "Message sent";
	}
	
	/**
	 * Collects this users messages from his mail box
	 * @return A String with is messages and, a mesage if he does'nt have more messages
	 */
	protected String collect() {
		StringBuilder sb = new StringBuilder();
		if(!msgs.isEmpty() ) {
			sb.append("Your Messages\n");
			for (String m : msgs) {
				sb.append(m + "\n");
			}
		
		msgs = new ArrayList<>();
		}else {
			sb.append("You have no Messages \n");
		}
		return sb.toString();
	}

	/**
	 * Do the precistence in the server when a client exist.
	 */
	protected void persistence() {
		try {
			//save friend list
			File friend = new File("Private/" + name + "++listFriend++");
			if (friend.exists()) {
				friend.delete();
			}
			friend.createNewFile();
			FileWriter fileWriter = new FileWriter(friend,true);
			BufferedWriter fw = new BufferedWriter(fileWriter);
			
			for (String f : friends) {
				fw.append(f + " ");
			}
			
			fw.close();
			
			//save msgCase
			File msg = new File("Private/" + name + "++msgCase++");
			if (msg.exists()) {
				msg.delete();	
			}
			msg.createNewFile();
			FileWriter msgWriter = new FileWriter(msg,true);
			BufferedWriter mw = new BufferedWriter(msgWriter);
			
			for (String m : msgs) {
				mw.append(m + " ");
			}
			
			mw.close();	
			
			
		} catch (IOException e) {
			e.printStackTrace();

		}	

		
	}

	/**
	 * Check is this user has the file
	 * @param file
	 * @return true if he has it, false if he doesn't 
	 */
	protected boolean hasFile(File file) {
		return files.contains(file);
	}

}
