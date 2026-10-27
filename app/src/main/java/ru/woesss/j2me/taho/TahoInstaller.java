/*
 * Taho fork of J2ME Loader: headless install. Converts a JAR (and optional JAD) into the app directory
 * layout the runtime loads (converted.dex, res.jar, converted.dex.conf, icon.png). This is the
 * conversion core of AppInstaller without the database, dialogs, downloads or app list.
 *
 * Licensed under the Apache License, Version 2.0, like the rest of J2ME Loader.
 */

package ru.woesss.j2me.taho;

import android.util.Log;

import com.android.dx.command.dexer.Main;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import ru.playsoftware.j2meloader.config.Config;
import ru.playsoftware.j2meloader.util.FileUtils;
import ru.playsoftware.j2meloader.util.IOUtils;
import ru.playsoftware.j2meloader.util.ZipUtils;
import ru.woesss.j2me.installer.ConverterException;
import ru.woesss.j2me.jar.Descriptor;

public final class TahoInstaller {
	private static final String TAG = TahoInstaller.class.getSimpleName();

	private TahoInstaller() {
	}

	/**
	 * @param jar    the game's JAR
	 * @param jadText the game's JAD text, or null when it has none
	 * @param outDir an empty directory that receives the converted game
	 */
	public static void prepare(File jar, String jadText, File outDir) throws IOException, ConverterException {
		if (!outDir.isDirectory() && !outDir.mkdirs()) {
			throw new ConverterException("Can't create directory: '" + outDir + "'");
		}
		Descriptor desc = loadManifest(jar);
		if (jadText != null) {
			desc.merge(new Descriptor(jadText, true));
		}
		// dx chooses how to read its input from the file extension, and the app stores the game as a file named
		// "game". Convert the copy that becomes res.jar anyway.
		File resJar = new File(outDir, Config.MIDLET_RES_FILE);
		FileUtils.copyFileUsingChannel(jar, resJar);
		try {
			Main.main(new String[]{"--no-optimize", "--core-library",
					"--output=" + outDir + Config.MIDLET_DEX_FILE, resJar.getAbsolutePath()});
		} catch (Throwable e) {
			throw new ConverterException("Dexing error", e);
		}
		String icon = desc.getIcon();
		if (icon != null) {
			File iconFile = new File(outDir, Config.MIDLET_ICON_FILE);
			try {
				ZipUtils.unzipEntry(resJar, icon, iconFile);
			} catch (IOException e) {
				Log.w(TAG, "Can't unzip icon: " + icon, e);
				//noinspection ResultOfMethodCallIgnored
				iconFile.delete();
			}
		}
		desc.writeTo(new File(outDir, Config.MIDLET_MANIFEST_FILE));
	}

	private static Descriptor loadManifest(File jar) throws IOException {
		try (ZipFile zip = new ZipFile(jar)) {
			FileHeader manifest = zip.getFileHeader(JarFile.MANIFEST_NAME);
			if (manifest == null) {
				throw new IOException("JAR not have " + JarFile.MANIFEST_NAME);
			}
			try (java.io.InputStream is = zip.getInputStream(manifest)) {
				return new Descriptor(new String(IOUtils.toByteArray(is)), false);
			}
		}
	}
}
