package com.simas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Simas Abramovas on 2015 Mar 19.
 */

public class IMAP {

	private static final String RESULT_NO = "NO";
	private static final String RESULT_BAD = "BAD";
	private static final int PORT = 993;
	private SSLSocket mSocket;

	public IMAP(String host) throws IOException {
		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		mSocket = (SSLSocket) factory.createSocket(host, PORT);
		System.out.println("S:\t" + readLineFromSocket());
//		System.out.println(readLineFromSocket());
//		System.out.println("done");
//
//		mSocket.startHandshake();
//		System.out.println("Handshaking Complete");

/**
 * Retrieve the server's certificate chain
 *
 * Returns the identity of the peer which was established as part of
 * defining the session. Note: This method can be used only when using
 * certificate-based cipher suites; using it with non-certificate-based
 * cipher suites, such as Kerberos, will throw an
 * SSLPeerUnverifiedException.
 *
 *
 * Returns: an ordered array of peer certificates, with the peer's own
 * certificate first followed by any certificate authorities.
 */
//		Certificate[] serverCerts = mSocket.getSession().getPeerCertificates();
//		System.out.println("Retreived Server's Certificate Chain");
//
//		System.out.println(serverCerts.length + "Certifcates Found\n\n\n");
//		for (int i = 0; i < serverCerts.length; i++) {
//			Certificate myCert = serverCerts[i];
//			System.out.println("====Certificate:" + (i+1) + "====");
//			System.out.println("-Public Key-\n" + myCert.getPublicKey());
//			System.out.println("-Certificate Type-\n " + myCert.getType());
//
//			System.out.println();
//		}

//		mSocket.close();
	}

	public boolean authenticate(String username, char[] password) {
		final String cmd = String.format("A1 LOGIN %s %s", username, String.valueOf(password));
		return runCommand(cmd) != null;
	}

	public String list() {
		final String cmd = "A2 LIST \"\" \"*\"";
		return runCommand(cmd);
	}

	/**
	 * Sends the command via the socket and fetches the result. Appends {@code \r} to each
	 * command as it's not implied on UNIX systems
	 * @param cmd    command to be sent via the socket
	 * @return null if command failed
	 */
	private String runCommand(final String cmd) {
		System.out.println("C:\t" + cmd);
		// Append \r (required on UNIX systems)
		String command = cmd + '\r';
		try {
			// Invoke cmd
			writeViaSocket(command);
			// Read return cmd
			String returnCmd = readLineFromSocket();
			System.out.println("S:\t" + returnCmd);
			return parseReturnCommand(cmd, returnCmd);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Checks the integrity of the returned command:
	 * - Makes sure that the command numbers are equal
	 * - Makes sure the response doesn't begin with a {@code RESULT_NO} identifier
	 * @param cmd         command that was executed
	 * @param response    the resulting command that was returned by the server
	 * @return {@code null} if either of the given commands are invalid
	 */
	private String parseReturnCommand(String cmd, String response) {
		// If either command is empty, return null
		if (cmd == null || cmd.length() == 0 || response == null || response.length() == 0) {
			return null;
		}
		String cmdNum = cmd.substring(0, cmd.indexOf(' '));
		String[] responseSplit = response.split("\\s+");

		/*if (responseSplit.length == 0 || !cmdNum.equals(responseSplit[0])) {
			System.out.println(String
					.format("Error! Command numbers do not match: '%s' and '%s'", cmd, response));
			return null;
		} else */if (responseSplit.length < 2 ||
				responseSplit[1].equalsIgnoreCase(RESULT_NO) ||
				responseSplit[1].equalsIgnoreCase(RESULT_BAD)) {
			System.out.println("Error! Unexpected result command: " + response);
			return null;
		}

		return response;
	}

	private String readLineFromSocket() throws IOException {
//		InputStreamReader isr = ;
		BufferedReader reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
		String string = reader.readLine();
		return (string != null) ? string : null;
	}

	private void writeViaSocket(String cmd) throws IOException {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream()));
//		BufferedWriter w = new BufferedWriter
		writer.println(cmd);
		writer.flush();
	}

}
