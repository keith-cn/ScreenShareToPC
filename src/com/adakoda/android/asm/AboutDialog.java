package com.adakoda.android.asm;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

public class AboutDialog extends JDialog {
	public AboutDialog(Frame owner, boolean modal) {
		super(owner, modal);

		setTitle("About Android Screen Monitor");
		setBounds(0, 0, 320, 140);
		setResizable(false);

		JLabel labelApp = new JLabel("Android Screen Monitor Version 2.30");
		JLabel labelCopyright = new JLabel(
				"Copyright (C) 2009-2011 adakoda Al rights reserved.");
		JTextField labelUrl = new JTextField(
				"http://www.adakoda.com/adakoda/android/asm/");
		labelUrl.setEditable(false);
		labelUrl.setBorder(new EmptyBorder(0, 0, 0, 0));
		labelUrl.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {
			}

			public void mousePressed(MouseEvent arg0) {
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mouseEntered(MouseEvent arg0) {
			}

			public void mouseClicked(MouseEvent arg0) {
				JTextField textField = (JTextField) arg0.getSource();
				textField.selectAll();
			}
		});
		JButton buttonOK = new JButton("OK");
		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.onOK();
			}
		});
		Container container1 = new Container();
		FlowLayout flowLayout = new FlowLayout(1, 5, 5);
		container1.setLayout(flowLayout);
		container1.add(labelApp);
		container1.add(labelCopyright);
		container1.add(labelUrl);
		container1.add(buttonOK);

		Container containger = getContentPane();
		containger.add(container1, "Center");
		containger.add(buttonOK, "South");

		AbstractAction actionOK = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.onOK();
			}
		};
		AbstractAction actionCancel = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.onCancel();
			}
		};
		JComponent targetComponent = getRootPane();
		InputMap inputMap = targetComponent.getInputMap();
		inputMap.put(KeyStroke.getKeyStroke(10, 0), "OK");
		inputMap.put(KeyStroke.getKeyStroke(27, 0), "Cancel");
		targetComponent.setInputMap(1, inputMap);
		targetComponent.getActionMap().put("OK", actionOK);
		targetComponent.getActionMap().put("Cancel", actionCancel);
	}

	private void onOK() {
		dispose();
	}

	private void onCancel() {
		dispose();
	}
}
