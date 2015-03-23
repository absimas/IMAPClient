package com.simas;

import com.sun.istack.internal.NotNull;

import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * Created by Simas Abramovas on 2015 Mar 19.
 */

class MessageDialog extends JDialog {

	private static final String DELETE = "Delete";
	private static final String SUBJECT = "Subject: ";
	private static final String FROM = "From: ";
	private static final String DATE = "Date: ";
	private IMAP.Message mMessage;
	private String mParentPath;

	public MessageDialog(MainFrame mainFrame, @NotNull IMAP.Message message) {
		super(mainFrame, true);
		if (message == null) {
			throw new IllegalArgumentException("Message mustn't be null when creating the dialog!");
		}
		mMessage = message;
		mParentPath = mainFrame.imap.getLastPath();
		initDialog();
	}

	private void initDialog() {
		// Components
		JButton deletionButton = new JButton(DELETE);
		deletionButton.addActionListener(e -> new Thread(() -> {
			MainFrame mainFrame = ((MainFrame) getParent());
			mainFrame.setProgressing(true);

			// Delete the message from mailbox
			mainFrame.imap.delete(mMessage);
			// If still on the same path, reload the messages
			if (mainFrame.imap.getSelection().path.equals(mParentPath)) {
				ListPanel list = (ListPanel) mainFrame.findCard(MainFrame.Card.LIST);
				list.updateMessages();
			}
			// Close the dialog
			dispose();
			mainFrame.setProgressing(false);
		}).start());

		JLabel fromLabel = new JLabel(mMessage.from);
		JLabel dateLabel = new JLabel(mMessage.date);
		JLabel subjectLabel = new JLabel(mMessage.subject);
		JTextPane textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPane.setEditable(false);
		textPane.setText(mMessage.text);


		/* Component arrangement */
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));

		// From
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		panel.add(new JLabel(FROM));
		panel.add(fromLabel);
		mainPane.add(panel);

		// Date
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		panel.add(new JLabel(DATE));
		panel.add(dateLabel);
		mainPane.add(panel);

		// Subject
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		panel.add(new JLabel(SUBJECT));
		panel.add(subjectLabel);
		mainPane.add(panel);

		// Body (scrollable)
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		panel.add(new JScrollPane(textPane));
		mainPane.add(panel);

		// Delete button
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		panel.add(deletionButton);
		mainPane.add(panel);

		// Add content
		mainPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		getContentPane().add(mainPane);

		// Fit content, center, show
		MainFrame.resizeToFitInScreen(this, (double) 2/5);
		setLocationRelativeTo(getParent());
		setLocation(getX() + 20, getY() + 20);
		setVisible(true);
	}

}