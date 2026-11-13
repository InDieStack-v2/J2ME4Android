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

public final class TahoBluetooth {
	/** False: no mode reaches the phone's Bluetooth. A later multiplayer feature adds a "real" mode and turns this into a check of it. */
	public static final boolean REAL = false;

	/** "off" (default) or "nobody". Set once, before the MIDlet starts; each game runs in its own process. */
	public static volatile String mode = "off";

	private TahoBluetooth() {
	}

	public static boolean nobody() {
		return "nobody".equals(mode);
	}
}
