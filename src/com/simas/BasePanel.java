package com.simas;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Created by Simas Abramovas on 2015 Mar 19.
 */

public class BasePanel extends JPanel {

	private MainFrame mFrame;

	public BasePanel(JFrame frame, String name) {
		mFrame = (MainFrame) frame;
		setName(name);
	}

	public MainFrame getFrame() {
		return mFrame;
	}

}
