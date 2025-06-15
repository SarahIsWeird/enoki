package blue.endless.enoki.gui.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import blue.endless.enoki.markdown.LayoutStyle;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class LineWidget extends AbstractMarkdownWidget {
	protected List<ClickableWidget> children = new ArrayList<>();
	
	public LineWidget(int width, LayoutStyle style) {
		super(0, 0, width, 0, Text.empty(), style);
	}

	@Override
	public Iterator<ClickableWidget> iterator() {
		return children.iterator();
	}
	
	/**
	 * Gets the available space, minus any padding that would be triggered by adding a new element.
	 * @return The amount of space available to an incoming element.
	 */
	public int getAvailableSpace() {
		if (children.isEmpty()) return this.getWidth();
		
		ClickableWidget last = children.getLast();
		int usedSpace = last.getX() + last.getWidth();
		return this.getWidth() - usedSpace;
	}
	
	public void add(ClickableWidget child) {
		if (children.isEmpty()) {
			child.setPosition(0, 0);
		} else {
			child.setPosition(children.getLast().getRight(), 0);
		}
		children.add(child);
		this.height = Math.max(this.height, child.getHeight());
	}
	
	public boolean isEmpty() {
		return children.isEmpty();
	}
}
