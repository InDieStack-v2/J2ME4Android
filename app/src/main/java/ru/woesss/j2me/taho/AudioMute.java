/*
 * Taho fork of J2ME Loader: one switch that silences every player the game creates, in place of the system
 * volume (which the app must not touch). Players register to be told when it changes.
 *
 * Licensed under the Apache License, Version 2.0, like the rest of J2ME Loader.
 */

package ru.woesss.j2me.taho;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class AudioMute {
	public interface Listener {
		void onMuteChanged();
	}

	private static volatile boolean muted;
	private static final Set<Listener> listeners = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	private AudioMute() {
	}

	public static boolean isMuted() {
		return muted;
	}

	public static float scale(float volume) {
		return muted ? 0f : volume;
	}

	public static void register(Listener listener) {
		listeners.add(listener);
	}

	public static void setMuted(boolean value) {
		if (muted == value) {
			return;
		}
		muted = value;
		ArrayList<Listener> copy;
		synchronized (listeners) {
			copy = new ArrayList<>(listeners);
		}
		for (Listener listener : copy) {
			listener.onMuteChanged();
		}
	}
}
