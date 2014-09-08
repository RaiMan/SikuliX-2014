/*
 * Copyright 2010-2014, Sikuli.org, sikulix.com
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.ide;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.sikuli.script.Location;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.ScreenUnion;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;

public class PatternWindow extends JFrame {

  private static final String me = "PatternWindow: ";
	private EditorPatternButton _imgBtn;
	private PatternPaneScreenshot _screenshot;
	private PatternPaneTargetOffset _tarOffsetPane;
	private PatternPaneNaming paneNaming;
	private JTabbedPane tabPane;
	private JPanel paneTarget, panePreview;
	private JLabel[] msgApplied;
	private int tabSequence = 0;
	private static final int tabMax = 3;
	private ScreenImage _simg;
	private boolean dirty;

	static String _I(String key, Object... args) {
		return SikuliIDEI18N._I(key, args);
	}

	public PatternWindow(EditorPatternButton imgBtn, boolean exact,
					float similarity, int numMatches) {
		init(imgBtn, exact, similarity, numMatches);
	}

	private void init(EditorPatternButton imgBtn, boolean exact, float similarity, int numMatches) {
		setTitle(_I("winPatternSettings"));
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		_imgBtn = imgBtn;
		Point pos = imgBtn.getLocationOnScreen();
		Debug.log(4, "pattern window: " + pos);
		setLocation(pos.x + imgBtn.getWidth(), pos.y);

		takeScreenshot();
		Container c = getContentPane();
		c.setLayout(new BorderLayout());

		tabPane = new JTabbedPane();
		tabPane.setPreferredSize(new Dimension(790, 700));
		msgApplied = new JLabel[tabMax];

		msgApplied[tabSequence] = new JLabel("...");
		paneNaming = new PatternPaneNaming(_imgBtn, msgApplied[tabSequence++]);
		tabPane.addTab(_I("tabNaming"), paneNaming);

		msgApplied[tabSequence] = new JLabel("...");
		panePreview = createPreviewPanel();
		tabSequence++;
		tabPane.addTab(_I("tabMatchingPreview"), panePreview);

		msgApplied[tabSequence] = new JLabel("...");
		paneTarget = createTargetPanel();
		tabSequence++;
		tabPane.addTab(_I("tabTargetOffset"), paneTarget);

		c.add(tabPane, BorderLayout.CENTER);
		c.add(createButtons(), BorderLayout.SOUTH);
		c.doLayout();
		pack();
		try {
			_screenshot.setParameters(_imgBtn.getFilename(),
							exact, similarity, numMatches);
		} catch (Exception e) {
      Debug.error(me + "Problem while setting up pattern pane\n%s", e.getMessage());
		}
		setDirty(false);
		setVisible(true);
	}

	void takeScreenshot() {
		SikuliIDE ide = SikuliIDE.getInstance();
		ide.setVisible(false);
		try {
			Thread.sleep(500);
		} catch (Exception e) {
		}
		_simg = (new ScreenUnion()).getScreen().capture();
		ide.setVisible(true);
	}

	private JPanel createPreviewPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		_screenshot = new PatternPaneScreenshot(_simg);
    createMarginBox(p, _screenshot);
		p.add(Box.createVerticalStrut(5));
		p.add(_screenshot.createControls());
		p.add(Box.createVerticalStrut(5));
		p.add(msgApplied[tabSequence]);
		p.doLayout();
		return p;
	}

	private JPanel createTargetPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		_tarOffsetPane = new PatternPaneTargetOffset(
						_simg, _imgBtn.getFilename(), _imgBtn.getTargetOffset());
		createMarginBox(p, _tarOffsetPane);
		p.add(Box.createVerticalStrut(5));
		p.add(_tarOffsetPane.createControls());
		p.add(Box.createVerticalStrut(5));
		p.add(msgApplied[tabSequence]);
		p.doLayout();
		return p;
	}

	private JComponent createButtons() {
		JPanel pane = new JPanel(new GridBagLayout());
		JButton btnOK = new JButton(_I("ok"));
		btnOK.addActionListener(new ActionOK(this));
		JButton btnApply = new JButton(_I("apply"));
		btnApply.addActionListener(new ActionApply(this));
		final JButton btnCancel = new JButton(_I("cancel"));
		btnCancel.addActionListener(new ActionCancel(this));
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 3;
		c.gridx = 0;
		c.insets = new Insets(5, 0, 10, 0);
		c.anchor = GridBagConstraints.LAST_LINE_END;
		pane.add(btnOK, c);
		c.gridx = 1;
		pane.add(btnApply, c);
		c.gridx = 2;
		pane.add(btnCancel, c);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				btnCancel.doClick();
			}
		});
		KeyStroke escapeKeyStroke =
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		this.getRootPane().
			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
				put(escapeKeyStroke, "ESCAPE");
		this.getRootPane().getActionMap().put("ESCAPE",
			new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					btnCancel.doClick();
			}
		});
		return pane;
	}

	private void createMarginBox(Container c, Component comp) {
		c.add(Box.createVerticalStrut(10));
		Box lrMargins = Box.createHorizontalBox();
		lrMargins.add(Box.createHorizontalStrut(10));
		lrMargins.add(comp);
		lrMargins.add(Box.createHorizontalStrut(10));
		c.add(lrMargins);
		c.add(Box.createVerticalStrut(10));
	}


	public void setMessageApplied(int i, boolean flag) {
		if (flag) {
			msgApplied[i].setText("Changes have been applied");
		} else {
			msgApplied[i].setText("...");
		}
	}

	public void close() {
		_simg = null;
		_imgBtn.resetWindow();
	}

	public JTabbedPane getTabbedPane() {
		return tabPane;
	}

	public void setTargetOffset(Location offset) {
		if (offset != null) {
			_tarOffsetPane.setTarget(offset.x, offset.y);
		}
	}

	private void actionPerformedUpdates(Window _parent) {
		boolean tempDirty = isDirty();
		if (paneNaming.isDirty()) {
			String filename = paneNaming.getAbsolutePath();
			String oldFilename = _imgBtn.getFilename();
			if (FileManager.exists(filename)) {
				String name = FileManager.getName(filename);
				int ret = JOptionPane.showConfirmDialog(
								_parent,
								SikuliIDEI18N._I("msgFileExists", name),
								SikuliIDEI18N._I("dlgFileExists"),
								JOptionPane.WARNING_MESSAGE,
								JOptionPane.YES_NO_OPTION);
				if (ret != JOptionPane.YES_OPTION) {
					return;
				}
			}
			try {
				FileManager.xcopy(oldFilename, filename);
				_imgBtn.setFilename(filename);
			} catch (IOException ioe) {
				Debug.error("renaming failed: " + oldFilename + " " + filename);
				Debug.error(ioe.getMessage());
			}
			paneNaming.updateFilename();
			addDirty(true);
		}
		addDirty(_imgBtn.setParameters(
						_screenshot.isExact(), _screenshot.getSimilarity(),
						_screenshot.getNumMatches()));
		addDirty(_imgBtn.setTargetOffset(_tarOffsetPane.getTargetOffset()));
		if (isDirty() || tempDirty) {
			Debug.log(3, "Preview: update: " + _imgBtn.toString());
			int i = _imgBtn.getWindow().getTabbedPane().getSelectedIndex();
			_imgBtn.getWindow().setMessageApplied(i, true);
			_imgBtn.repaint();
		}
	}

	class ActionOK implements ActionListener {

		private Window _parent;

		public ActionOK(JFrame parent) {
			_parent = parent;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			actionPerformedUpdates(_parent);
			_imgBtn.getWindow().close();
			_parent.dispose();
			SikuliIDE.getInstance().getCurrentCodePane().setDirty(setDirty(false));
		}
	}

	class ActionApply implements ActionListener {

		private Window _parent;

		public ActionApply(Window parent) {
			_parent = parent;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			actionPerformedUpdates(_parent);
			_imgBtn.getWindow().getTabbedPane().getSelectedComponent().transferFocus();
		}
	}

	class ActionCancel implements ActionListener {

		private Window _parent;

		public ActionCancel(Window parent) {
			_parent = parent;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (isDirty()) {
				_imgBtn.resetParameters();
			}
			_imgBtn.getWindow().close();
			_parent.dispose();
		}
	}

	protected boolean isDirty() {
		return dirty;
	}

	private boolean setDirty(boolean flag) {
		boolean xDirty = dirty;
		dirty = flag;
		return xDirty;
	}

	protected void addDirty(boolean flag) {
		dirty |= flag;
	}
}
