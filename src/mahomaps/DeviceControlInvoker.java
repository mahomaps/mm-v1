package mahomaps;

import com.nokia.mid.ui.DeviceControl;

public class DeviceControlInvoker {
	
	public static boolean supported;
	
	static {
		try {
			if (System.getProperty("com.nokia.mid.ui.version") != null &&
					System.getProperty("com.nokia.mid.ui.screensaverprevention") != null) {
				Class.forName("com.nokia.mid.ui.DeviceControl");
				supported = true;
			}
		} catch (Exception e) {
		}
	}
	
	public static void resetUserInactivityTime() {
		if (!supported) return;
		try {
			DeviceControl.resetUserInactivityTime();
		} catch (Throwable e) {
		}
	}

}
