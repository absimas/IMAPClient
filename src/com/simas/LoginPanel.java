package com.simas;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;


/**
 * Created by Simas Abramovas on 2015 Mar 19.
 */

public class LoginPanel extends BasePanel {

	private static final String USERNAME = "Username";
	private static final String PASSWORD = "Password";
	private static final String SERVER_IP = "Server ip";

	private static final int MAX_USERNAME_LENGHT = 20;
	private static final int MAX_PASSWORD_LENGHT = 20;

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
		JLabel label = new JLabel(SERVER_IP);
		add(label, gbc);
		++gbc.gridx;
		JTextField textField = new JTextField("   .   .   .   ", 15); // ToDo force dots
		add(textField, gbc);

		// Username
		++gbc.gridy;
		gbc.gridx = 0;
		label = new JLabel(USERNAME);
		add(label, gbc);
		++gbc.gridx;
		textField = new JTextField(null, MAX_USERNAME_LENGHT);
		add(textField, gbc);

		// Username
		++gbc.gridy;
		gbc.gridx = 0;
		label = new JLabel(PASSWORD);
		add(label, gbc);
		++gbc.gridx;
		textField = new JTextField(null, MAX_PASSWORD_LENGHT);
		add(textField, gbc);

		// Exit button
		gbc.gridy += 2;
		gbc.gridx += 3;
		JButton button = new JButton(MainFrame.LOGIN);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});
		add(button, gbc);

	}

}
