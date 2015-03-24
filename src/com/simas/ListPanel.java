package com.simas;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * Created by Simas Abramovas on 2015 Mar 22.
 */

public class ListPanel extends BasePanel implements MouseListener {

	private static final int MAX_VISIBLE_ROWS = 8;
	private static final String PREV = "Previous";
	private static final String NEXT = "Next";
	private JList<IMAP.Item> mList;
	private ArrayList<IMAP.Item> mItems = new ArrayList<>();
	private JPanel mPaginationContainer;

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
//		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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

		// Create a pagination and hide it by default
		mPaginationContainer = new JPanel();
		JButton prevButton = new JButton(PREV);
		prevButton.addActionListener(e -> {
			try {
				IMAP.Message message = (IMAP.Message) mItems.get(0);
				if (message.id == 1) {
					// Already showing page 1
					return;
				}

				int page = (int) Math.ceil((double)message.id / mList.getVisibleRowCount()) - 1;
				int from = (page-1) * mList.getVisibleRowCount() + 1;
				int to = Math.min(from + mList.getVisibleRowCount(),
						getFrame().imap.getSelection().messageCount);
				populateList(getFrame().imap.fetchMessages(from, to));
			} catch (ClassCastException ignored) {}
		});
		prevButton.setSize(300, 200);
		JButton nextButton = new JButton(NEXT);
		nextButton.addActionListener(e -> {
			try {
				IMAP.Message lastMessage = (IMAP.Message) mItems.get(mItems.size()-1);
				if (getFrame().imap.getSelection().messageCount == lastMessage.id) {
					// No more pages
					return;
				}

				IMAP.Message firstMessage = (IMAP.Message) mItems.get(0);
				int from = firstMessage.id + mList.getVisibleRowCount();
				int to = Math.min(from + mList.getVisibleRowCount(),
						getFrame().imap.getSelection().messageCount);
				populateList(getFrame().imap.fetchMessages(from, to));
			} catch (ClassCastException ignored) {}
		});
		nextButton.setSize(300, 200);
		mPaginationContainer.add(prevButton);
		mPaginationContainer.add(nextButton);
		add(mPaginationContainer, BorderLayout.PAGE_END);
		mPaginationContainer.setVisible(false);
	}

	public void setMultiSelectable(boolean multiSelectable) {
		if (multiSelectable) {
			mList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else {
			mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
	}

	/**
	 * Blocking operation. Caller needs to provide user with interactiveness until this completes.
	 */
	public void populateList(ArrayList<IMAP.Item> items) {
		mItems = items;
		ItemListModel model = new ItemListModel();
		for (IMAP.Item item : mItems) {
			model.addElement(item);
		}
		mList.setModel(model);
	}

	/**
	 * Blocking operation. Caller needs to provide user with interactiveness until this completes.
	 */
	public void updateMessages() {
		IMAP imap = getFrame().imap;
		// If a mailbox is selected, reload it's messages
		IMAP.Mailbox mailbox = imap.getSelection();
		if (mailbox != null) {
			imap.close();
			imap.select(mailbox);
			populateList(imap.fetchMessages(1,
					Math.min(mList.getVisibleRowCount(), mailbox.messageCount)));
		}
	}

	public void navigateUp() {
		new Thread(() -> {
			IMAP imap = getFrame().imap;
			if (!imap.getLastPath().isEmpty()) {
				getFrame().setProgressing(true);
				// Navigate up the path
				String parent = "";
				int lastSeparator = imap.getLastPath().lastIndexOf('/');
				// If a separator exists cut the path up to it
				if (lastSeparator != -1) {
					parent = imap.getLastPath().substring(0, lastSeparator);
				}
				// Close selected mailbox (if any)
				if (imap.getSelection() != null) {
					imap.close();
					// If a mailbox is selected, then there might be a pagination, remove it.
					hidePagination();
				}

				populateList(imap.list(parent));
				getFrame().setProgressing(false);
			} else {
				// Already at root. Do nothing
			}
		}).start();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// Double click
		if (e.getClickCount() == 2) {
			new Thread(() -> {
				JList list = (JList) e.getSource();

				int index = list.locationToIndex(e.getPoint());

				// First list item = navigator
				if (index == 0) {
					navigateUp();
					return;
				} else {
					--index;
				}

				getFrame().setProgressing(true);
				IMAP.Item item = mItems.get(index);
				if (item instanceof IMAP.Mailbox) {
					IMAP.Mailbox mailbox = (IMAP.Mailbox) item;
					if (mailbox.hasChildren) {
						hidePagination();
						// Show sub-folders
						populateList(getFrame().imap.list(mailbox.path));
					} else {
						// Show messages
						getFrame().imap.select(mailbox);
						if (mailbox.messageCount > 0) {
							// If messages don't fit, add a pagination
							if (mailbox.messageCount > mList.getVisibleRowCount()) {
								showPagination();
							}
							populateList(getFrame().imap.fetchMessages(1,
									Math.min(mList.getVisibleRowCount(), mailbox.messageCount)));
						} else {
							populateList(new ArrayList<>());
						}
					}
				} else {
					// Item is a message, open it in a dialog
					IMAP.Message message = (IMAP.Message) item;
					getFrame().imap.fetchMessageContent(message);
					new MessageDialog(getFrame(), message);
				}
				getFrame().setProgressing(false);
			}).start();
		}
	}

	private void showPagination() {
		mPaginationContainer.setVisible(true);
	}

	void hidePagination() {
		mPaginationContainer.setVisible(false);
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
