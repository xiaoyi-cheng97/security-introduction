package Server.Manager;

import java.io.*;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import jdk.nashorn.internal.runtime.ArgumentSetter;

/**
 * Manager que trabalha com acessos ao ficheiro logIn e com MAC's do
 * administrador
 * 
 * @author 49525, Mariana Santos
 * @author 49446,Xiao Yi Cheng
 * @author 49022, Inês Lobo
 *
 */
public class UserManager {

	public static void main(String[] args) {

		try {
			File logIn = new File("Private/LogIn.txt");
			File macFile = new File("Private/MAC.txt");

			if (validaMAC(args, logIn, macFile)) {
				System.out.println("Bem vindo Admin");
				AdminHandler admin = AdminHandler.getInstance();
				System.out.print(imprimeComandos());
				Scanner sc = new Scanner(System.in);
				String comando;

				while (!(comando = sc.nextLine()).equals("quit")) {
					try {
						System.out.println(correPedidos(comando, admin));
						atualizaMAC(args[1], logIn, macFile);
						System.out.print(imprimeComandos());
					} catch (Exception e) {
						System.err.println(e.getMessage());
					}
					if (!validaMAC(args, logIn, macFile))
						throw new Exception("MAC nao é valido, nao pode continuar a editar os users");
					System.out.print(imprimeComandos());
				}

				System.out.println("UserManager Terminado.");

			} else {
				throw new Exception("Validacao do Admin Falhou ");
			}
		} catch (InvalidKeyException e) {
			System.err.println(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e.getMessage());
		} catch (IllegalStateException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Verifica se o MAC esta de acordo com a pass do administrador e com o
	 * conteudo do ficheiro logIn.
	 * 
	 * caso o ficheiro macFile nao existe este é criado, e colocado um MAC
	 * calculado no instante. Se o ficheiro macFile estiver vazio é colocado um
	 * MAC
	 * 
	 * @param parametros
	 *            parametros recebidos, com username do administrador (tem de
	 *            ser Admin) e password, respetivamente
	 * @param logIn
	 *            ficheiro que contem as passwords
	 * @param macFile
	 *            ficheiro que contem o MAC para validacao
	 * @return true se o MAC gerado por este administrador ( parametros) é igual
	 *         ao que está guardado no ficheiro macFile. Caso contrario é
	 *         lancada a excepcao
	 * @throws Exception
	 *             justificacao do erro.
	 */
	private static boolean validaMAC(String[] parametros, File logIn, File macFile) throws Exception {
		// 2 argumentos admin e pass
		if (parametros.length < 2)
			throw new Exception("Terminado: Parametros em Falta");

		if (!parametros[0].equals("Admin"))
			throw new Exception("Terminado: Apenas e parmitido acesso ao admin ");

		// atualizaMAC(args[1], logIn, macFile);
		if (!logIn.exists())
			logIn.createNewFile();

		if (!macFile.exists()) { // Mac.txt nao existe
			System.err.println("O Ficheiro MAC.txt nao existe, esta a ser criado com o mac");

			if (!macFile.createNewFile()) // criar o mac file
				throw new Exception("Impossivel criar Ficheiro mac.txt");

			atualizaMAC(parametros[1], logIn, macFile);
			System.out.println("Ficheiro mac.txt criado e MAC calculado");

		} else { // o ficheiro mac.txt existe
			FileReader f = new FileReader(macFile);
			BufferedReader fr = new BufferedReader(f);
			String linha;

			if ((linha = fr.readLine()) == null) { // esta vazio
				System.err.println("Ficheiro mac.txt está vazio. A calcular o MAC");
				atualizaMAC(parametros[1], logIn, macFile);
				System.out.println("Mac calculado.");
			} else { // tem um mac
				String novoMac = criaMAC(parametros[1], logIn);

				if (!novoMac.equals(linha)) // mac n corresponde
					throw new Exception("Erro ao autentificar administrador, passwor errada gera invalidacao do MAC");
			}

		} // todos casos de erro tratados

		return true;

	}

	/**
	 * Interface que mostra quais os comando que pode executar
	 * 
	 * @return interface com nome dos comandos
	 */
	private static String imprimeComandos() {
		StringBuilder sb = new StringBuilder("Comandos disponiveis:\n\t");
		sb.append("Adiciona <Username> <Password>\n\t");
		sb.append("Remove <Username> \n\t");
		sb.append("AlteraPass <Username> <Password>\n\t");
		sb.append("quit \nInsira comando:\n>>>");
		return sb.toString();

	}

	/**
	 * Responsavel por avaliar o comando pedido e executar-lo
	 * 
	 * @param comando
	 *            linha escrita pelo administrador
	 * @param admin
	 *            handler que ira realizar as funcionalidades
	 * @return true se as funconalidades foram bem conseguidas, caso contraio
	 *         lanca uma excepcao a assinalar o erro
	 * @throws Exception
	 *             justificacao do erro
	 */
	private static String correPedidos(String comando, AdminHandler admin) throws Exception {
		String[] c = comando.split(" ");

		switch (c[0].toLowerCase()) {
		case "adiciona":

			if (c.length != 3)
				throw new Exception("Operacao nao realizada: Parametros inválidos");
			if (!admin.adicionaUser(c[1], c[2]))
				throw new Exception("");
			break;

		case "remove":

			if (c.length != 2)
				throw new Exception("Operacao nao realizada: Parametros inválidos");
			if (!admin.removeUser(c[1]))
				throw new Exception("");
			break;

		case "alterapass":

			if (c.length != 3)
				throw new Exception("Operacao nao realizada: Parametros inválidos");
			if (!admin.alteraPassUser(c[1], c[2]))
				throw new Exception("");

			break;

		default:
			throw new Exception("Operacao invalida\n");

		}

		return "Opercao feita com sucesso";

	}

	/**
	 * Cria um MAC com a password e com o conteudo do ficheiro LogIn e atualiza
	 * o valor do mac no ficheiro macFile
	 * 
	 * @param password
	 *            password da pessoa autorizada
	 * @param logIn
	 *            ficheiro de log in
	 * @param macFile
	 *            ficheiro que vai guardar o MAC
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	private static void atualizaMAC(String password, File logIn, File macFile)
			throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, IOException {
		// escreve mac no ficheiro Mac.txt
		FileWriter fm = new FileWriter(macFile);
		BufferedWriter frM = new BufferedWriter(fm);
		frM.write(criaMAC(password, logIn));
		frM.close();
		fm.close();
	}

	/**
	 * Cria um MAC com a password e com o conteudo do ficheiro LogIn
	 * 
	 * @param password
	 *            password da pessoa auturizada
	 * @param logIn
	 *            ficheiro com conteudo
	 * @return string do MAC
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws InvalidKeyException
	 */
	private static String criaMAC(String password, File logIn)
			throws NoSuchAlgorithmException, IllegalStateException, IOException, InvalidKeyException {
		// cria Mac
		byte[] pass = password.getBytes();
		SecretKey key = new SecretKeySpec(pass, "HmacSHA256");

		Mac m;
		byte[] mac = null;
		m = Mac.getInstance("HmacSHA256");
		m.init(key);
		m.update(Files.readAllBytes(logIn.toPath()));
		mac = m.doFinal();
		return Arrays.toString(mac);
	}

}
