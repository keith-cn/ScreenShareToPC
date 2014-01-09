package com.adakoda.android.asm;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

public class SelectDeviceDialog extends JDialog {
	private JList mList;
	private JScrollPane mScrollPane;
	private JButton mOK;
	private JButton mCancel;
	private DefaultListModel mModel;
	private boolean mIsOK = false;
	private int mSelectedIndex = -1;

	public SelectDeviceDialog(Frame owner, boolean modal,
			ArrayList<String> initialList) {
		super(owner, modal);

		setTitle("Select a Android Device");
		setBounds(0, 0, 240, 164);
		setResizable(false);

		this.mModel = new DefaultListModel();
		for (int i = 0; i < initialList.size(); i++) {
			this.mModel.addElement(initialList.get(i));
		}

		this.mList = new JList(this.mModel);
		if (this.mModel.getSize() > 0) {
			this.mSelectedIndex = 0;
			this.mList.setSelectedIndex(this.mSelectedIndex);
		}
		this.mList.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1)
					SelectDeviceDialog.this.onOK();
			}
		});
		this.mScrollPane = new JScrollPane(this.mList);
		this.mScrollPane.setVerticalScrollBarPolicy(20);

		this.mOK = new JButton("OK");
		this.mOK.setEnabled(this.mModel.getSize() > 0);
		this.mOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SelectDeviceDialog.this.onOK();
			}
		});
		this.mCancel = new JButton("Cancel");
		this.mCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SelectDeviceDialog.this.onCancel();
			}
		});
		Container container1 = new Container();
		GridLayout gridLayout = new GridLayout(1, 2, 0, 0);
		container1.setLayout(gridLayout);
		container1.add(this.mOK);
		container1.add(this.mCancel);

		Container containger = getContentPane();
		containger.add(this.mScrollPane, "Center");
		containger.add(container1, "South");

		AbstractAction actionOK = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				SelectDeviceDialog.this.onOK();
			}
		};
		AbstractAction actionCancel = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				SelectDeviceDialog.this.onCancel();
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

	public int getSelectedIndex() {
		return this.mSelectedIndex;
	}

	public boolean isOK() {
		return this.mIsOK;
	}

	private void onOK() {
		this.mSelectedIndex = this.mList.getSelectedIndex();
		this.mIsOK = true;
		dispose();
	}

	private void onCancel() {
		dispose();
	}
}
