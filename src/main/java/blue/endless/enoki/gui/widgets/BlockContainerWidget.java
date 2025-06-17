package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.gui.widgets.Splittable.Result;
import blue.endless.enoki.markdown.styles.LayoutStyle;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockContainerWidget extends AbstractMarkdownWidget implements Resizeable {
	protected List<ClickableWidget> children = new ArrayList<>();
	
	public BlockContainerWidget(int width, LayoutStyle style) {
		super(0, 0, width, 1, Text.empty(), style);
	}
	
	public void add(ClickableWidget child) {
		// Note: For unknown widgets, default to treating them as non-splittable inline-blocks
		if (child instanceof AbstractMarkdownWidget w && !w.isInline()) {
			addBlock(child);
		} else {
			addInline(child);
		}
	}
	
	private void addBlock(ClickableWidget block) {
		if (children.isEmpty()) {
			block.setPosition(0, 0);
		} else {
			block.setPosition(0, children.getLast().getBottom());
		}
		
		children.add(block);
		
		if (block instanceof Resizeable r) r.setSize(this.width, -1);
		this.height = block.getBottom(); // TODO: Add margin
	}
	
	private void addUnsplittableInline(ClickableWidget widget) {
		LineWidget lastLine = getOrCreateLine();
		int available = lastLine.getAvailableSpace();
		if (widget.getWidth() > available && !lastLine.isEmpty()) {
			lastLine = newLine();
		}
		lastLine.add(widget);
		this.height = lastLine.getBottom();
	}
	
	private void addInline(ClickableWidget widget) {
		if (widget instanceof Splittable splittable) {
			/*
			 * We will operate in two phases.
			 * 
			 * First, if we already have something on lastLine, we need to be very careful to only split to the
			 * *remaining* space, and to never force the line break. We can fall down to the next line for that.
			 */
			ClickableWidget remainder = widget;
			LineWidget lastLine = getOrCreateLine();
			
			if (!lastLine.isEmpty()) {
				Result r = splittable.split(lastLine.getAvailableSpace(), false);
				if (r.success()) {
					lastLine.add(r.result());
					remainder = r.leftover();
				}
				
				lastLine = newLine();
			}
			
			/*
			 * Second phase is as many full-width wraps as we need. If at any point we can't fit anything on a line,
			 * we need to hard-wrap it.
			 */
			while(remainder != null) {
				// Try to fit against the full width of the line
				if (remainder.getWidth() < this.width) {
					lastLine.add(remainder);
					remainder = null;
					break;
				}
				
				if (remainder instanceof Splittable sp) {
					Result r = sp.split(this.width, false);
					
					if (!r.success()) {
						r = sp.split(width, true);
					}
					
					lastLine.add(r.result());
					lastLine = newLine();
						
					remainder = r.leftover();
					break;
					
				} else {
					lastLine.add(remainder);
					remainder = null;
					break;
				}
			}
			
			this.height = lastLine.getBottom();
		} else {
			addUnsplittableInline(widget);
		}
	}
	
	protected LineWidget getOrCreateLine() {
		if (!children.isEmpty() && children.getLast() instanceof LineWidget line) {
			return line;
		} else {
			return newLine();
		}
	}
	
	protected LineWidget newLine() {
		LineWidget line = new LineWidget(this.width, style);
		if (!this.children.isEmpty()) {
			line.setPosition(0, children.getLast().getBottom());
		}
		children.add(line);
		return line;
	}

	@Override
	public Iterator<ClickableWidget> iterator() {
		return children.iterator();
	}
	
	@Override
	public boolean isInline() {
		return false;
	}
	
	@Override
	public void setSize(int width, int height) {
		// if we're not empty and width is changing, redo layout
		if (width == this.width || width == -1 || this.children.isEmpty()) {
			if (children.isEmpty()) Resizeable.super.setSize(width, height);
			return;
		}
		
		// THIS ISN'T GOING TO WORK GREAT - it might smash previously-split text spans together without a space!
		// But it will reflow the text as well as we can.
		List<ClickableWidget> oldList = children;
		children = new ArrayList<ClickableWidget>();
		Resizeable.super.setSize(width, height);
		for(ClickableWidget w : oldList) {
			if (w instanceof LineWidget line) {
				for(ClickableWidget z : line) {
					this.add(z);
				}
			} else {
				this.add(w);
			}
		}
	}
	
}
