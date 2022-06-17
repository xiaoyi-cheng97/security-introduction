package Server.Manager;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * Responsavel pela manipulcacao do ficheiro logIn
 * 
 * premite adicionar, remover e alterar passes de users
 * 
 * @author 49525, Mariana Santos
 * @author 49446, Xiao Yi Cheng
 * @author 49022, Ines Lobo
 *
 */
public class AdminHandler {

	/**
	 * Insntancia singleton
	 */
	private static AdminHandler instance = null;

	/**
	 * Construtroi uma instancia de UserManager
	 */
	private AdminHandler() {
	}

	/**
	 * @return instancia da class UserManager
	 */
	public static AdminHandler getInstance() {
		if (instance == null)
			instance = new AdminHandler();
		return instance;
	}

	/**
	 * Encripta uma palavra pass usando o algoritmo PBKDF2 e o salt.
	 * 
	 * @param pass
	 *            password em texto corrente
	 * @param salt
	 *            salt da password
	 * @return password encriptada
	 * @throws NoSuchAlgorithmException
	 *             Se o algoritmo PBKDF2 nao existe
	 * @throws InvalidKeySpecException
	 *             se a expecificacao da chave for invalida
	 * @see https://www.javacodegeeks.com/2012/05/secure-password-storage-donts-dos-and.html
	 */
	private byte[] encriptaPass(String pass, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// PBKDF2 with SHA-1 as the hashing algorithm.
		String algoritmo = "PBKDF2WithHmacSHA1";
		// SHA-1 generates 160 bit hashes
		int tamanhoChaveEnc = 160;
		// The NIST recommends at least 1,000 iteration
		int it = 20;

		KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt, it, tamanhoChaveEnc);

		SecretKeyFactory fab = SecretKeyFactory.getInstance(algoritmo);

		return fab.generateSecret(spec).getEncoded();
	}

	/**
	 * Cria um salt para mapear a password
	 * 
	 * @return o salt
	 * @throws NoSuchAlgorithmException
	 *             Se o algoritmo para o secure random nao for válido
	 */
	private byte[] geraSalt() throws NoSuchAlgorithmException {
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[1];
		random.nextBytes(salt);

		return salt;
	}
	
	/**
	 * Verifica a existencia de um user
	 * 
	 * @param username
	 * @return se o user existe devolve a linha com as suas credenciais, caso
	 *         contrario null,
	 * @throws IOException
	 *             Se o ficheiro de logIn for invalido
	 */
	private String existeUser(String username) throws IOException {

		boolean found = false;
		File logIn = new File("Private/LogIn.txt");
		FileReader file = new FileReader(logIn);
		BufferedReader fr = new BufferedReader(file);
		String line = null;

		while (!found && (line = fr.readLine()) != null) {
			if (username.equals(line.split(":")[0]))
				found = true;
		}

		return line;
	}

	/**
	 * Adiciona um user ao sistema(ao mapa de users)
	 * 
	 * @param username
	 *            username
	 * @param password
	 *            password
	 * @return false se ja existe um utizador com esse username, true se fou
	 *         adicionado com sucesso
	 * @throws Exception
	 *             justificacao do erro
	 */
	public boolean adicionaUser(String username, String password) throws Exception {
		File logIn = new File("Private/LogIn.txt");

		try {

			FileWriter fileWriter = new FileWriter(logIn, true);
			BufferedWriter fr = new BufferedWriter(fileWriter);

			if (existeUser(username) != null)
				throw new Exception("Username já usado, impossivel inserir 2 useres com o mesmo username");

			byte[] salt = geraSalt();
			String passEnc = DatatypeConverter.printBase64Binary(encriptaPass(password, salt));

			fr.write(username + ":" + DatatypeConverter.printBase64Binary(salt) + ":" + passEnc + "\n");
			fr.close();
			return true;
		} catch (NoSuchAlgorithmException e) {
			System.err.println("O algoritmo de encriptacao, da factory nao e valido- adicionar user");

		} catch (InvalidKeySpecException e) {
			System.err.println("Especificacao da key para a fabrica nao e valida- adicionar user");

		} catch (IOException e) {
			System.err.println("Erro ao aceder a ficheiros- adicionar user");

		}
		return false;

	}

	

	/**
	 * Altera a password do user (no mapa de users)
	 * 
	 * @param user
	 *            nome do user que quer alterar a pass
	 * @param novaPass
	 *            nova password escolhida
	 * @requires o user tem de estar autenticado
	 * @return true se a alteracao foi feita com sucesso, falso caso contrario
	 * @throws Exception
	 *             justificacao do erro
	 */
	public boolean alteraPassUser(String user, String novaPass) throws Exception {
		try {
			boolean alterado = false;
			String line;
			File log = new File("Private/LogIn.txt");
			FileReader reader = new FileReader(log);
			BufferedReader fr = new BufferedReader(reader);
			StringBuilder sb = new StringBuilder();

			while ((line = fr.readLine()) != null) {
				String[] credenciais = line.split(":");
				if (user.equals(credenciais[0])) {
					alterado = true;
					byte[] salt = DatatypeConverter.parseBase64Binary(credenciais[1]);
					String novaPassEnc = DatatypeConverter.printBase64Binary(encriptaPass(novaPass, salt));
					sb.append(user + ":" + credenciais[1] + ":" + novaPassEnc);
				} else {
					sb.append(line);

				}
			}
			fr.close();
			reader.close();

			FileWriter writer = new FileWriter(log);
			BufferedWriter fw = new BufferedWriter(writer);
			fw.write(sb.toString());
			fw.close();
			if (!alterado)
				throw new Exception("Pass nao alterada: User nao existe");
			return alterado;

		} catch (NoSuchAlgorithmException e) {
			System.err.println("O algoritmo de encriptacao, da factory nao e valido- altera pass");

		} catch (InvalidKeySpecException e) {
			System.err.println("Especificacao da key para a fabrica nao e valida- altera pass");

		} catch (IOException e) {
			System.err.println("Erro ao aceder a ficheiros- altera pass");

		}
		return false;
	}

	/**
	 * Remove um utilizador com esse username
	 * 
	 * @param username
	 *            nome do user a remover
	 * @return true se foi removido corretamente, false caso contrario
	 * @throws Exception
	 *             justificacao do erro
	 */
	public boolean removeUser(String username) throws Exception {

		StringBuilder sb = new StringBuilder();
		String line;
		File log = new File("Private/LogIn.txt");
		FileReader reader = new FileReader(log);
		BufferedReader fr = new BufferedReader(reader);

		long inalterdoSize = log.length();
		try {

			while ((line = fr.readLine()) != null) {
				if (!username.equals(line.split(":")[0])) {
					sb.append(line);
				}
			}
			fr.close();
			reader.close();

			FileWriter writer = new FileWriter(log);
			BufferedWriter fw = new BufferedWriter(writer);
			fw.write(sb.toString());

			writer.close();
			fw.close();
			if (inalterdoSize == log.length())
				throw new Exception("User não foi removido: User não exste.");

		} catch (Exception e) {
			System.err.println(e.getMessage());

		}
		return inalterdoSize > log.length();
	}

}
