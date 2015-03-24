package com.simas;

import com.sun.istack.internal.NotNull;
import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Simas Abramovas on 2015 Mar 19.
 */

public class IMAP {

	private static final String RESPONSE_NO = "NO";
	private static final String RESPONSE_BAD = "BAD";
	private static final String COMMAND_PREFIX = "?";
	private static final String HAS_CHILDREN_FLAG = "\\HasChildren";
	private static final String RESPONSE_SUBJECT = "Subject: ";
	private static final String RESPONSE_FROM = "From: ";
	private static final String RESPONSE_DATE = "Date: ";
	private static final int PORT = 993;
	private final BufferedWriter mWriter;
	private final BufferedReader mReader;
	private final Socket mSocket;
	private String mLastPath = "";
	private Mailbox mSelection;

	public IMAP(String host) throws IOException {
		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		mSocket = factory.createSocket(host, PORT);
		mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
		mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
		System.out.println("S:\t" + readLine());


		// Handshake and certificate list
//		System.out.println(readLine());
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
	}

	public boolean login(String username, char[] password) {
		final String cmd = String.format("LOGIN %s %s", username, String.valueOf(password));
		return runCommand(cmd) != null;
	}

	public boolean logout() {
		final String cmd = "LOGOUT";
		return runCommand(cmd) != null;
	}

	public boolean exit() {
		try {
			mSocket.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public ArrayList<Item> list(String mailboxPath) {
		String cmd;
		if (mailboxPath == null || mailboxPath.isEmpty()) {
			// Root folders
			cmd = "LIST \"\" %";
		} else {
			// Sub folders or folder content
			cmd = String.format("LIST \"%s\" *", mailboxPath);
		}

		String response = runCommand(cmd);
		ArrayList<Item> list = new ArrayList<>();
		if (response == null) return list;

		// Split response into separate lines
		String[] responseLines = response.split("\n");
		for (String line : responseLines) {
			// Parse the folder name
			String[] lineArgs = line.split("(?<!\\\\)\"");
			if (lineArgs.length == 0) continue;
			String folderPath = lineArgs[lineArgs.length-1];
			String folderName = folderPath;
			if (mailboxPath != null && !mailboxPath.isEmpty()) {
				folderName = folderPath.replace(mailboxPath + '/', "");
			}

			// Parse hasChildren flag
			boolean hasChildren = false;
			if (line.contains(HAS_CHILDREN_FLAG)) hasChildren = true;

			// Add to array
			list.add(new Mailbox(folderName, folderPath, hasChildren));
		}

		mLastPath = mailboxPath;
		return list;
	}

	public boolean select(Mailbox mailbox) {
		final String cmd = String.format("SELECT \"%s\"", mailbox.path);
		String response = runCommand(cmd);
		if (response == null) return false;

		// Split response into separate lines
		String[] responseLines = response.split("\n");
		// Find the line containing message count
		for (String line : responseLines) {
			if (line.endsWith("EXISTS")) {
				// Parse the message count and save it
				String[] lineSplit = line.split("\\s+");
				int count = 0;
				try {
					count = Integer.valueOf(lineSplit[1]);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				mailbox.messageCount = count;
				break;
			}
		}

		mLastPath = mailbox.path;
		mSelection = mailbox;
		return true;
	}

	public boolean close() {
		final String cmd = "CLOSE";
		String response = runCommand(cmd);
		if (response == null) return false;
		mSelection = null;
		return true;
	}

	public Message fetchMessageContent(@NotNull Message msg) {
		if (msg == null || msg.id < 1) {
			throw new IllegalArgumentException("Message must be valid!");
		}
		// Fetch header fields (from, date)
		String cmd = String.format("FETCH %d (BODY[HEADER.FIELDS (from date)])", msg.id);
		String response = runCommand(cmd);
		if (response == null) return msg;

		// Parse header fields
		String[] responseSplit = response.split("\n");
		for (String line : responseSplit) {
			if (line.startsWith(RESPONSE_FROM)) {
				msg.from = line.replace(RESPONSE_FROM, "");
			} else if (line.startsWith(RESPONSE_DATE)) {
				msg.date = line.replace(RESPONSE_DATE, "");
			}
		}

		// Fetch message text
		cmd = String.format("FETCH %d BODY[1]", msg.id);
		response = runCommand(cmd);
		if (response == null) return msg;

		// Remove first response line
		String text;
		int nlIndex = response.indexOf('\n');
		if (nlIndex == -1) {
			System.out.println("First text line couldn't be removed:\n\n" + response);
			return msg;
		}
		response = response.substring(nlIndex + 1);
		// Remove last response line
		nlIndex = response.lastIndexOf('\n', response.length() - 2);
		if (nlIndex == -1) {
			System.out.println("Last text line couldn't be removed:\n\n" + response);
			return msg;
		}
		response = response.substring(0, nlIndex + 1);

		// Decode quoted-printable
		try {
			InputStream is = new ByteArrayInputStream(response.getBytes());
			InputStreamReader isr = new InputStreamReader(MimeUtility
					.decode(is, "quoted-printable"));
			BufferedReader reader = new BufferedReader(isr);
			String line, result = "";
			while ((line = reader.readLine()) != null) {
				result += line + "<br>";
			}
			msg.text = result;

		} catch (MessagingException | IOException e) {
			e.printStackTrace();
		}

		return msg;
	}

	/**
	 * Fetches an array of {@code Message}s whose subject (name) will be set
	 * @param from    from id. Must be higher than 0
	 * @param to      to id. Must be higher than {@code from}
	 * @return array of found message subjects
	 */
	public ArrayList<Item> fetchMessages(int from, int to) {
		if (from < 1 || to < from) {
			throw new IllegalArgumentException(String
					.format("Incorrect range given: [%d,%d]", from, to));
		}

		ArrayList<Item> messages = new ArrayList<>();
		// Fetch messages in requested range
		for (int i=from; i<=to; ++i) {
			final String cmd = String.format("FETCH %d (BODY[HEADER.FIELDS (subject)])", i);
			String response = runCommand(cmd);
			if (response == null) continue;

			String[] responseLines = response.split("\n");
			for (String line : responseLines) {
				// Find the correct response line
				if (line.startsWith(RESPONSE_SUBJECT)) {
					// Remove "Subject: " prefix
					String subject = line.replace(RESPONSE_SUBJECT, "");
					// Decode subject
					try {
						subject = MimeUtility.decodeText(subject);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					messages.add(new Message(subject, i));
					// No further lines are needed
					break;
				}
			}
		}

		return messages;
	}

	public boolean delete(@NotNull Message msg) {
		if (msg == null || msg.id < 1) {
			throw new IllegalArgumentException("Message must be valid!");
		}
		String cmd = String.format("STORE %d +flags.silent (\\Seen \\Deleted)", msg.id);
		return runCommand(cmd) != null;
	}

	/**
	 * Sends the command via the socket and fetches the result. Appends {@code \r} to each
	 * command as it's not implied on UNIX systems
	 * @param cmd    command to be sent via the socket
	 * @return null if command failed
	 */
	private String runCommand(final String cmd) {
		// Prepend a ? for debugging and append a \r which is required on UNIX systems
		String command = String.format("%s %s\r", COMMAND_PREFIX, cmd);
		// Log only safe commands
		if (!command.contains("? LOGIN")) {
			System.out.println("C:\t" + command);
		}

		try {
			// Invoke cmd
			writeViaSocket(command);
			// Read return cmd
			String line, response = "";
			while ((line = readLine()) != null) {
				// Log response line
				System.out.println("S:\t" + line);
				// End reader loop if response finished
				if (line.startsWith(COMMAND_PREFIX)) {
					break;
				}
				// Append to response string
				response += line + '\n';
			}
			// Return null if command failed
			if (isResponseValid(cmd, line)) {
				return response;
			} else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Checks the validity of the server response.
	 * @param response    the last line of the server response
	 * @param cmd         the command that was issued to the server
	 * @return false if the response is {@code RESPONSE_BAD} or {@code RESPONSE_NO}. Otherwise true./
	 */
	private boolean isResponseValid(String cmd, String response) {
		// If either command is empty, return null
		if (cmd == null || cmd.length() == 0 || response == null || response.length() == 0) {
			return false;
		}
		String[] responseSplit = response.split("\\s+");

		if (responseSplit.length < 2) {
			System.out.println("Error! Response too short: " + response);
			return false;
		} else if (responseSplit[1].equalsIgnoreCase(RESPONSE_NO)) {
			System.out.println("Error! Invalid response: " + response);
			return false;
		} else if (responseSplit[1].equalsIgnoreCase(RESPONSE_BAD)) {
			System.out.println("Error! Badly formed request: " + response);
			return false;
		} else {
			return true;
		}
	}

	private String readLine() throws IOException {
		return mReader.readLine();
	}

	private void writeViaSocket(String cmd) throws IOException {
		mWriter.write(cmd);
		mWriter.newLine();
		mWriter.flush();
	}

	public Mailbox getSelection() {
		return mSelection;
	}

	public String getLastPath() {
		// Make sure null is not returned
		return (mLastPath == null) ? "" : mLastPath;
	}

	/* Custom holder classes */
	public static class Item {

	}

	public static class Mailbox extends Item {
		public String name, path;
		public boolean hasChildren;
		public int messageCount;

		public Mailbox(String name, String path, boolean hasChildren) {
			this.name = name;
			this.path = path;
			this.hasChildren = hasChildren;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static class Message extends Item {
		public String subject, text, from, date;
		public int id;

		public Message(String subject, int id) {
			this.subject = subject;
			this.id = id;
		}

		@Override
		public String toString() {
			return subject;
		}
	}

}
