package org.sikuli.ide;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.sikuli.basics.Debug;
import org.sikuli.script.Image;
import org.sikuli.script.ImagePath;

public class SikuliIDEPopUpMenu extends JPopupMenu {

  private static String me = "SikuliIDEPopUpMenu";
  private static int lvl = 3;
  private String popType;
  private boolean validMenu = true;

  public static final String POP_TAB = "POP_TAB";
  private CloseableTabbedPane refTab;
  public static final String POP_IMAGE = "POP_IMAGE";
  private EditorPane refEditorPane = null;
  public static final String POP_LINE = "POP_LINE";
  private EditorLineNumberView refLineNumberView = null;

  private MouseEvent mouseTrigger;

  /**
   * Get the value of isValidMenu
   *
   * @return the value of isValidMenu
   */
  public boolean isValidMenu() {
    return validMenu;
  }

  private static void log(int level, String message, Object... args) {
    Debug.logx(level, "", me + ": " + message, args);
  }

  public SikuliIDEPopUpMenu(String pType, Object ref) {
    popType = pType;
    init(ref);
  }

  private void init(Object ref) {
    if (popType.equals(POP_TAB)) {
      refTab = (CloseableTabbedPane) ref;
      popTabMenu();
    } else if (popType.equals(POP_IMAGE)) {
      refEditorPane = (EditorPane) ref;
      popImageMenu();
    } else if (popType.equals(POP_LINE)) {
      refLineNumberView = (EditorLineNumberView) ref;
      popLineMenu();
    } else {
      validMenu = false;
    }
    if (!validMenu) {
      return;
    }
  }

  public void doShow(CloseableTabbedPane comp, MouseEvent me) {
    mouseTrigger = me;
    show(comp, me.getX(), me.getY());
  }

  private void fireIDEFileMenu(String name) throws NoSuchMethodException {
    fireIDEMenu(SikuliIDE.getInstance().getFileMenu(), name);
  }

  private void fireIDERunMenu(String name) throws NoSuchMethodException {
    fireIDEMenu(SikuliIDE.getInstance().getRunMenu(), name);
  }

  private void fireIDEMenu(JMenu menu, String name) throws NoSuchMethodException {
    JMenuItem jmi;
    String jmiName = null;
    for (int i = 0; i < menu.getItemCount(); i++) {
      jmi = menu.getItem(i);
      if (jmi == null || jmi.getName() == null) {
        continue;
      }
      jmiName = jmi.getName();
      if (jmiName.equals(name)) {
        jmi.doClick();
      }
    }
    if (jmiName == null) {
      log(-1, "IDEFileMenu not found: " + name);
    }
  }

  private void fireInsertTabAndLoad(int tabIndex) {
    SikuliIDE.FileAction insertNewTab = SikuliIDE.getInstance().getFileAction(tabIndex);
    insertNewTab.doInsert(null);
  }

  private JMenuItem createMenuItem(JMenuItem item, ActionListener listener) {
    item.addActionListener(listener);
    return item;
  }

  private JMenuItem createMenuItem(String name, ActionListener listener) {
    return createMenuItem(new JMenuItem(name), listener);
  }

  private void setMenuText(int index, String text) {
    ((JMenuItem) getComponent(index)).setText(text);
  }

  private String getMenuText(int index) {
    return ((JMenuItem) getComponent(index)).getText();
  }

  private void setMenuEnabled(int index, boolean enabled) {
    ((JMenuItem) getComponent(index)).setEnabled(enabled);
  }

  class MenuAction implements ActionListener {

    protected Method actMethod = null;
    protected String action;

    public MenuAction() {
    }

    public MenuAction(String item) throws NoSuchMethodException {
      Class[] paramsWithEvent = new Class[1];
      try {
        paramsWithEvent[0] = Class.forName("java.awt.event.ActionEvent");
        actMethod = this.getClass().getMethod(item, paramsWithEvent);
        action = item;
      } catch (ClassNotFoundException cnfe) {
        log(-1, "Can't find menu action: %s\n" + cnfe, item);
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (actMethod != null) {
        try {
          log(lvl, "MenuAction." + action);
          Object[] params = new Object[1];
          params[0] = e;
          actMethod.invoke(this, params);
        } catch (Exception ex) {
          log(-1, "Problem when trying to invoke menu action %s\nError: %s",
                  action, ex.getMessage());
        }
      }
    }
  }

  private void popTabMenu() {
    try {
      add(createMenuItem("Set Type", new PopTabAction(PopTabAction.SET_TYPE)));
      addSeparator();
      add(createMenuItem("Move Tab", new PopTabAction(PopTabAction.MOVE_TAB)));
      add(createMenuItem("Duplicate", new PopTabAction(PopTabAction.DUPLICATE)));
      add(createMenuItem("Open", new PopTabAction(PopTabAction.OPEN)));
      add(createMenuItem("Open left", new PopTabAction(PopTabAction.OPENL)));
      addSeparator();
      add(createMenuItem("Save", new PopTabAction(PopTabAction.SAVE)));
      add(createMenuItem("SaveAs", new PopTabAction(PopTabAction.SAVE_AS)));
      addSeparator();
      add(createMenuItem("Run", new PopTabAction(PopTabAction.RUN)));
      add(createMenuItem("Run Slowly", new PopTabAction(PopTabAction.RUN_SLOW)));
      addSeparator();
      add(createMenuItem("Reset", new PopTabAction(PopTabAction.RESET)));

    } catch (NoSuchMethodException ex) {
      validMenu = false;
    }
  }

  class PopTabAction extends MenuAction {

    static final String SET_TYPE = "doSetType";
    static final String MOVE_TAB = "doMoveTab";
    static final String DUPLICATE = "doDuplicate";
    static final String OPEN = "doOpen";
    static final String OPENL = "doOpenLeft";
    static final String SAVE = "doSave";
    static final String SAVE_AS = "doSaveAs";
    static final String RUN = "doRun";
    static final String RUN_SLOW = "doRunSlow";
    static final String RESET = "doReset";

    public PopTabAction() {
      super();
    }

    public PopTabAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void doSetType(ActionEvent ae) {
			Debug.log(3, "doSetType: selected");
		}

    public void doMoveTab(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "doMoveTab: entered");
      if (getMenuText(0).contains("Insert")) {
        log(lvl, "doMoveTab: insert");
        doLoad(refTab.getSelectedIndex()+1);
        setMenuText(0, "Move Tab");
        setMenuText(3, "Open left");
        return;
      }
      refTab.resetLastClosed();
      boolean success = refTab.fireCloseTab(mouseTrigger, refTab.getSelectedIndex());
      log(lvl, "doMoveTab: success = %s", success);
      if (success && refTab.getLastClosed() != null) {
        setMenuText(0, "Insert Tab");
        setMenuText(3, "Insert Left");
      }
    }

    public void doDuplicate(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "doDuplicate: entered");
      fireIDEFileMenu("SAVE");
      fireIDEFileMenu("SAVE_AS");
      setMenuText(3, "Insert left");
      doOpenLeft(null);
    }

    private boolean doLoad(int tabIndex) {
      boolean success = true;
      fireInsertTabAndLoad(tabIndex);
      return success;
    }

    public void doOpen(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "doOpen: entered");
      refTab.resetLastClosed();
      doLoad(refTab.getSelectedIndex()+1);
    }

    public void doOpenLeft(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "doOpenLeft: entered");
      if (getMenuText(3).contains("Insert")) {
        log(lvl, "doMoveTab: insert left");
        doLoad(refTab.getSelectedIndex());
        setMenuText(0, "Move Tab");
        setMenuText(3, "Open left");
        return;
      }
      refTab.resetLastClosed();
      doLoad(refTab.getSelectedIndex());
    }

    public void doSave(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "doSave: entered");
      fireIDEFileMenu("SAVE");
    }

    public void doSaveAs(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "doSaveAs: entered");
      fireIDEFileMenu("SAVE_AS");
    }

    public void doRun(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "doRun: entered");
      fireIDERunMenu("RUN");
    }

    public void doRunSlow(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "doRunSlow: entered");
      fireIDERunMenu("RUN_SLOWLY");
    }

    public void doReset(ActionEvent ae) throws NoSuchMethodException {
      log(lvl, "Reset: entered");
      Image.dump();
      ImagePath.reset();
      Image.dump();
  }
}

  private void popImageMenu() {
    try {
      add(createMenuItem("Preview", new PopImageAction(PopImageAction.PREVIEW)));
    } catch (NoSuchMethodException ex) {
      validMenu = false;
    }
  }

  class PopImageAction extends MenuAction {

    static final String PREVIEW = "doPreview";

    public PopImageAction() {
      super();
    }

    public PopImageAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void doPreview(ActionEvent ae) {
      log(lvl, "doPreview:");
    }
  }

  private void popLineMenu() {
    try {
      add(createMenuItem("Action", new PopLineAction(PopLineAction.ACTION)));
    } catch (NoSuchMethodException ex) {
      validMenu = false;
    }
  }

  class PopLineAction extends MenuAction {

    static final String ACTION = "doAction";

    public PopLineAction() {
      super();
    }

    public PopLineAction(String item) throws NoSuchMethodException {
      super(item);
    }

    public void doAction(ActionEvent ae) {
      log(lvl, "doAction:");
    }
  }
}
