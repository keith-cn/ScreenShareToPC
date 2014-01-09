package com.adakoda.android.asm;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.TimeoutException;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;

public class MainFrame extends JFrame {
	private static final int DEFAULT_WIDTH = 320;
	private static final int DEFAULT_HEIGHT = 480;
	private static final String EXT_PNG = "png";
	private MainPanel mPanel;
	private JPopupMenu mPopupMenu;
	private int mRawImageWidth = 320;
	private int mRawImageHeight = 480;
	private boolean mPortrait = true;
	private double mZoom = 1.0D;
	private boolean mAdjustColor = false;
	private JCheckBoxMenuItem mAdjustColorCheckBoxMenuItem;
	private ADB mADB;
	private IDevice[] mDevices;
	private IDevice mDevice;
	private MonitorThread mMonitorThread;
	private MouseListener mMouseListener = new MouseListener() {
		public void mouseReleased(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e))
				MainFrame.this.mPopupMenu.show(e.getComponent(), e.getX(),
						e.getY());
		}
	};

	private WindowListener mWindowListener = new WindowListener() {
		public void windowOpened(WindowEvent arg0) {
		}

		public void windowIconified(WindowEvent arg0) {
		}

		public void windowDeiconified(WindowEvent arg0) {
		}

		public void windowDeactivated(WindowEvent arg0) {
		}

		public void windowClosing(WindowEvent arg0) {
			if (MainFrame.this.mADB != null)
				MainFrame.this.mADB.terminate();
		}

		public void windowClosed(WindowEvent arg0) {
		}

		public void windowActivated(WindowEvent arg0) {
		}
	};

	public MainFrame(String[] args) {
		initialize(args);
	}

	public void startMonitor() {
		this.mMonitorThread = new MonitorThread();
		this.mMonitorThread.start();
	}

	public void stopMonitor() {
		this.mMonitorThread = null;
	}

	public void selectDevice() {
		stopMonitor();

		this.mDevices = this.mADB.getDevices();
		if (this.mDevices != null) {
			ArrayList list = new ArrayList();
			for (int i = 0; i < this.mDevices.length; i++) {
				list.add(this.mDevices[i].toString());
			}
			SelectDeviceDialog dialog = new SelectDeviceDialog(this, true, list);
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
			if (dialog.isOK()) {
				int selectedIndex = dialog.getSelectedIndex();
				if (selectedIndex >= 0) {
					this.mDevice = this.mDevices[selectedIndex];
					setImage(null);
				}
			}
		}

		startMonitor();
	}

	public void setOrientation(boolean portrait) {
		if (this.mPortrait != portrait) {
			this.mPortrait = portrait;
			updateSize();
		}
	}

	public void setZoom(double zoom) {
		if (this.mZoom != zoom) {
			this.mZoom = zoom;
			updateSize();
		}
	}

	public void saveImage() {
		FBImage inImage = this.mPanel.getFBImage();
		if (inImage != null) {
			BufferedImage outImage = new BufferedImage(
					(int) (inImage.getWidth() * this.mZoom),
					(int) (inImage.getHeight() * this.mZoom), inImage.getType());
			if (outImage != null) {
				AffineTransformOp op = new AffineTransformOp(
						AffineTransform
								.getScaleInstance(this.mZoom, this.mZoom),
						2);
				op.filter(inImage, outImage);
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileFilter() {
					public String getDescription() {
						return "*.png";
					}

					public boolean accept(File f) {
						String ext = f.getName().toLowerCase();
						return ext.endsWith(".png");
					}
				});
				if (fileChooser.showSaveDialog(this) == 0)
					try {
						File file = fileChooser.getSelectedFile();
						String path = file.getAbsolutePath();
						if (!path.endsWith(".png")) {
							file = new File(path + "." + "png");
						}
						ImageIO.write(outImage, "png", file);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(this,
								"Failed to save a image.", "Save Image", 0);
					}
			}
		}
	}

	public void about() {
		AboutDialog dialog = new AboutDialog(this, true);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	public void updateSize() {
		int height;
		int width;
		// int height;
		if (this.mPortrait) {
			width = this.mRawImageWidth;
			height = this.mRawImageHeight;
		} else {
			width = this.mRawImageHeight;
			height = this.mRawImageWidth;
		}
		Insets insets = getInsets();
		int newWidth = (int) (width * this.mZoom) + insets.left + insets.right;
		int newHeight = (int) (height * this.mZoom) + insets.top
				+ insets.bottom;

		if ((getWidth() != newWidth) || (getHeight() != newHeight))
			setSize(newWidth, newHeight);
	}

	public void setImage(FBImage fbImage) {
		if (fbImage != null) {
			this.mRawImageWidth = fbImage.getRawWidth();
			this.mRawImageHeight = fbImage.getRawHeight();
		}
		this.mPanel.setFBImage(fbImage);
		updateSize();
	}

	private void initialize(String[] args) {
		this.mADB = new ADB();
		if (!this.mADB.initialize()) {
			JOptionPane
					.showMessageDialog(
							this,
							"Could not find adb, please install Android SDK and set path to adb.",
							"Error", 0);
		}

		parseArgs(args);

		initializeFrame();
		initializePanel();
		initializeMenu();
		initializeActionMap();

		addMouseListener(this.mMouseListener);
		addWindowListener(this.mWindowListener);

		pack();
		setImage(null);
	}

	private void parseArgs(String[] args) {
		if (args != null)
			for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				if (arg.equals("-a"))
					this.mAdjustColor = true;
			}
	}

	private void initializeFrame() {
		setTitle("Android Screen Monitor");
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("icon.png")));
		setDefaultCloseOperation(3);
		setResizable(false);
	}

	private void initializePanel() {
		this.mPanel = new MainPanel();
		add(this.mPanel);
	}

	private void initializeMenu() {
		this.mPopupMenu = new JPopupMenu();

		initializeSelectDeviceMenu();
		this.mPopupMenu.addSeparator();
		initializeOrientationMenu();
		initializeZoomMenu();
		initializeAdjustColor();
		this.mPopupMenu.addSeparator();
		initializeSaveImageMenu();
		this.mPopupMenu.addSeparator();
		initializeAbout();

		this.mPopupMenu.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
	}

	private void initializeSelectDeviceMenu() {
		JMenuItem menuItemSelectDevice = new JMenuItem("Select Device...");
		menuItemSelectDevice.setMnemonic(68);
		menuItemSelectDevice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.selectDevice();
			}
		});
		this.mPopupMenu.add(menuItemSelectDevice);
	}

	private void initializeOrientationMenu() {
		JMenu menuOrientation = new JMenu("Orientation");
		menuOrientation.setMnemonic(79);
		this.mPopupMenu.add(menuOrientation);

		ButtonGroup buttonGroup = new ButtonGroup();

		JRadioButtonMenuItem radioButtonMenuItemPortrait = new JRadioButtonMenuItem(
				"Portrait");
		radioButtonMenuItemPortrait.setSelected(true);
		radioButtonMenuItemPortrait.setMnemonic(80);
		radioButtonMenuItemPortrait.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setOrientation(true);
			}
		});
		buttonGroup.add(radioButtonMenuItemPortrait);
		menuOrientation.add(radioButtonMenuItemPortrait);

		JRadioButtonMenuItem radioButtonMenuItemLandscape = new JRadioButtonMenuItem(
				"Landscape");
		radioButtonMenuItemLandscape.setMnemonic(76);
		radioButtonMenuItemLandscape.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setOrientation(false);
			}
		});
		buttonGroup.add(radioButtonMenuItemLandscape);
		menuOrientation.add(radioButtonMenuItemLandscape);
	}

	private void initializeZoomMenu() {
		JMenu menuZoom = new JMenu("Zoom");
		menuZoom.setMnemonic(90);
		this.mPopupMenu.add(menuZoom);

		ButtonGroup buttonGroup = new ButtonGroup();

		JRadioButtonMenuItem radioButtonMenuItemZoom50 = new JRadioButtonMenuItem(
				"50%");
		radioButtonMenuItemZoom50.setMnemonic(53);
		radioButtonMenuItemZoom50.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setZoom(0.5D);
			}
		});
		buttonGroup.add(radioButtonMenuItemZoom50);
		menuZoom.add(radioButtonMenuItemZoom50);

		JRadioButtonMenuItem radioButtonMenuItemZoom75 = new JRadioButtonMenuItem(
				"75%");
		radioButtonMenuItemZoom75.setMnemonic(55);
		radioButtonMenuItemZoom75.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setZoom(0.75D);
			}
		});
		buttonGroup.add(radioButtonMenuItemZoom75);
		menuZoom.add(radioButtonMenuItemZoom75);

		JRadioButtonMenuItem radioButtonMenuItemZoom100 = new JRadioButtonMenuItem(
				"100%");
		radioButtonMenuItemZoom100.setSelected(true);
		radioButtonMenuItemZoom100.setMnemonic(49);
		radioButtonMenuItemZoom100.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setZoom(1.0D);
			}
		});
		buttonGroup.add(radioButtonMenuItemZoom100);
		menuZoom.add(radioButtonMenuItemZoom100);

		JRadioButtonMenuItem radioButtonMenuItemZoom150 = new JRadioButtonMenuItem(
				"150%");
		radioButtonMenuItemZoom150.setMnemonic(48);
		radioButtonMenuItemZoom150.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setZoom(1.5D);
			}
		});
		buttonGroup.add(radioButtonMenuItemZoom150);
		menuZoom.add(radioButtonMenuItemZoom150);

		JRadioButtonMenuItem radioButtonMenuItemZoom200 = new JRadioButtonMenuItem(
				"200%");
		radioButtonMenuItemZoom200.setMnemonic(50);
		radioButtonMenuItemZoom200.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setZoom(2.0D);
			}
		});
		buttonGroup.add(radioButtonMenuItemZoom200);
		menuZoom.add(radioButtonMenuItemZoom200);
	}

	private void initializeAdjustColor() {
		this.mAdjustColorCheckBoxMenuItem = new JCheckBoxMenuItem(
				"Adjust Color");
		this.mAdjustColorCheckBoxMenuItem.setMnemonic(74);
		this.mAdjustColorCheckBoxMenuItem
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MainFrame.this.mAdjustColor = (!MainFrame.this.mAdjustColor);
						MainFrame.this.mAdjustColorCheckBoxMenuItem
								.setSelected(MainFrame.this.mAdjustColor);
					}
				});
		this.mPopupMenu.add(this.mAdjustColorCheckBoxMenuItem);
	}

	private void initializeSaveImageMenu() {
		JMenuItem menuItemSaveImage = new JMenuItem("Save Image...");
		menuItemSaveImage.setMnemonic(83);
		menuItemSaveImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.saveImage();
			}
		});
		this.mPopupMenu.add(menuItemSaveImage);
	}

	private void initializeActionMap() {
		AbstractAction actionSelectDevice = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.selectDevice();
			}
		};
		AbstractAction actionPortrait = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setOrientation(true);
			}
		};
		AbstractAction actionLandscape = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setOrientation(false);
			}
		};
		AbstractAction actionZoom50 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setZoom(0.5D);
			}
		};
		AbstractAction actionZoom75 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setZoom(0.75D);
			}
		};
		AbstractAction actionZoom100 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setZoom(1.0D);
			}
		};
		AbstractAction actionZoom150 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setZoom(1.5D);
			}
		};
		AbstractAction actionZoom200 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setZoom(2.0D);
			}
		};
		AbstractAction actionAdjustColor = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.mAdjustColor = (!MainFrame.this.mAdjustColor);
				MainFrame.this.mAdjustColorCheckBoxMenuItem
						.setSelected(MainFrame.this.mAdjustColor);
			}
		};
		AbstractAction actionSaveImage = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.saveImage();
			}
		};
		AbstractAction actionAbout = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.about();
			}
		};
		JComponent targetComponent = getRootPane();
		InputMap inputMap = targetComponent.getInputMap();

		inputMap.put(KeyStroke.getKeyStroke(68, 128), "Select Device");
		inputMap.put(KeyStroke.getKeyStroke(80, 128), "Portrait");
		inputMap.put(KeyStroke.getKeyStroke(76, 128), "Landscape");
		inputMap.put(KeyStroke.getKeyStroke(53, 128), "50%");
		inputMap.put(KeyStroke.getKeyStroke(55, 128), "75%");
		inputMap.put(KeyStroke.getKeyStroke(49, 128), "100%");
		inputMap.put(KeyStroke.getKeyStroke(48, 128), "150%");
		inputMap.put(KeyStroke.getKeyStroke(50, 128), "200%");
		inputMap.put(KeyStroke.getKeyStroke(74, 128), "Adjust Color");
		inputMap.put(KeyStroke.getKeyStroke(83, 128), "Save Image");
		inputMap.put(KeyStroke.getKeyStroke(65, 128), "About ASM");

		targetComponent.setInputMap(1, inputMap);

		targetComponent.getActionMap().put("Select Device", actionSelectDevice);
		targetComponent.getActionMap().put("Portrait", actionPortrait);
		targetComponent.getActionMap().put("Landscape", actionLandscape);
		targetComponent.getActionMap().put("Select Device", actionSelectDevice);
		targetComponent.getActionMap().put("50%", actionZoom50);
		targetComponent.getActionMap().put("75%", actionZoom75);
		targetComponent.getActionMap().put("100%", actionZoom100);
		targetComponent.getActionMap().put("150%", actionZoom150);
		targetComponent.getActionMap().put("200%", actionZoom200);
		targetComponent.getActionMap().put("Adjust Color", actionAdjustColor);
		targetComponent.getActionMap().put("Save Image", actionSaveImage);
		targetComponent.getActionMap().put("About ASM", actionAbout);
	}

	private void initializeAbout() {
		JMenuItem menuItemAbout = new JMenuItem("About ASM");
		menuItemAbout.setMnemonic(65);
		menuItemAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.about();
			}
		});
		this.mPopupMenu.add(menuItemAbout);
	}

	public class MainPanel extends JPanel {
		private FBImage mFBImage;

		public MainPanel() {
			setBackground(Color.BLACK);
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (this.mFBImage != null) {
				int srcHeight;
				int srcWidth;
				// int srcHeight;
				if (MainFrame.this.mPortrait) {
					srcWidth = MainFrame.this.mRawImageWidth;
					srcHeight = MainFrame.this.mRawImageHeight;
				} else {
					srcWidth = MainFrame.this.mRawImageHeight;
					srcHeight = MainFrame.this.mRawImageWidth;
				}
				int dstWidth = (int) (srcWidth * MainFrame.this.mZoom);
				int dstHeight = (int) (srcHeight * MainFrame.this.mZoom);
				if (MainFrame.this.mZoom == 1.0D) {
					g.drawImage(this.mFBImage, 0, 0, dstWidth, dstHeight, 0, 0,
							srcWidth, srcHeight, null);
				} else {
					Image image = this.mFBImage.getScaledInstance(dstWidth,
							dstHeight, 4);
					if (image != null)
						g.drawImage(image, 0, 0, dstWidth, dstHeight, 0, 0,
								dstWidth, dstHeight, null);
				}
			}
		}

		public void setFBImage(FBImage fbImage) {
			this.mFBImage = fbImage;
			repaint();
		}

		public FBImage getFBImage() {
			return this.mFBImage;
		}
	}

	public class MonitorThread extends Thread {
		public MonitorThread() {
		}

		public void run() {
			Thread thread = Thread.currentThread();
			if (MainFrame.this.mDevice != null)
				try {
					while (MainFrame.this.mMonitorThread == thread) {
						final FBImage fbImage = getDeviceImage();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								MainFrame.this.setImage(fbImage);
							}
						});
					}
				} catch (IOException localIOException) {
				}
		}

		private FBImage getDeviceImage() throws IOException {
			boolean success = true;
			boolean debug = false;
			FBImage fbImage = null;
			RawImage tmpRawImage = null;
			RawImage rawImage = null;

			if (success) {
				try {
					tmpRawImage = MainFrame.this.mDevice.getScreenshot();

					if (tmpRawImage == null) {
						success = false;
					} else if (!debug) {
						rawImage = tmpRawImage;
					} else {
						rawImage = new RawImage();
						rawImage.version = 1;
						rawImage.bpp = 32;
						rawImage.size = (tmpRawImage.width * tmpRawImage.height * 4);
						rawImage.width = tmpRawImage.width;
						rawImage.height = tmpRawImage.height;
						rawImage.red_offset = 0;
						rawImage.red_length = 8;
						rawImage.blue_offset = 16;
						rawImage.blue_length = 8;
						rawImage.green_offset = 8;
						rawImage.green_length = 8;
						rawImage.alpha_offset = 0;
						rawImage.alpha_length = 0;
						rawImage.data = new byte[rawImage.size];

						int index = 0;
						int dst = 0;
						for (int y = 0; y < rawImage.height; y++) {
							for (int x = 0; x < rawImage.width; x++) {
								int value = tmpRawImage.data[(index++)] & 0xFF;
								value |= tmpRawImage.data[(index++)] << 8 & 0xFF00;
								int r = (value >> 11 & 0x1F) << 3;
								int g = (value >> 5 & 0x3F) << 2;
								int b = (value >> 0 & 0x1F) << 3;

								rawImage.data[(dst++)] = ((byte) r);
								rawImage.data[(dst++)] = ((byte) g);
								rawImage.data[(dst++)] = ((byte) b);
								rawImage.data[(dst++)] = -1;
							}
						}

					}

				} catch (IOException localIOException) {
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AdbCommandRejectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if ((rawImage == null)
							|| ((rawImage.bpp != 16) && (rawImage.bpp != 32))) {
						success = false;
					}
				}
			}

			if (success) {
				int imageHeight;
				int imageWidth;
				// int imageHeight;
				if (MainFrame.this.mPortrait) {
					imageWidth = rawImage.width;
					imageHeight = rawImage.height;
				} else {
					imageWidth = rawImage.height;
					imageHeight = rawImage.width;
				}

				fbImage = new FBImage(imageWidth, imageHeight, 1,
						rawImage.width, rawImage.height);

				byte[] buffer = rawImage.data;
				int redOffset = rawImage.red_offset;
				int greenOffset = rawImage.green_offset;
				int blueOffset = rawImage.blue_offset;
				int alphaOffset = rawImage.alpha_offset;
				int redMask = getMask(rawImage.red_length);
				int greenMask = getMask(rawImage.green_length);
				int blueMask = getMask(rawImage.blue_length);
				int alphaMask = getMask(rawImage.alpha_length);
				int redShift = 8 - rawImage.red_length;
				int greenShift = 8 - rawImage.green_length;
				int blueShift = 8 - rawImage.blue_length;
				int alphaShift = 8 - rawImage.alpha_length;

				int index = 0;

				if (rawImage.bpp == 16) {
					int offset1;
					int offset0;
					// int offset1;
					if (!MainFrame.this.mAdjustColor) {
						offset0 = 0;
						offset1 = 1;
					} else {
						offset0 = 1;
						offset1 = 0;
					}
					if (MainFrame.this.mPortrait) {
						for (int y = 0; y < rawImage.height; y++)
							for (int x = 0; x < rawImage.width; x++) {
								int value = buffer[(index + offset0)] & 0xFF;
								value |= buffer[(index + offset1)] << 8 & 0xFF00;
								int r = (value >>> redOffset & redMask) << redShift;
								int g = (value >>> greenOffset & greenMask) << greenShift;
								int b = (value >>> blueOffset & blueMask) << blueShift;
								value = 0xFF000000 | r << 16 | g << 8 | b;
								index += 2;
								fbImage.setRGB(x, y, value);
							}
					} else {
						for (int y = 0; y < rawImage.height; y++)
							for (int x = 0; x < rawImage.width; x++) {
								int value = buffer[(index + offset0)] & 0xFF;
								value |= buffer[(index + offset1)] << 8 & 0xFF00;
								int r = (value >>> redOffset & redMask) << redShift;
								int g = (value >>> greenOffset & greenMask) << greenShift;
								int b = (value >>> blueOffset & blueMask) << blueShift;
								value = 0xFF000000 | r << 16 | g << 8 | b;
								index += 2;
								fbImage.setRGB(y, rawImage.width - x - 1, value);
							}
					}
				} else if (rawImage.bpp == 32) {
					int offset3;
					int offset0;
					int offset1;
					int offset2;
					// int offset3;
					if (!MainFrame.this.mAdjustColor) {
						offset0 = 0;
						offset1 = 1;
						offset2 = 2;
						offset3 = 3;
					} else {
						offset0 = 3;
						offset1 = 2;
						offset2 = 1;
						offset3 = 0;
					}
					if (MainFrame.this.mPortrait) {
						for (int y = 0; y < rawImage.height; y++)
							for (int x = 0; x < rawImage.width; x++) {
								int value = buffer[(index + offset0)] & 0xFF;
								value |= (buffer[(index + offset1)] & 0xFF) << 8;
								value |= (buffer[(index + offset2)] & 0xFF) << 16;
								value |= (buffer[(index + offset3)] & 0xFF) << 24;
								int r = (value >>> redOffset & redMask) << redShift;
								int g = (value >>> greenOffset & greenMask) << greenShift;
								int b = (value >>> blueOffset & blueMask) << blueShift;
								int a;
								// int a;
								if (rawImage.alpha_length == 0)
									a = 255;
								else {
									a = (value >>> alphaOffset & alphaMask) << alphaShift;
								}
								value = a << 24 | r << 16 | g << 8 | b;
								index += 4;
								fbImage.setRGB(x, y, value);
							}
					} else {
						for (int y = 0; y < rawImage.height; y++) {
							for (int x = 0; x < rawImage.width; x++) {
								int value = buffer[(index + offset0)] & 0xFF;
								value |= (buffer[(index + offset1)] & 0xFF) << 8;
								value |= (buffer[(index + offset2)] & 0xFF) << 16;
								value |= (buffer[(index + offset3)] & 0xFF) << 24;
								int r = (value >>> redOffset & redMask) << redShift;
								int g = (value >>> greenOffset & greenMask) << greenShift;
								int b = (value >>> blueOffset & blueMask) << blueShift;
								int a;
								// int a;
								if (rawImage.alpha_length == 0)
									a = 255;
								else {
									a = (value >>> alphaOffset & alphaMask) << alphaShift;
								}
								value = a << 24 | r << 16 | g << 8 | b;
								index += 4;
								fbImage.setRGB(y, rawImage.width - x - 1, value);
							}
						}
					}
				}
			}

			return fbImage;
		}

		public int getMask(int length) {
			int res = 0;
			for (int i = 0; i < length; i++) {
				res = (res << 1) + 1;
			}

			return res;
		}
	}
}
