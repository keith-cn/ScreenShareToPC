package com.adakoda.android.asm;

import javax.swing.SwingUtilities;

public class AndroidScreenMonitor {
	private MainFrame mMainFrame;
	private static String[] mArgs;

	public void initialize() {
		this.mMainFrame = new MainFrame(mArgs);
		this.mMainFrame.setLocationRelativeTo(null);
		this.mMainFrame.setVisible(true);
		this.mMainFrame.selectDevice();
	}

	public static void main(String[] args) {
		mArgs = args;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new AndroidScreenMonitor().initialize();
			}
		});
	}
}
