/*
 * Taho fork of J2ME Loader: starts a prepared game inside a MicroHost and exposes the few controls the
 * embedding app needs (keys, pause, root view). This replaces what MicroActivity.onCreate did.
 *
 * Licensed under the Apache License, Version 2.0, like the rest of J2ME Loader.
 */

package ru.woesss.j2me.taho;

import android.content.Context;
import android.view.View;

import java.io.File;
import java.util.LinkedHashMap;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Displayable;
import javax.microedition.shell.MicroHost;
import javax.microedition.shell.MicroLoader;
import javax.microedition.shell.MidletSystem;
import javax.microedition.shell.MidletThread;
import javax.microedition.util.ContextHolder;

import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.config.Config;
import ru.playsoftware.j2meloader.config.ProfileModel;
import ru.playsoftware.j2meloader.config.ProfilesManager;

public final class TahoMidletHost {
	private final MicroHost host;

	private TahoMidletHost(MicroHost host) {
		this.host = host;
	}

	/** Must be called on the main thread, once per process, with the game already prepared (TahoInstaller). */
	public static TahoMidletHost start(Context context, String gameId, int width, int height,
									   MicroHost.Listener listener) throws Exception {
		ContextHolder.setAppContext(context);
		File appDir = new File(Config.getAppDir(), gameId);
		File configDir = new File(Config.getConfigsDir(), gameId);
		if (!configDir.isDirectory() && !configDir.mkdirs()) {
			throw new IllegalStateException("Can't create " + configDir);
		}
		ProfileModel profile = new ProfileModel(configDir);
		profile.screenWidth = width;
		profile.screenHeight = height;
		profile.showKeyboard = false; // the app's own controls send the keys
		profile.touchInput = true;
		profile.forceFullscreen = true; // no J2ME Loader soft-key bar: the app's keypad sends the soft keys
		if (!ProfilesManager.saveConfig(profile)) {
			throw new IllegalStateException("Can't write the game's configuration");
		}

		MicroHost host = new MicroHost(context, R.style.TahoHostTheme, gameId);
		host.setListener(listener);
		ContextHolder.setCurrentActivity(host);
		ContextHolder.setVibration(true);
		MidletSystem.setProperty("com.nokia.mid.cmdline.instance", "1");

		MicroLoader loader = new MicroLoader(host, appDir.getAbsolutePath());
		if (!loader.init()) {
			throw new IllegalStateException("The game's configuration could not be loaded");
		}
		loader.applyConfiguration();
		LinkedHashMap<String, String> midlets = loader.loadMIDletList();
		if (midlets.isEmpty()) {
			throw new IllegalStateException("No MIDlets found");
		}
		MidletThread.create(loader, midlets.keySet().iterator().next());
		// MicroActivity.onResume did this: the MIDlet is only started once the host is visible.
		host.setVisible(true);
		return new TahoMidletHost(host);
	}

	public View getRootView() {
		return host.getRootView();
	}

	/** A MIDP key code (Canvas.KEY_*, or an ASCII digit, star, pound). */
	public void key(int midpCode, boolean down) {
		Displayable current = host.getCurrent();
		if (!(current instanceof Canvas)) {
			return;
		}
		Canvas canvas = (Canvas) current;
		if (down) {
			canvas.postKeyPressed(midpCode);
		} else {
			canvas.postKeyReleased(midpCode);
		}
	}

	public void setPaused(boolean paused) {
		host.setVisible(!paused);
	}
}
