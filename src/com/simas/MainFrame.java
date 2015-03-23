package com.simas;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Created by Simas Abramovas on 2015 Mar 19.
 */

public class MainFrame extends JFrame {

    public static final String APP_NAME = "Mail Client";

    // File menu
    private static final String FILE = "File";
    public static final String LOGIN = "Login";
    private static final String LOGOUT = "Logout";
    private static final String WRITE_NEW = "Write new";
    private static final String EXIT = "Exit";
    private static final String MAIL = "Mail";
    private static final String MAILBOX = "Mailbox";
    private static final String FOLDERS = "Folders";

    private JDialog mProgressDialog;
    private JPanel mCards;
    public IMAP imap;

    // ToDo pagination

    // Maybe:
    // use MULTIPLE_INTERVAL_SELECTION for multi msg deletion
        // Selecting multiple and clicking read => Opens multiple dialogs
        // Selecting multiple and clicking delete => Deletes multiple messages
    // fetch uid!!!! FETCH 1 (UID) => dafuq is uid
    // MimeUtils.decode(subject) => doesn't easily work
    // if multiple messages open, and one of them is deleted, others have their ids changed
    // write new mail dialog
    // bold text = unread
    // folder rename, delete
    // \NoSelect

    /**
     * Cards that are used in the {@code MainFrame} are distinguished by this enum
     */
    enum Card {
        LOGIN, LIST, FOLDERS, MESSAGES
    }

    public MainFrame() {
        super(APP_NAME);
        usePreAuthFileMenu();
        customizeFrame();
        addCards();
        showCard(Card.LOGIN);
        setVisible(true);
        initProgressDialog();
    }

    private void initProgressDialog() {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setSize(100, 30);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        container.add(progressBar);

        mProgressDialog = new JDialog(this, "Please wait");
        mProgressDialog.add(container);
        mProgressDialog.pack();
        mProgressDialog.setLocationRelativeTo(null);
    }

    private void usePreAuthFileMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu(FILE);
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem(EXIT);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.ALT_MASK));
        menuItem.addActionListener(e -> new Thread(() -> {
            System.exit(0);
        }).start());
        menu.add(menuItem);

        setJMenuBar(menuBar);
    }

    void usePostAuthMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu(FILE);
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem(FOLDERS);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_MASK));
        menuItem.addActionListener(e -> new Thread(() -> {
            setProgressing(true);
            ListPanel list = (ListPanel) findCard(Card.LIST);
            // Close selected mailbox (if any)
            if (imap.getSelection() != null) imap.close();
            list.populateList(imap.list(""));
            setProgressing(false);
        }).start());
        menu.add(menuItem);

//        menuItem = new JMenuItem(WRITE_NEW);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_MASK));
//        menuItem.addActionListener(e -> {
//            // new mail dialog
//        });
//        menu.add(menuItem);

        menuItem = new JMenuItem(LOGOUT);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.ALT_MASK));
        menuItem.addActionListener(e -> new Thread(() -> {
            setProgressing(true);
            usePreAuthFileMenu();
            imap.logout();
            imap.close();
            showCard(Card.LOGIN);
            setProgressing(false);
        }).start());
        menu.add(menuItem);
        menu.addSeparator();

        menuItem = new JMenuItem(EXIT);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.ALT_MASK));
        menuItem.addActionListener(e -> new Thread(() -> {
            setProgressing(true);
            imap.logout();
            imap.close();
            System.exit(0);
        }).start());
        menu.add(menuItem);

        setJMenuBar(menuBar);
    }

    /**
     * Basic {@code Frame} customization
     */
    private void customizeFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(false);
        resizeToFitInScreen(this, (double) 2/5);
    }

    /**
     * Resize the{@code }JFrame} to fit the screen at specified proportions.
     * @param frame     Window that will be resized
     * @param atMost    Part of the window that can be covered at most.
     */
    public static void resizeToFitInScreen(Window frame, double atMost) {
        // Check image sizes
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double maxWidth = screenSize.getWidth() * atMost;
        double maxHeight = screenSize.getHeight() * atMost;
        frame.setSize((int) maxWidth, (int) maxHeight);
        frame.setLocationRelativeTo(null); // Center frame on the screen
    }

    /**
     * Prepare and add the cards that will be used in this frame.
     */
    private void addCards() {
        mCards = new JPanel(new CardLayout());
        mCards.add(new LoginPanel(this), Card.LOGIN.name());
		mCards.add(new ListPanel(this), Card.LIST.name());
//		mCards.add(new ScoresPanel(this), Card.FOLDERS.name());
        add(mCards);
    }

    /**
     * Display the specified card
     * @param card The card that will be shown, {@code Card} enum value.
     */
    public void showCard(Card card) {
        CardLayout cardLayout = (CardLayout) mCards.getLayout();
        cardLayout.show(mCards, card.name());
    }

    /**
     * Fetch the specified card from the Cards array (if one exists)
     * @param card    The card's, which will be returned, {@code Card} enum value.
     * @return Returns the specified card or {@code null} if it wasn't found
     */
    public Component findCard(Card card) {
        for(Component c: mCards.getComponents()) {
            if (card.name().equals(c.getName())) return c;
        }
        return null;
    }

    public void setProgressing(boolean shown) {
        mProgressDialog.setVisible(shown);
        // Disable window interaction when progress bar is shown
        setEnabled(!shown);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }

}
