package mahomaps;

import com.nokia.mid.ui.DeviceControl;

public class DeviceControlInvoker {
	
	public static boolean screenSaverPreventionSupported;
	public static boolean setLightsSupported;
	
	static {
		try {
			Class.forName("com.nokia.mid.ui.DeviceControl");
			setLightsSupported = true;
			if (System.getProperty("com.nokia.mid.ui.version") != null &&
					System.getProperty("com.nokia.mid.ui.screensaverprevention") != null) {
				screenSaverPreventionSupported = true;
			}
		} catch (Exception e) {
		}
	}
	
	public static void resetUserInactivityTime() {
		if (!screenSaverPreventionSupported) {
			if(!setLightsSupported) return;
			try {
				DeviceControl.setLights(0, 100);
			} catch (Throwable e) {
			}
			return;
		}
		try {
			DeviceControl.resetUserInactivityTime();
		} catch (Throwable e) {
		}
	}

}
