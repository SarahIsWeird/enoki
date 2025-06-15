package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.gui.Size;

/**
 * Implementing this interface indicates that a Widget can be resized by the layout engine. The engine will typically
 * call setSize with a concrete size on the primary axis, and ask the control to make its best judgement along the other
 * axis.
 */
public interface Resizeable {
	
	/**
	 * Gets the current width of this Widget
	 * @return the width of this Widget, in layout pixels.
	 */
	int getWidth();
	
	/**
	 * Gets the current height of this Widget
	 * @return the height of this Widget, in layout pixels.
	 */
	int getHeight();
	
	/**
	 * Sets the width of this widget. May not be negative.
	 * @param width a new concrete width for this Widget.
	 */
	void setWidth(int width);
	
	/**
	 * Sets the height of this widget. May not be negative.
	 * @param height a new concrete height for this Widget.
	 */
	void setHeight(int height);
	
	/**
	 * Asks the Widget to change its size.
	 * @param width  The new width of the Widget, or -1 to ask the Widget to select an appropriate size.
	 * @param height The new height of the Widget, or -1 to ask the Widget to select an appropriate size.
	 */
	default void setSize(int width, int height) {
		if (width != -1) this.setWidth(width);
		if (height != -1) this.setHeight(height);
	}
	
	default void setSize(Size size) {
		setSize(size.width(), size.height());
	}
}
