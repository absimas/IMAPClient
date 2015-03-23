package com.simas;

/**
 * Created by Simas Abramovas on 2015 Mar 23.
 */
public class BackgroundTask extends Thread {

	public BackgroundTask(Runnable runnable) {
		super(runnable);
		start();
	}

}
