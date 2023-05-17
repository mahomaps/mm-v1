package mahomaps.ui;

import java.util.Vector;

public abstract class UIComposite extends UIElement {
	public final Vector children = new Vector();
	
	public UIComposite() {
		
	}
	public UIComposite(UIElement[] elems) {
		for (int i = 0; i < elems.length; i++) {
			children.addElement(elems[i]);
		}
	}
}
