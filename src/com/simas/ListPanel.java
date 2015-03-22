package com.simas;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * Created by Simas Abramovas on 2015 Mar 22.
 */

public class ListPanel extends BasePanel implements MouseListener {

	private static final int MAX_VISIBLE_ROWS = 10;
	private JList<IMAP.Item> mList;
	private ArrayList<IMAP.Item> mItems = new ArrayList<>();

	/**
	 * Creates a Card and it's components
	 */
	public ListPanel(JFrame frame) {
		super(frame, MainFrame.Card.LIST.name());
		addComponents();
	}

	/**
	 * Initializes this card's components
	 */
	public void addComponents() {
		setLayout(new BorderLayout());

		//Create the list and put it in a scroll pane.
		mList = new JList<>();
		mList.addMouseListener(this);
		setMultiSelectable(false);
		mList.setSelectedIndex(0);
		mList.setVisibleRowCount(MAX_VISIBLE_ROWS);

		JScrollPane scrollPane = new JScrollPane(mList);
		scrollPane.setWheelScrollingEnabled(true);
		scrollPane.createVerticalScrollBar();
		add(scrollPane);
	}

	public void setMultiSelectable(boolean multiSelectable) {
		if (multiSelectable) {
			mList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else {
			mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
	}

	public void populateList(ArrayList<IMAP.Item> items) {
		mItems = items;
		ItemListModel model = new ItemListModel();
		for (IMAP.Item item : mItems) {
			model.addElement(item);
		}
		mList.setModel(model);
	}

	public void navigateUp() {
		IMAP imap = getFrame().imap;
		if (!imap.getLastPath().isEmpty()) {
			// Navigate up the path
			String parent = "";
			int lastSeparator = imap.getLastPath().lastIndexOf('/');
			// If a separator exists cut the path up to it
			if (lastSeparator != -1) {
				parent = imap.getLastPath().substring(0, lastSeparator);
			}
			// Close the selected mailbox
			if (imap.isSelected()) imap.close();

			populateList(imap.list(parent));
		} else {
			// Already at root. Do nothing
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// Double click
		if (e.getClickCount() == 2) {
			JList list = (JList) e.getSource();
			int index = list.locationToIndex(e.getPoint());

			// First list item = navigator
			if (index == 0) {
				navigateUp();
				return;
			} else {
				--index;
			}

			IMAP.Item item = mItems.get(index);
			if (item instanceof IMAP.Mailbox) {
				IMAP.Mailbox mailbox = (IMAP.Mailbox) item;
				if (mailbox.hasChildren) {
					// Show sub-folders
					populateList(getFrame().imap.list(mailbox.path));
				} else {
					System.out.println(mailbox.path);
					getFrame().imap.select(mailbox);
					if (mailbox.messageCount > 0) {
						populateList(getFrame().imap.fetchMessages(1,
								Math.min(mList.getVisibleRowCount(), mailbox.messageCount)));
					} else {
						populateList(new ArrayList<>());
					}
				}
			} else {
				IMAP.Message message = (IMAP.Message) item;
				// Item is a message, open it in a dialog
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}


	private class ItemListModel extends DefaultListModel<IMAP.Item> {

		public ItemListModel() {
			add(0, new IMAP.Mailbox("..", "..", true));
		}

	}

}