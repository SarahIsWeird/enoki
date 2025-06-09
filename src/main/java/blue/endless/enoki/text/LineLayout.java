package blue.endless.enoki.text;

import blue.endless.enoki.gui.Position;
import blue.endless.enoki.gui.Size;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.widget.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * Lays out flow (non-block) elements along a line. Can be reused for multiple lines.
 * 
 * <p>To use this class, first configure it if desired, such as
 * <code><pre>
 * LineLayout layout = new LineLayout(Position.ZERO, 320, ScreenAxis.HORIZONTAL)
 *     .setAdditionalPadding(4)
 *     .setCrossAxisAlignment(TRAILING);
 * </pre></code>
 * 
 * Next, perform line wrapping operations and add widgets. (This is a toy example! Assumes all nodes are text!)
 * <code><pre>
 * for(DocNode n : textNodes) {
 *     int remainingSpace = layout.remainingSpacePlusPadding();
 *     String lineText = wordWrap.getFirstLine(font, remainingSpace, n.text(), n.style());
 *     TextSpanWidget widget = new TextSpanWidget(0, 0, Text.literal(lineText), n.style(), font);
 *     layout.add(widget);
 * }
 * </pre></code>
 * 
 * Finally, call the layout and advance the position:
 * <pre><code>
 * ScreenRect layoutRect = layout.layout();
 * layout.advanceLine(layoutRect);</pre></code>
 * 
 * Now the widgets are positioned along the line, and the layout is ready to assemble the next line.
 */
public class LineLayout {
	protected Position startPosition;
	protected int availableOnPrimaryAxis;
	protected ScreenAxis axis;
	
	protected List<Widget> tray = new ArrayList<>();
	protected int sizeSoFar = 0;
	protected int additionalPadding = 0;
	protected boolean justify = false;
	protected Alignment crossAxisAlign = Alignment.CENTER;
	
	public LineLayout(Position startPosition, int layoutSize, ScreenAxis layoutAxis) {
		this.startPosition = startPosition;
		this.availableOnPrimaryAxis = layoutSize;
		this.axis = layoutAxis;
	}
	
	/**
	 * Sets additonal padding between elements along the line's primary axis. Generally should be set to zero. Defaults
	 * to zero.
	 * @param additionalPadding the number of "layout pixels" of minimum extra space to ensure between elements
	 *                          along the primary axis.
	 * @return this layout for further configuration.
	 */
	public LineLayout setAdditionalPadding(int additionalPadding) {
		this.additionalPadding = additionalPadding;
		return this;
	}
	
	/**
	 * If true, this layout will justify text and other elements laid out along the primary axis. Defaults to false.
	 * @param justify if true, justify line elements. If false, elements will be packed to the leading edge.
	 * @return this layout for further configuration.
	 */
	public LineLayout setJustify(boolean justify) {
		this.justify = justify;
		return this;
	}
	
	/**
	 * Sets the behavior that happens along the "cross" or non-primary axis. Defaults to CENTER.
	 * @param alignment The alignment that will be used to arrange elements on the cross axis of the line.
	 * @return this layout for further configuration.
	 */
	public LineLayout setCrossAxisAlignment(Alignment alignment) {
		this.crossAxisAlign = alignment;
		return this;
	}
	
	/**
	 * Sets the start position - the top left corner of the eventual layout box for this line.
	 */
	public LineLayout setStartPosition(Position pos) {
		this.startPosition = pos;
		return this;
	}
	
	/**
	 * Sets the number of layout pixels available along the line's primary axis (usually its width).
	 * @param size The amount of room available for layout operations
	 * @return this layout for further configuration.
	 */
	public LineLayout setLayoutSize(int size) {
		this.availableOnPrimaryAxis = size;
		return this;
	}
	
	/**
	 * Clears the line, preparing the layout for reuse, but does not alter the start position.
	 */
	public void clearLine() {
		tray.clear();
		sizeSoFar = 0;
	}
	
	/**
	 * Clears the line, and, given the rect from the previous layout operation, updates the start position to the start
	 * of the next line.
	 * @param rect A ScreenRect representing a previously laid-out line. The magnitude of its cross-axis size will be
	 *             added to this line's starting position.
	 */
	public void advanceLine(ScreenRect rect) {
		clearLine();
		int primaryPosition = startPosition.get(axis); // Stays the same for each line
		int advanceSize = axis.opposite().select(rect.getLeft(), rect.getTop());
		int newCrossPosition = startPosition.get(axis.opposite()) + advanceSize;
		startPosition = axis.orientCoordinates(primaryPosition, newCrossPosition);
	}
	
	/**
	 * Gets the amount of space on this line that has not been laid out along its primary axis.
	 * 
	 * <p>NOTE: This method is really for advanced use cases, and where possible, {@link #canFit(Widget)}
	 * should be used instead, as that ensures that not only can the component be inserted, but that it can be inserted
	 * without violating any invariants (such as padding between components).
	 * @return The amount of "free space", in layout pixels.
	 */
	public int remainingSpace() {
		return availableOnPrimaryAxis - sizeSoFar;
	}
	
	/**
	 * Gets the amount of space that would be available to an *additional* widget along this line. Factors in any
	 * padding that would be added by adding one more widget. This method is suitable for using with word wrap.
	 * @return The amount of space available to an additional widget if added.
	 */
	public int remainingSpacePlusPadding() {
		int totalSpace = sizeSoFar;
		if (!tray.isEmpty()) totalSpace += additionalPadding;
		return availableOnPrimaryAxis - totalSpace;
	}
	
	public Position startPosition() {
		return startPosition;
	}
	
	/**
	 * Returns true if the provided widget can be inserted into this line without extending outside the layout area.
	 * @param widget the widget to measure, and possibly add.
	 * @return true if the widget will fit, otherwise false.
	 */
	public boolean canFit(Widget widget) {
		int remaining = remainingSpacePlusPadding();
		return axis.getSize(widget) <= remaining;
	}
	
	/**
	 * Adds this widget to the line layout. When {@link #layout()} is called, the provided widget's position will change.
	 * @param widget The widget that is to occur next in the line layout.
	 */
	public void add(Widget widget) {
		sizeSoFar += axis.getSize(widget);
		if (!tray.isEmpty()) sizeSoFar += additionalPadding;
		
		tray.add(widget);
	}
	
	/**
	 * Computes the line geometry, and lays added widgets out along the line according to the configured rules.
	 * @return A rectangle indicating the area the components were actually laid out in - a minimum bounding box for
	 *         the line.
	 */
	public ScreenRect layout() {
		if (tray.isEmpty()) return new ScreenRect(startPosition.asScreenPos(), 0, 0);
		
		// Figure out how much space we have to effect the layout
		int primaryAxisSize = 0;
		int crossAxisSize = 0;
		ScreenAxis crossAxis = axis.opposite();
		
		for(Widget w : tray) {
			// Elements are layed out non-overlapping along the primary axis, so the layout "width" is their sum
			primaryAxisSize += axis.getSize(w);
			// Elements are "overlapping" along the cross axis, so their layout "height" is the height of the tallest one.
			crossAxisSize = Math.max(crossAxisSize, crossAxis.getSize(w));
		}
		
		// Add on any mandatory padding between elements.
		primaryAxisSize += (tray.size() - 1) * additionalPadding;
		
		// Add on even *more* padding to justify elements along the line
		int extraAdvance = 0;
		if (justify & primaryAxisSize < availableOnPrimaryAxis) {
			extraAdvance = availableOnPrimaryAxis - primaryAxisSize;
			if (extraAdvance < 0) extraAdvance = 0;
		}
		
		// Finally, lay out the elements along the line
		int primaryAxisAdvance = startPosition.get(axis);
		int crossAxisStart = startPosition.get(crossAxis);
		int lastComponentAdvance = primaryAxisAdvance;
		
		for(Widget w : tray) {
			int crossAxisRemainingSpace = crossAxisSize - crossAxis.getSize(w);
			int crossAxisOffset = switch(crossAxisAlign) {
				case LEADING -> 0;
				case TRAILING -> crossAxisRemainingSpace;
				case CENTER -> crossAxisRemainingSpace / 2;
			};
			Position widgetPosition = axis.orientCoordinates(primaryAxisAdvance, crossAxisStart + crossAxisOffset);
			w.setPosition(widgetPosition.x(), widgetPosition.y());
			
			primaryAxisAdvance += axis.getSize(w);
			lastComponentAdvance = primaryAxisAdvance;
			primaryAxisAdvance += additionalPadding + extraAdvance; // Note that this may wind up outside our layout area
		}
		
		// Report the area we actually occupied in this layout - most crucially the height of this rectangle (if we're laying out HORIZONTAL)
		Size size = axis.orientSizes(lastComponentAdvance, crossAxisSize);
		return new ScreenRect(startPosition.asScreenPos(), size.width(), size.height());
	}
}
