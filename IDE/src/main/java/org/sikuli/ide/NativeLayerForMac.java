/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2013
 */
package org.sikuli.ide;

import com.apple.eawt.*;
import java.io.File;
import java.util.List;
import org.sikuli.basics.Debug;

// http://lists.apple.com/archives/mac-games-dev/2001/Sep/msg00113.html
// full key table: http://www.mactech.com/articles/mactech/Vol.04/04.12/Macinkeys/
// modifiers code: http://www.mactech.com/macintosh-c/chap02-1.html
public class NativeLayerForMac implements NativeLayer, AboutHandler, PreferencesHandler, OpenFilesHandler, QuitHandler {
  
  private static final String me = "NativeLayerForMac: ";

	@Override
	public void initIDE(final SikuliIDE ide) {
	}

  @Override
  public void handleAbout(AppEvent.AboutEvent evt) {
    Debug.log(5, "NativeLayerforMac: aboutHandler entered");
    SikuliIDE.getInstance().doAbout();
  }

  @Override
	public void handlePreferences(AppEvent.PreferencesEvent evt) {
			SikuliIDE.getInstance().showPreferencesWindow();
	}

	@Override
	public void openFiles(AppEvent.OpenFilesEvent evt) {
		List<File> fnameList = evt.getFiles();
		final String fname = fnameList.get(0).getPath();
		Debug.log(2, "opening " + fname);
    if (fname.endsWith(".skl")) {
			SikuliIDE._runningSkl = true;
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						SikuliIDE.main(new String[] {fname});
					} catch (Exception e) {
            Debug.error(me + "openFiles: Problem starting IDE\n%s", e.getMessage());
					}
				}
			};
			t.setDaemon(false);
			t.start();
		} else if (fname.endsWith(".sikuli")) {
			SikuliIDE ide = SikuliIDE.getInstance(null);
			ide.restoreScriptFromSession(fname);
		}
	}

	@Override
	public void handleQuitRequestWith(AppEvent.QuitEvent evt, QuitResponse resp) {
		if (! SikuliIDE.getInstance().quit()) resp.cancelQuit();
		else resp.performQuit();
	}

	@Override
	public void initApp() {

		Application.getApplication().setAboutHandler(this);
		Application.getApplication().setPreferencesHandler(this);
		Application.getApplication().setOpenFileHandler(this);
		Application.getApplication().setQuitHandler(this);


		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Sikuli IDE");
	}
}
