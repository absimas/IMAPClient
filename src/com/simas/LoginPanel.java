package com.simas;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


/**
 * Created by Simas Abramovas on 2015 Mar 19.
 */

public class LoginPanel extends BasePanel implements ActionListener {

	private static final String USERNAME = "Username";
	private static final String PASSWORD = "Password";
	private static final String HOST = "Host";

	private static final String LOGIN_FAILED = "Failed to login with given credentials. " +
			"Please try again.";
	private static final String CONNECTION_FAILED = "Failed to connect to given host. " +
			"Please try again.";

	private static final String HOST_REQUIRED = "Must enter a host name";
	private static final String USERNAME_REQUIRED = "Must enter a username";
	private static final String PASSWORD_REQUIRED = "Must enter a password";
	private static final String ERROR_TITLE = "Error!";

	private static final int MAX_USERNAME_LENGHT = 20;
	private static final int MAX_PASS_LENGTH = 20;

	private JTextField mHostField, mUserField;
	private JPasswordField mPassField;

	/**
	 * Creates a Card and it's components
	 */
	public LoginPanel(JFrame frame) {
		super(frame, MainFrame.Card.LOGIN.name());
		addComponents();
	}

	/**
	 * Initializes this card's components
	 */
	private void addComponents() {
		setLayout(new GridBagLayout());
//		setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

		// Constraints
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 20, 20, 0);
		gbc.fill = GridBagConstraints.NONE;

		// Server IP
		JLabel label = new JLabel(HOST);
		add(label, gbc);
		++gbc.gridx;
		mHostField = new JTextField(Credentials.HOST, 15);
		mHostField.addActionListener(this);
		add(mHostField, gbc);

		// Username
		++gbc.gridy;
		gbc.gridx = 0;
		label = new JLabel(USERNAME);
		add(label, gbc);
		++gbc.gridx;
		mUserField = new JTextField(Credentials.USERNAME, MAX_USERNAME_LENGHT);
		mUserField.addActionListener(this);
		add(mUserField, gbc);

		// Password
		++gbc.gridy;
		gbc.gridx = 0;
		label = new JLabel(PASSWORD);
		add(label, gbc);
		++gbc.gridx;
		mPassField = new JPasswordField(Credentials.PASSWORD, MAX_PASS_LENGTH);
		mPassField.setEchoChar('*');
		mPassField.addActionListener(this);
		add(mPassField, gbc);

		// Exit button
		gbc.gridy += 2;
		gbc.gridx += 3;
		JButton button = new JButton(MainFrame.LOGIN);
		button.addActionListener(this);
		add(button, gbc);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		// Validate fields
		if (mHostField.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, HOST_REQUIRED,
					ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
			return;
		} else if (mUserField.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, USERNAME_REQUIRED,
					ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
			return;
		} else if (mPassField.getPassword().length == 0) {
			JOptionPane.showMessageDialog(this, PASSWORD_REQUIRED,
					ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
			return;
		}

		new Thread(() -> {
			getFrame().setProgressing(true);
			try {
				IMAP imap = getFrame().imap = new IMAP(mHostField.getText());

				if (imap.login(mUserField.getText(), mPassField.getPassword())) {
					// Update the file menu
					getFrame().usePostAuthMenu();
					// Fetch the top folders
					ListPanel list = (ListPanel) getFrame().findCard(MainFrame.Card.LIST);
					list.populateList(getFrame().imap.list(""));
					// Show the list
					getFrame().showCard(MainFrame.Card.LIST);
				} else {
					JOptionPane.showMessageDialog(LoginPanel.this, LOGIN_FAILED,
							ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
				}
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(LoginPanel.this, CONNECTION_FAILED,
						ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
			}
			getFrame().setProgressing(false);
		}).start();
	}
}
