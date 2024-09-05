package mahomaps.ui;

import mahomaps.MahoMapsApp;

/**
 * Button with "close" text and zero ID.
 */
public class CloseButton extends Button {

	public CloseButton(IButtonHandler handler) {
		super(MahoMapsApp.text[38], 0, handler);
	}

}
