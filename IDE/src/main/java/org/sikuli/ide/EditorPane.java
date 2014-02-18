/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.ide;

import org.sikuli.basics.PreferencesUser;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import org.sikuli.basics.FileManager;
import org.sikuli.basics.IResourceLoader;
import org.sikuli.basics.IndentationLogic;
import org.sikuli.script.Location;
import org.sikuli.basics.SikuliX;
import org.sikuli.script.Image;
import org.sikuli.script.ImagePath;

public class EditorPane extends JTextPane implements KeyListener, CaretListener {

	private static final String me = "EditorPane: ";
	private static TransferHandler transferHandler = null;
	private PreferencesUser pref;
	private File _editingFile;
	private String editingType = null;
	private String _srcBundlePath = null;
	private boolean _srcBundleTemp = false;
	private boolean _dirty = false;
	private EditorCurrentLineHighlighter _highlighter;
	private EditorUndoManager _undo = null;
	private boolean hasErrorHighlight = false;
	public boolean showThumbs;
	// TODO: move to SikuliDocument ????
	private IndentationLogic _indentationLogic = null;
	static Pattern patPngStr = Pattern.compile("(\"[^\"]+?\\.(?i)(png|jpg)\")");
	static Pattern patCaptureBtn = Pattern.compile("(\"__CLICK-TO-CAPTURE__\")");
	static Pattern patPatternStr = Pattern.compile(
					"\\b(Pattern\\s*\\(\".*?\"\\)(\\.\\w+\\([^)]*\\))+)");
	static Pattern patRegionStr = Pattern.compile(
					"\\b(Region\\s*\\([\\d\\s,]+\\))");
	//TODO what is it for???
	private int _caret_last_x = -1;
	private boolean _can_update_caret_last_x = true;
	private SikuliIDEPopUpMenu popMenuImage;
	private String sikuliContentType;

	//<editor-fold defaultstate="collapsed" desc="Initialization">
	public EditorPane(SikuliIDE ide) {
		pref = PreferencesUser.getInstance();
		showThumbs = !pref.getPrefMorePlainText();
	}

	public void initBeforeLoad(String scriptType) {
		initBeforeLoad(scriptType, false);
	}

	public void reInit(String scriptType) {
		initBeforeLoad(scriptType, true);
	}

	public void initBeforeLoad(String scriptType, boolean reInit) {
		String scrType = null;
		boolean paneIsEmpty = false;

		if (scriptType == null) {
			scriptType = Settings.EDEFAULT;
			paneIsEmpty = true;
		}

		if (Settings.EPYTHON.equals(scriptType)) {
			scrType = Settings.CPYTHON;
			_indentationLogic = SikuliIDE.getIDESupport(scriptType).getIndentationLogic();
			_indentationLogic.setTabWidth(pref.getTabWidth());
		} else if (Settings.ERUBY.equals(scriptType)) {
			scrType = Settings.CRUBY;
			_indentationLogic = null;
		}

		if (scrType != null) {
			sikuliContentType = scrType;
			SikuliEditorKit ek = new SikuliEditorKit(this);
			setEditorKit(ek);
			setContentType(scrType);

			if (_indentationLogic != null) {
				pref.addPreferenceChangeListener(new PreferenceChangeListener() {
					@Override
					public void preferenceChange(PreferenceChangeEvent event) {
						if (event.getKey().equals("TAB_WIDTH")) {
							_indentationLogic.setTabWidth(Integer.parseInt(event.getNewValue()));
						}
					}
				});
			}
		}

		initKeyMap();

		if (transferHandler == null) {
			transferHandler = new MyTransferHandler();
		}
		setTransferHandler(transferHandler);
		_highlighter = new EditorCurrentLineHighlighter(this);

		addCaretListener(_highlighter);

		setFont(new Font(pref.getFontName(), Font.PLAIN, pref.getFontSize()));
		setMargin(new Insets(3, 3, 3, 3));
		setBackground(Color.WHITE);
		if (!Settings.isMac()) {
			setSelectionColor(new Color(170, 200, 255));
		}

		updateDocumentListeners();

		addKeyListener(this);
		addCaretListener(this);
		popMenuImage = new SikuliIDEPopUpMenu("POP_IMAGE", this);
		if (!popMenuImage.isValidMenu()) {
			popMenuImage = null;
		}

		if (paneIsEmpty || reInit) {
//			this.setText(String.format(Settings.TypeCommentDefault, getSikuliContentType()));
			this.setText("");
		}

		Debug.log(3, "InitTab: %s :--: %s", getContentType(), getEditorKit());
	}

	public String getSikuliContentType() {
		return sikuliContentType;
	}

	public SikuliIDEPopUpMenu getPopMenuImage() {
		return popMenuImage;
	}

	private void updateDocumentListeners() {
		getDocument().addDocumentListener(new DirtyHandler());
		getDocument().addUndoableEditListener(getUndoManager());
	}

	public EditorUndoManager getUndoManager() {
		if (_undo == null) {
			_undo = new EditorUndoManager();
		}
		return _undo;
	}

	public IndentationLogic getIndentationLogic() {
		return _indentationLogic;
	}

	private void initKeyMap() {
		InputMap map = this.getInputMap();
		int shift = InputEvent.SHIFT_MASK;
		int ctrl = InputEvent.CTRL_MASK;
		map.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, shift), SikuliEditorKit.deIndentAction);
		map.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ctrl), SikuliEditorKit.deIndentAction);
	}

	@Override
	public void keyPressed(java.awt.event.KeyEvent ke) {
	}

	@Override
	public void keyReleased(java.awt.event.KeyEvent ke) {
	}

	@Override
	public void keyTyped(java.awt.event.KeyEvent ke) {
		//TODO implement code completion * checkCompletion(ke);
	}
  //</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="file handling">
	public String loadFile(boolean accessingAsFile) throws IOException {
		File file = new SikuliIDEFileChooser(SikuliIDE.getInstance(), accessingAsFile).load();
		if (file == null) {
			return null;
		}
		String fname = FileManager.slashify(file.getAbsolutePath(), false);
		SikuliIDE ide = SikuliIDE.getInstance();
		int i = ide.isAlreadyOpen(fname);
		if (i > -1) {
			Debug.log(2, "Already open in IDE: " + fname);
			return null;
		}
		loadFile(fname);
		if (_editingFile == null) {
			return null;
		}
		return fname;
	}

	public void loadFile(String filename) {
		filename = FileManager.slashify(filename, false);
		setSrcBundle(filename + "/");
		File script = new File(filename);
		_editingFile = FileManager.getScriptFile(script, null, null);
		editingType = _editingFile.getAbsolutePath().substring(_editingFile.getAbsolutePath().lastIndexOf(".") + 1);
		initBeforeLoad(editingType);
		try {
			this.read(new BufferedReader(new InputStreamReader(
							new FileInputStream(_editingFile), "UTF8")), null);
		} catch (Exception ex) {
			_editingFile = null;
		}
		if (_editingFile != null) {
			updateDocumentListeners();
			setDirty(false);
			_srcBundleTemp = false;
		} else {
			_srcBundlePath = null;
		}
	}

	public boolean hasEditingFile() {
		return _editingFile != null;
	}

	public String saveFile() throws IOException {
		if (_editingFile == null) {
			return saveAsFile(Settings.isMac());
		} else {
			ImagePath.remove(_srcBundlePath);
			writeSrcFile();
			return getCurrentShortFilename();
		}
	}

	public String saveAsFile(boolean accessingAsFile) throws IOException {
		File file = new SikuliIDEFileChooser(SikuliIDE.getInstance(), accessingAsFile).save();
		if (file == null) {
			return null;
		}
		String bundlePath = FileManager.slashify(file.getAbsolutePath(), false);
		if (!file.getAbsolutePath().endsWith(".sikuli")) {
			bundlePath += ".sikuli";
		}
		if (FileManager.exists(bundlePath)) {
			int res = JOptionPane.showConfirmDialog(
							null, SikuliIDEI18N._I("msgFileExists", bundlePath),
							SikuliIDEI18N._I("dlgFileExists"), JOptionPane.YES_NO_OPTION);
			if (res != JOptionPane.YES_OPTION) {
				return null;
			}
		} else {
			FileManager.mkdir(bundlePath);
		}
		try {
			saveAsBundle(bundlePath, (SikuliIDE.getInstance().getCurrentFileTabTitle()));
			if (Settings.isMac()) {
				if (!Settings.handlesMacBundles) {
					makeBundle(bundlePath, accessingAsFile);
				}
			}
		} catch (IOException iOException) {
		}
		return getCurrentShortFilename();
	}

	private void makeBundle(String path, boolean asFile) {
		String isBundle = asFile ? "B" : "b";
		IResourceLoader loader = FileManager.getNativeLoader("basic", new String[0]);
		String[] cmd = new String[]{"#SetFile", "-a", isBundle, path};
		loader.doSomethingSpecial("runcmd", cmd);
		if (!cmd[0].isEmpty()) {
			Debug.error("makeBundle: return: " + cmd[0]);
		}
		if (asFile) {
			if (!FileManager.writeStringToFile("/Applications/SikuliX-IDE.app",
							(new File(path, ".LSOverride")).getAbsolutePath())) {
				Debug.error("makeBundle: not possible: .LSOverride");
			}
		} else {
			new File(path, ".LSOverride").delete();
		}
	}

	private void saveAsBundle(String bundlePath, String current) throws IOException {
//TODO allow other file types
		bundlePath = FileManager.slashify(bundlePath, true);
		if (_srcBundlePath != null) {
			FileManager.xcopy(_srcBundlePath, bundlePath, current);
		}
		if (_srcBundleTemp) {
			FileManager.deleteTempDir(_srcBundlePath);
			_srcBundleTemp = false;
		}
		ImagePath.remove(_srcBundlePath);
		setSrcBundle(bundlePath);
		_editingFile = createSourceFile(bundlePath, "." + Settings.TypeEndings.get(sikuliContentType));
		Debug.log(3, "IDE: saveAsBundle: " + getSrcBundle());
		writeSrcFile();
		reparse();
	}

	private File createSourceFile(String bundlePath, String ext) {
		if (ext != null) {
			String name = new File(bundlePath).getName();
			name = name.substring(0, name.lastIndexOf("."));
			return new File(bundlePath, name + ext);
		} else {
			return new File(bundlePath);
		}
	}

	private void writeSrcFile() throws IOException {
		Debug.log(3, "IDE: writeSrcFile: " + _editingFile.getName());
		writeFile(_editingFile.getAbsolutePath());
		if (PreferencesUser.getInstance().getAtSaveMakeHTML()) {
			convertSrcToHtml(getSrcBundle());
		} else {
			String snameDir = new File(_editingFile.getAbsolutePath()).getParentFile().getName();
			String sname = snameDir.replace(".sikuli", "") + ".html";
			(new File(snameDir, sname)).delete();
		}
//TODO bundle image clean in Java
		if (Settings.CPYTHON.equals(getSikuliContentType()) && PreferencesUser.getInstance().getAtSaveCleanBundle()) {
			cleanBundle(getSrcBundle());
		}
		setDirty(false);
	}

	public String exportAsZip() throws IOException, FileNotFoundException {
		File file = new SikuliIDEFileChooser(SikuliIDE.getInstance()).export();
		if (file == null) {
			return null;
		}

		String zipPath = file.getAbsolutePath();
		String srcName = file.getName();
		if (!file.getAbsolutePath().endsWith(".skl")) {
			zipPath += ".skl";
		} else {
			srcName = srcName.substring(0, srcName.lastIndexOf('.'));
		}
		writeFile(getSrcBundle() + srcName + ".py");
		FileManager.zip(getSrcBundle(), zipPath);
		Debug.log(2, "export to executable file: " + zipPath);
		return zipPath;
	}

	private void writeFile(String filename) throws IOException {
		this.write(new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(filename), "UTF8")));
	}

	public boolean close() throws IOException {
		if (isDirty()) {
			Object[] options = {SikuliIDEI18N._I("yes"), SikuliIDEI18N._I("no"), SikuliIDEI18N._I("cancel")};
			int ans = JOptionPane.showOptionDialog(this,
							SikuliIDEI18N._I("msgAskSaveChanges", getCurrentShortFilename()),
							SikuliIDEI18N._I("dlgAskCloseTab"),
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null,
							options, options[0]);
			if (ans == JOptionPane.CANCEL_OPTION
							|| ans == JOptionPane.CLOSED_OPTION) {
				return false;
			} else if (ans == JOptionPane.YES_OPTION) {
				if (saveFile() == null) {
					return false;
				}
			} else {
				SikuliIDE.getInstance().getTabPane().resetLastClosed();
			}
			if (_srcBundleTemp) {
				FileManager.deleteTempDir(_srcBundlePath);
			}
			setDirty(false);
		}
		if (_srcBundlePath != null) {
			ImagePath.remove(_srcBundlePath);
		}
		return true;
	}

	private void setSrcBundle(String newBundlePath) {
		_srcBundlePath = newBundlePath;
		ImagePath.setBundlePath(_srcBundlePath);
	}

	public String getSrcBundle() {
		if (_srcBundlePath == null) {
			File tmp = FileManager.createTempDir();
			setSrcBundle(FileManager.slashify(tmp.getAbsolutePath(), true));
			_srcBundleTemp = true;
		}
		return _srcBundlePath;
	}

	public boolean isSourceBundleTemp() {
		return _srcBundleTemp;
	}

	public String getCurrentSrcDir() {
		if (_srcBundlePath != null) {
			if (_editingFile == null || _srcBundleTemp) {
				return null;
			} else {
				return _editingFile.getParent();
			}
		} else {
			return null;
		}
	}

	private String getCurrentShortFilename() {
		if (_srcBundlePath != null) {
			File f = new File(_srcBundlePath);
			return f.getName();
		}
		return "Untitled";
	}

	public File getCurrentFile() {
		return getCurrentFile(true);
	}

	public File getCurrentFile(boolean shouldSave) {
		if (shouldSave && _editingFile == null && isDirty()) {
			try {
				saveAsFile(Settings.isMac());
			} catch (IOException e) {
				Debug.error(me + "getCurrentFile: Problem while trying to save %s\n%s",
								_editingFile.getAbsolutePath(), e.getMessage());
			}
		}
		return _editingFile;
	}

	public String getCurrentFilename() {
		if (_editingFile == null) {
			return null;
		}
		return _editingFile.getAbsolutePath();
	}

	private void convertSrcToHtml(String bundle) {
		SikuliX.getScriptRunner("jython", null, null).doSomethingSpecial("convertSrcToHtml",
						new String[]{bundle});
	}

	private void cleanBundle(String bundle) {
		if (!PreferencesUser.getInstance().getAtSaveCleanBundle()) {
			return;
		}
		SikuliX.getScriptRunner("jython", null, null).doSomethingSpecial("cleanBundle",
						new String[]{bundle});
	}

	public File copyFileToBundle(String filename) {
		File f = new File(filename);
		String bundlePath = getSrcBundle();
		if (f.exists()) {
			try {
				File newFile = FileManager.smartCopy(filename, bundlePath);
				return newFile;
			} catch (IOException e) {
				Debug.error(me + "copyFileToBundle: Problem while trying to save %s\n%s",
								filename, e.getMessage());
				return f;
			}
		}
		return null;
	}

	public File getFileInBundle(String filename) {
		String fullpath = Image.create(filename).getFilename();
		if (fullpath != null) {
			return new File(fullpath);
		}
		return null;
	}

	public Image getImageInBundle(String filename) {
		return Image.createThumbNail(filename);
	}

//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="fill pane content">
	@Override
	public void read(Reader in, Object desc) throws IOException {
		super.read(in, desc);
		Document doc = getDocument();
		Element root = doc.getDefaultRootElement();
		parse(root);
		setCaretPosition(0);
	}
  //</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="Dirty handling">
	public boolean isDirty() {
		return _dirty;
	}

	public void setDirty(boolean flag) {
		if (_dirty == flag) {
			return;
		}
		_dirty = flag;
		//<editor-fold defaultstate="collapsed" desc="RaiMan no global dirty">
		if (flag) {
			//RaiManMod getRootPane().putClientProperty("Window.documentModified", true);
		} else {
			//SikuliIDE.getInstance().checkDirtyPanes();
		}
		//</editor-fold>
		SikuliIDE.getInstance().setCurrentFileTabTitleDirty(_dirty);
	}

	private class DirtyHandler implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent ev) {
			Debug.log(9, "change update");
			//setDirty(true);
		}

		@Override
		public void insertUpdate(DocumentEvent ev) {
			Debug.log(9, "insert update");
			setDirty(true);
		}

		@Override
		public void removeUpdate(DocumentEvent ev) {
			Debug.log(9, "remove update");
			setDirty(true);
		}
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Caret handling">
//TODO not used
	@Override
	public void caretUpdate(CaretEvent evt) {
		/* seems not to be used
		 * if (_can_update_caret_last_x) {
		 * _caret_last_x = -1;
		 * } else {
		 * _can_update_caret_last_x = true;
		 * }
		 */
	}

	public int getLineNumberAtCaret(int caretPosition) {
		Element root = getDocument().getDefaultRootElement();
		return root.getElementIndex(caretPosition) + 1;
	}

	public Element getLineAtCaret(int caretPosition) {
		Element root = getDocument().getDefaultRootElement();
		if (caretPosition == -1) {
			return root.getElement(root.getElementIndex(getCaretPosition()));
		} else {
			return root.getElement(root.getElementIndex(root.getElementIndex(caretPosition)));
		}
	}

	public Element getLineAtPoint(MouseEvent me) {
		Point p = me.getLocationOnScreen();
		Point pp = getLocationOnScreen();
		p.translate(-pp.x, -pp.y);
		int pos = viewToModel(p);
		Element root = getDocument().getDefaultRootElement();
		int e = root.getElementIndex(pos);
		if (e == -1) {
			return null;
		}
		return root.getElement(e);
	}

	public boolean jumpTo(int lineNo, int column) {
		Debug.log(6, "jumpTo: " + lineNo + "," + column);
		try {
			int off = getLineStartOffset(lineNo - 1) + column - 1;
			int lineCount = getDocument().getDefaultRootElement().getElementCount();
			if (lineNo < lineCount) {
				int nextLine = getLineStartOffset(lineNo);
				if (off >= nextLine) {
					off = nextLine - 1;
				}
			}
			if (off >= 0) {
				setCaretPosition(off);
			}
		} catch (BadLocationException ex) {
			jumpTo(lineNo);
			return false;
		}
		return true;
	}

	public boolean jumpTo(int lineNo) {
		Debug.log(6, "jumpTo: " + lineNo);
		try {
			setCaretPosition(getLineStartOffset(lineNo - 1));
		} catch (BadLocationException ex) {
			return false;
		}
		return true;
	}

	public int getLineStartOffset(int line) throws BadLocationException {
		// line starting from 0
		Element map = getDocument().getDefaultRootElement();
		if (line < 0) {
			throw new BadLocationException("Negative line", -1);
		} else if (line >= map.getElementCount()) {
			throw new BadLocationException("No such line", getDocument().getLength() + 1);
		} else {
			Element lineElem = map.getElement(line);
			return lineElem.getStartOffset();
		}
	}

	//<editor-fold defaultstate="collapsed" desc="TODO only used for UnitTest">
	public void jumpTo(String funcName) throws BadLocationException {
		Debug.log(6, "jumpTo: " + funcName);
		Element root = getDocument().getDefaultRootElement();
		int pos = getFunctionStartOffset(funcName, root);
		if (pos >= 0) {
			setCaretPosition(pos);
		} else {
			throw new BadLocationException("Can't find function " + funcName, -1);
		}
	}

	private int getFunctionStartOffset(String func, Element node) throws BadLocationException {
		Document doc = getDocument();
		int count = node.getElementCount();
		Pattern patDef = Pattern.compile("def\\s+" + func + "\\s*\\(");
		for (int i = 0; i < count; i++) {
			Element elm = node.getElement(i);
			if (elm.isLeaf()) {
				int start = elm.getStartOffset(), end = elm.getEndOffset();
				String line = doc.getText(start, end - start);
				Matcher matcher = patDef.matcher(line);
				if (matcher.find()) {
					return start;
				}
			} else {
				int p = getFunctionStartOffset(func, elm);
				if (p >= 0) {
					return p;
				}
			}
		}
		return -1;
	}
  //</editor-fold>
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="replace text patterns with image buttons">
	public boolean reparse() {
		File temp = null;
		Element e = this.getDocument().getDefaultRootElement();
		if (e.getEndOffset() - e.getStartOffset() == 1) {
			return true;
		}
		if ((temp = reparseBefore()) != null) {
			if (reparseAfter(temp)) {
			updateDocumentListeners();
			return true;
			}
		}
		return false;
	}

	public File reparseBefore() {
		Element e = this.getDocument().getDefaultRootElement();
		if (e.getEndOffset() - e.getStartOffset() == 1) {
			return null;
		}
		File temp = FileManager.createTempFile("reparse");
		try {
			writeFile(temp.getAbsolutePath());
			return temp;
		} catch (IOException ex) {
		}
		return null;
	}

	public boolean reparseCheckContent() {
		Element e = this.getDocument().getDefaultRootElement();
		String txt;
		if (e.getElementCount() > 2) {
			return false;
		} else if (e.getElement(1).getEndOffset() - e.getElement(1).getStartOffset() > 1) {
			return false;
		} else {
			int is = e.getElement(0).getStartOffset();
			int ie = e.getElement(0).getEndOffset();
			try {
				txt = e.getElement(0).getDocument().getText(is, ie - 1);
				if (txt.endsWith(Settings.TypeCommentToken)) {
					return true;
				}
			} catch (BadLocationException ex) {
				return false;
			}
		}
		return false;
	}

	public boolean reparseAfter(File temp) {
		try {
			this.read(new BufferedReader(new InputStreamReader(new FileInputStream(temp), "UTF8")), null);
			return true;
		} catch (IOException ex) {
		}
		return false;
	}

	private void parse(Element node) {
		if (!showThumbs) {
			// do not show any thumbnails
			return;
		}
		int count = node.getElementCount();
		for (int i = 0; i < count; i++) {
			Element elm = node.getElement(i);
			Debug.log(8, elm.toString());
			if (elm.isLeaf()) {
				int start = elm.getStartOffset(), end = elm.getEndOffset();
				parseRange(start, end);
			} else {
				parse(elm);
			}
		}
	}

	private int parseRange(int start, int end) {
		if (!showThumbs) {
			// do not show any thumbnails
			return end;
		}
		try {
			end = parseLine(start, end, patCaptureBtn);
			end = parseLine(start, end, patPatternStr);
			end = parseLine(start, end, patRegionStr);
			end = parseLine(start, end, patPngStr);
		} catch (BadLocationException e) {
			Debug.error(me + "parseRange: Problem while trying to parse line\n%s", e.getMessage());
		}
		return end;
	}

	private int parseLine(int startOff, int endOff, Pattern ptn) throws BadLocationException {
		if (endOff <= startOff) {
			return endOff;
		}
		Document doc = getDocument();
		while (true) {
			String line = doc.getText(startOff, endOff - startOff);
			Matcher m = ptn.matcher(line);
			//System.out.println("["+line+"]");
			if (m.find()) {
				int len = m.end() - m.start();
				if (replaceWithImage(startOff + m.start(), startOff + m.end(), ptn)) {
					startOff += m.start() + 1;
					endOff -= len - 1;
				} else {
					startOff += m.end() + 1;
				}
			} else {
				break;
			}
		}
		return endOff;
	}

	private boolean replaceWithImage(int startOff, int endOff, Pattern ptn)
					throws BadLocationException {
		Document doc = getDocument();
		String imgStr = doc.getText(startOff, endOff - startOff);
		JComponent comp = null;

		if (ptn == patPatternStr || ptn == patPngStr) {
			if (pref.getPrefMoreImageThumbs()) {
				comp = EditorPatternButton.createFromString(this, imgStr, null);
			} else {
				comp = EditorPatternLabel.labelFromString(this, imgStr);
			}
		} else if (ptn == patRegionStr) {
			if (pref.getPrefMoreImageThumbs()) {
				comp = EditorRegionButton.createFromString(this, imgStr);
			} else {
				comp = EditorRegionLabel.labelFromString(this, imgStr);
			}
		} else if (ptn == patCaptureBtn) {
			comp = EditorPatternLabel.labelFromString(this, "");
		}
		if (comp != null) {
			this.select(startOff, endOff);
			this.insertComponent(comp);
			return true;
		}
		return false;
	}

	public String getRegionString(int x, int y, int w, int h) {
		return String.format("Region(%d,%d,%d,%d)", x, y, w, h);
	}

	public String getPatternString(String ifn, float sim, Location off) {
		if (ifn == null) {
			return "\"" + EditorPatternLabel.CAPTURE + "\"";
		}
		String img = new File(ifn).getName();
		String pat = "Pattern(\"" + img + "\")";
		String ret = "";
		if (sim > 0) {
			if (sim >= 0.99F) {
				ret += ".exact()";
			} else if (sim != 0.7F) {
				ret += String.format(Locale.ENGLISH, ".similar(%.2f)", sim);
			}
		}
		if (off != null && (off.x != 0 || off.y != 0)) {
			ret += ".targetOffset(" + off.x + "," + off.y + ")";
		}
		if (!ret.equals("")) {
			ret = pat + ret;
		} else {
			ret = "\"" + img + "\"";
		}
		return ret;
	}
  //</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="content insert append">
	public void insertString(String str) {
		int sel_start = getSelectionStart();
		int sel_end = getSelectionEnd();
		if (sel_end != sel_start) {
			try {
				getDocument().remove(sel_start, sel_end - sel_start);
			} catch (BadLocationException e) {
				Debug.error(me + "insertString: Problem while trying to insert\n%s", e.getMessage());
			}
		}
		int pos = getCaretPosition();
		insertString(pos, str);
		int new_pos = getCaretPosition();
		int end = parseRange(pos, new_pos);
		setCaretPosition(end);
	}

	private void insertString(int pos, String str) {
		Document doc = getDocument();
		try {
			doc.insertString(pos, str, null);
		} catch (Exception e) {
			Debug.error(me + "insertString: Problem while trying to insert at pos\n%s", e.getMessage());
		}
	}

//TODO not used
	public void appendString(String str) {
		Document doc = getDocument();
		try {
			int start = doc.getLength();
			doc.insertString(doc.getLength(), str, null);
			int end = doc.getLength();
			//end = parseLine(start, end, patHistoryBtnStr);
		} catch (Exception e) {
			Debug.error(me + "appendString: Problem while trying to append\n%s", e.getMessage());
		}
	}
  //</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="feature search">
  /*
	 * public int search(Pattern pattern){
	 * return search(pattern, true);
	 * }
	 *
	 * public int search(Pattern pattern, boolean forward){
	 * if(!pattern.equals(_lastSearchPattern)){
	 * _lastSearchPattern = pattern;
	 * Document doc = getDocument();
	 * int pos = getCaretPosition();
	 * Debug.log("caret: "  + pos);
	 * try{
	 * String body = doc.getText(pos, doc.getLength()-pos);
	 * _lastSearchMatcher = pattern.matcher(body);
	 * }
	 * catch(BadLocationException e){
	 * e.printStackTrace();
	 * }
	 * }
	 * return continueSearch(forward);
	 * }
	 */

	/*
	 * public int search(String str){
	 * return search(str, true);
	 * }
	 */
	public int search(String str, int pos, boolean forward) {
		boolean isCaseSensitive = true;
		String toSearch = str;
		if (str.startsWith("!")) {
			str = str.substring(1).toUpperCase();
			isCaseSensitive = false;
		}
		int ret = -1;
		Document doc = getDocument();
		Debug.log(9, "search caret: " + pos + ", " + doc.getLength());
		try {
			String body;
			int begin;
			if (forward) {
				int len = doc.getLength() - pos;
				body = doc.getText(pos, len > 0 ? len : 0);
				begin = pos;
			} else {
				body = doc.getText(0, pos);
				begin = 0;
			}
			if (!isCaseSensitive) {
				body = body.toUpperCase();
			}
			Pattern pattern = Pattern.compile(Pattern.quote(str));
			Matcher matcher = pattern.matcher(body);
			ret = continueSearch(matcher, begin, forward);
			if (ret < 0) {
				if (forward && pos != 0) {
					return search(toSearch, 0, forward);
				}
				if (!forward && pos != doc.getLength()) {
					return search(toSearch, doc.getLength(), forward);
				}
			}
		} catch (BadLocationException e) {
			Debug.log(7, "search caret: " + pos + ", " + doc.getLength()
							+ e.getStackTrace());
		}
		return ret;
	}

	protected int continueSearch(Matcher matcher, int pos, boolean forward) {
		boolean hasNext = false;
		int start = 0, end = 0;
		if (!forward) {
			while (matcher.find()) {
				hasNext = true;
				start = matcher.start();
				end = matcher.end();
			}
		} else {
			hasNext = matcher.find();
			if (!hasNext) {
				return -1;
			}
			start = matcher.start();
			end = matcher.end();
		}
		if (hasNext) {
			Document doc = getDocument();
			getCaret().setDot(pos + end);
			getCaret().moveDot(pos + start);
			getCaret().setSelectionVisible(true);
			return pos + start;
		}
		return -1;
	}
  //</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="Transfer code incl. images between code panes">
	private class MyTransferHandler extends TransferHandler {

		private static final String me = "EditorPaneTransferHandler: ";
		Map<String, String> _copiedImgs = new HashMap<String, String>();

		@Override
		public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
			super.exportToClipboard(comp, clip, action);
		}

		@Override
		protected void exportDone(JComponent source,
						Transferable data,
						int action) {
			if (action == TransferHandler.MOVE) {
				JTextPane aTextPane = (JTextPane) source;
				int sel_start = aTextPane.getSelectionStart();
				int sel_end = aTextPane.getSelectionEnd();
				Document doc = aTextPane.getDocument();
				try {
					doc.remove(sel_start, sel_end - sel_start);
				} catch (BadLocationException e) {
					Debug.error(me + "exportDone: Problem while trying to remove text\n%s", e.getMessage());
				}
			}
		}

		@Override
		public int getSourceActions(JComponent c) {
			return COPY_OR_MOVE;
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			JTextPane aTextPane = (JTextPane) c;

			SikuliEditorKit kit = ((SikuliEditorKit) aTextPane.getEditorKit());
			Document doc = aTextPane.getDocument();
			int sel_start = aTextPane.getSelectionStart();
			int sel_end = aTextPane.getSelectionEnd();

			StringWriter writer = new StringWriter();
			try {
				_copiedImgs.clear();
				kit.write(writer, doc, sel_start, sel_end - sel_start, _copiedImgs);
				return new StringSelection(writer.toString());
			} catch (Exception e) {
				Debug.error(me + "createTransferable: Problem creating text to copy\n%s", e.getMessage());
			}
			return null;
		}

		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			for (int i = 0; i < transferFlavors.length; i++) {
				//System.out.println(transferFlavors[i]);
				if (transferFlavors[i].equals(DataFlavor.stringFlavor)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean importData(JComponent comp, Transferable t) {
			DataFlavor htmlFlavor = DataFlavor.stringFlavor;
			if (canImport(comp, t.getTransferDataFlavors())) {
				try {
					String transferString = (String) t.getTransferData(htmlFlavor);
					EditorPane targetTextPane = (EditorPane) comp;
					for (Map.Entry<String, String> entry : _copiedImgs.entrySet()) {
						String imgName = entry.getKey();
						String imgPath = entry.getValue();
						File destFile = targetTextPane.copyFileToBundle(imgPath);
						String newName = destFile.getName();
						if (!newName.equals(imgName)) {
							String ptnImgName = "\"" + imgName + "\"";
							newName = "\"" + newName + "\"";
							transferString = transferString.replaceAll(ptnImgName, newName);
							Debug.info(ptnImgName + " exists. Rename it to " + newName);
						}
					}
					targetTextPane.insertString(transferString);
				} catch (Exception e) {
					Debug.error(me + "importData: Problem pasting text\n%s", e.getMessage());
				}
				return true;
			}
			return false;
		}
	}
//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="currently not used">
	private String _tabString = "   ";

	private void setTabSize(int charactersPerTab) {
		FontMetrics fm = this.getFontMetrics(this.getFont());
		int charWidth = fm.charWidth('w');
		int tabWidth = charWidth * charactersPerTab;

		TabStop[] tabs = new TabStop[10];

		for (int j = 0; j < tabs.length; j++) {
			int tab = j + 1;
			tabs[j] = new TabStop(tab * tabWidth);
		}

		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setFontSize(attributes, 18);
		StyleConstants.setFontFamily(attributes, "Osaka-Mono");
		StyleConstants.setTabSet(attributes, tabSet);
		int length = getDocument().getLength();
		getStyledDocument().setParagraphAttributes(0, length, attributes, true);
	}

	private void setTabs(int spaceForTab) {
		String t = "";
		for (int i = 0; i < spaceForTab; i++) {
			t += " ";
		}
		_tabString = t;
	}

	private void expandTab() throws BadLocationException {
		int pos = getCaretPosition();
		Document doc = getDocument();
		doc.remove(pos - 1, 1);
		doc.insertString(pos - 1, _tabString, null);
	}
	private Class _historyBtnClass;

	private void setHistoryCaptureButton(ButtonCapture btn) {
		_historyBtnClass = btn.getClass();
	}

	private void indent(int startLine, int endLine, int level) {
		Document doc = getDocument();
		String strIndent = "";
		if (level > 0) {
			for (int i = 0; i < level; i++) {
				strIndent += "  ";
			}
		} else {
			Debug.error("negative indentation not supported yet!!");
		}
		for (int i = startLine; i < endLine; i++) {
			try {
				int off = getLineStartOffset(i);
				if (level > 0) {
					doc.insertString(off, strIndent, null);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void checkCompletion(java.awt.event.KeyEvent ke) throws BadLocationException {
		Document doc = getDocument();
		Element root = doc.getDefaultRootElement();
		int pos = getCaretPosition();
		int lineIdx = root.getElementIndex(pos);
		Element line = root.getElement(lineIdx);
		int start = line.getStartOffset(), len = line.getEndOffset() - start;
		String strLine = doc.getText(start, len - 1);
		Debug.log(9, "[" + strLine + "]");
		if (strLine.endsWith("find") && ke.getKeyChar() == '(') {
			ke.consume();
			doc.insertString(pos, "(", null);
			ButtonCapture btnCapture = new ButtonCapture(this, line);
			insertComponent(btnCapture);
			doc.insertString(pos + 2, ")", null);
		}

	}
  //</editor-fold>
}
