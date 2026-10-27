/*
 * Taho fork of J2ME Loader: the host the MIDP runtime runs in, in place of MicroHost.
 * It is a themed Context (so LCDUI views can be built from it) that owns the container the current
 * Displayable is shown in. It has no window of its own: the embedding service attaches getRootView() to a
 * SurfaceControlViewHost, and the app's own screen supplies keys, touch, orientation and chrome.
 *
 * Licensed under the Apache License, Version 2.0, like the rest of J2ME Loader.
 */

package javax.microedition.shell;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.StringRes;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.ViewHandler;
import javax.microedition.lcdui.event.SimpleEvent;
import javax.microedition.lcdui.overlay.OverlayView;

public class MicroHost extends ContextThemeWrapper {
	/** Notifications for the embedding service. Called on the main thread. */
	public interface Listener {
		/** The first Displayable has been put on screen. */
		void onFirstFrame();

		void onExit();
	}

	private final Handler ui = new Handler(Looper.getMainLooper());
	private final FrameLayout rootView;
	private final FrameLayout displayableContainer;
	private final OverlayView overlayView;
	private final String appName;
	private Listener listener;
	private Displayable current;
	private volatile boolean visible = true;
	private boolean firstFrameSent;

	public MicroHost(Context base, int theme, String appName) {
		super(base, theme);
		this.appName = appName;
		rootView = new FrameLayout(this);
		displayableContainer = new FrameLayout(this);
		overlayView = new OverlayView(this, null);
		rootView.addView(displayableContainer, new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		rootView.addView(overlayView, new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public View getRootView() {
		return rootView;
	}

	public OverlayView getOverlayView() {
		return overlayView;
	}

	public String getAppName() {
		return appName;
	}

	public void setCurrent(Displayable displayable) {
		ViewHandler.postEvent(new SetCurrentEvent(current, displayable));
		current = displayable;
	}

	public Displayable getCurrent() {
		return current;
	}

	public boolean isVisible() {
		return visible;
	}

	/** The app's Play screen pauses and resumes the game; this mirrors MicroHost.onPause / onResume. */
	public void setVisible(boolean visible) {
		this.visible = visible;
		if (visible) {
			MidletThread.resumeApp();
		} else {
			MidletThread.pauseApp();
		}
	}

	public void runOnUiThread(Runnable action) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			action.run();
		} else {
			ui.post(action);
		}
	}

	public void toast(@StringRes int message) {
		runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
	}

	/** The MIDlet ended itself (notifyDestroyed). */
	public void finish() {
		if (listener != null) {
			listener.onExit();
		}
	}

	// Things MicroHost did through the window or other activities. The app owns all of them now.
	public void setTitle(CharSequence title) {
	}

	public void openOptionsMenu() {
	}

	@Override
	public void startActivity(Intent intent) {
	}

	public void startActivityForResult(Intent intent, int requestCode) {
	}

	private class SetCurrentEvent extends SimpleEvent {
		private final Displayable current;
		private final Displayable next;

		private SetCurrentEvent(Displayable current, Displayable next) {
			this.current = current;
			this.next = next;
		}

		@Override
		public void process() {
			if (current != null) {
				current.clearDisplayableView();
			}
			if (next instanceof Alert) {
				return;
			}
			displayableContainer.removeAllViews();
			if (next != null) {
				displayableContainer.addView(next.getDisplayableView());
				if (!firstFrameSent && listener != null) {
					firstFrameSent = true;
					displayableContainer.post(listener::onFirstFrame);
				}
			}
		}
	}
}
