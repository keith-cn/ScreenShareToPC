package com.adakoda.android.asm;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import java.io.File;

public class ADB {
	private AndroidDebugBridge mAndroidDebugBridge;

	public boolean initialize() {
		boolean success = true;

		String adbLocation = System
				.getProperty("com.android.screenshot.bindir");

		if (success) {
			if ((adbLocation != null) && (adbLocation.length() != 0))
				adbLocation = adbLocation + File.separator + "adb";
			else {
				adbLocation = "adb";
			}
			AndroidDebugBridge.init(false);
			this.mAndroidDebugBridge = AndroidDebugBridge.createBridge(
					adbLocation, true);
			if (this.mAndroidDebugBridge == null) {
				success = false;
			}
		}

		if (success) {
			int count = 0;
			while (!this.mAndroidDebugBridge.hasInitialDeviceList()) {
				try {
					Thread.sleep(100L);
					count++;
				} catch (InterruptedException localInterruptedException) {
				}
				if (count > 100) {
					success = false;
					break;
				}
			}
		}

		if (!success) {
			terminate();
		}

		return success;
	}

	public void terminate() {
		AndroidDebugBridge.terminate();
	}

	public IDevice[] getDevices() {
		IDevice[] devices = (IDevice[]) null;
		if (this.mAndroidDebugBridge != null) {
			devices = this.mAndroidDebugBridge.getDevices();
		}
		return devices;
	}
}
