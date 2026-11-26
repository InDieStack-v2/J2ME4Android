/*
 * Taho fork of J2ME Loader: what a game is told when it asks for Bluetooth.
 *
 * Taho does not support Bluetooth multiplayer. The game never reaches the phone's Bluetooth adapter and the app declares no
 * Bluetooth permission. "off" answers like a phone with Bluetooth turned off (getLocalDevice throws); "nobody" answers like a
 * phone with Bluetooth on and no phone in range (searches finish empty, connections fail). The real Android Bluetooth code in
 * javax.bluetooth and org.microemu.cldc.bt* is kept, unreachable, for a later multiplayer feature.
 *
 * Licensed under the Apache License, Version 2.0, like the rest of J2ME Loader.
 */

package ru.woesss.j2me.taho;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class TahoBluetooth {
	/** "off" (default), "nobody" (fake: Bluetooth on, nobody nearby) or "real" (the phone's adapter). Set once, before the MIDlet starts; each game runs in its own process. */
	public static volatile String mode = "off";

	private TahoBluetooth() {
	}

	/** Sends a request to the app process, which owns the windows: kind is "permission", "enable" or "discoverable". */
	public interface Requester {
		void request(int id, String kind);
	}

	/** Set by the embedding service before the game starts. */
	public static volatile Requester requester;

	private static final long ASK_TIMEOUT_MS = 60_000;
	private static final Object askLock = new Object();
	private static int askId;
	private static CountDownLatch askLatch;
	private static boolean askResult;

	/**
	 * Asks the app to show a permission request or a system dialog, and waits for the player's answer (at most 60 s).
	 * One request at a time. False when denied, when it timed out or when there is nobody to ask. Never call on the
	 * thread that must answer the result (the binder thread calling onResult).
	 */
	public static boolean ask(String kind) {
		Requester r = requester;
		if (r == null) {
			return false;
		}
		synchronized (askLock) { // a second request waits for the first one's dialog to close
			int id = ++askId;
			CountDownLatch latch = new CountDownLatch(1);
			askResult = false;
			askLatch = latch;
			try {
				r.request(id, kind);
				latch.await(ASK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				return false;
			} finally {
				askLatch = null;
			}
			return askResult;
		}
	}

	/** The app's answer to request(id, ...). A late answer to an earlier, timed-out request is ignored. */
	public static void onResult(int id, boolean granted) {
		CountDownLatch latch = askLatch;
		if (latch != null && id == askId) {
			askResult = granted;
			latch.countDown();
		}
	}

	/**
	 * Changes the mode while the game runs (the player switched Bluetooth On or Off). Objects the game already holds keep the
	 * mode they were made in; the next LocalDevice.getLocalDevice() is made in the new one, so a game takes the change the next
	 * time it checks for Bluetooth (most do when the player opens their Bluetooth menu).
	 */
	public static void setMode(String value) {
		mode = "nobody".equals(value) || "real".equals(value) ? value : "off";
		javax.bluetooth.LocalDevice.tahoReset();
	}

	public static boolean nobody() {
		return "nobody".equals(mode);
	}

	/** True only in the Android player with Bluetooth On: the game gets the phone's real adapter. */
	public static boolean real() {
		return "real".equals(mode);
	}
}
