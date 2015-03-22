package com.simas;

import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * Created by Simas Abramovas on 2015 Mar 19.
 */

public class MessagePanel extends BasePanel {

	private JList<String> mMessageList;
	private ArrayList<String> mMessages = new ArrayList<String>();

	/**
	 * Creates a Card and it's components
	 */
	public MessagePanel(JFrame frame) {
		super(frame, MainFrame.Card.MESSAGES.name());
		addComponents();
	}

	/**
	 * Initializes this card's components
	 */
	public void addComponents() {
		setLayout(new BorderLayout());

		//Create the list and put it in a scroll pane.
		mMessageList = new JList<String>();
		mMessageList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mMessageList.setSelectedIndex(0);
		mMessageList.setVisibleRowCount(10);

		JScrollPane scrollPane = new JScrollPane(mMessageList);
		scrollPane.setWheelScrollingEnabled(true);
		scrollPane.createVerticalScrollBar();
		add(scrollPane);
	}

	public void populateList() {
		DefaultListModel<String> model = new DefaultListModel<>();
		for (String message : mMessages) {
			model.addElement(message);
		}
	}

	private void fetchMail() {
		// ToDo mail fetching
		// Same as populateList?
	}

}
