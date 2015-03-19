package com.simas;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
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
    private static final String FOLDER = "Folder";
    private static final String FOLDERS = "Folders";

    private JPanel mCards;

    /**
     * Cards that are used in the {@code MainFrame} are distinguished by this enum
     */
    enum Card {
        LOGIN, FOLDERS, MESSAGES
    }

    public MainFrame() {
        super(APP_NAME);
        useAuthenticationMenu();
        customizeFrame();
        addCards();
        showCard(Card.LOGIN);
        setVisible(true);
    }

    private void useAuthenticationMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu(FILE);
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem(EXIT,  KeyEvent.VK_Q);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_MASK));
        menuItem.addActionListener(e -> System.exit(0));
        menu.add(menuItem);

        setJMenuBar(menuBar);
    }

    private void usePostAuthenticatedMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu(FILE);
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem(FOLDERS,  KeyEvent.VK_Q);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_MASK));
        menuItem.addActionListener(e -> showCard(Card.FOLDERS));
        menu.add(menuItem);

        menuItem = new JMenuItem(WRITE_NEW,  KeyEvent.VK_Q);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_MASK));
        menuItem.addActionListener(e -> {
            // ToDo write new mail dialog
        });
        menu.add(menuItem);

        menuItem = new JMenuItem(LOGOUT,  KeyEvent.VK_Q);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.ALT_MASK));
        menuItem.addActionListener(e -> {
            // ToDo logout
        });
        menu.add(menuItem);
        menu.addSeparator();

        menuItem = new JMenuItem(EXIT,  KeyEvent.VK_Q);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.ALT_MASK));
        menuItem.addActionListener(e -> System.exit(0));
        menu.add(menuItem);

        setJMenuBar(menuBar);
    }

    /**
     * Basic {@code Frame} customization
     */
    private void customizeFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(false);
        resizeToFitInScreen((double) 2/3);
    }

    /**
     * Resize the{@code }JFrame} to fit the screen at specified proportions.
     * @param atMost    Part of the window that can be covered at most.
     */
    private void resizeToFitInScreen(double atMost) {
        // Check image sizes
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double maxWidth = screenSize.getWidth() * atMost;
        double maxHeight = screenSize.getHeight() * atMost;
        setSize((int) maxWidth, (int) maxHeight);
        setLocationRelativeTo(null); // Center frame on the screen
    }

    /**
     * Prepare and add the cards that will be used in this frame.
     */
    private void addCards() {
        mCards = new JPanel(new CardLayout());
        mCards.add(new LoginPanel(this), Card.LOGIN.name());
//		mCards.add(new GamePanel(this), Card.FOLDERS.name());
//		mCards.add(new ScoresPanel(this), Card.MESSAGES.name());
        add(mCards);
    }

    /**
     * Display the specified card
     * @param card The card that will be shown, <code>Card</code> enum value.
     */
    public void showCard(Card card) {
        CardLayout cardLayout = (CardLayout) mCards.getLayout();
        cardLayout.show(mCards, card.name());
    }

    /**
     * Fetch the specified card from the Cards array (if one exists)
     * @param card The card's, which will be returned, <code>Card</code> enum value.
     * @return Returns the specified card or <code>null</code> if it wasn't found
     */
    public Component findCard(Card card) {
        for(Component c: mCards.getComponents()) {
            if (card.name().equals(c.getName())) return c;
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }

}
