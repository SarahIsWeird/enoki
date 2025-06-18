package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.gui.widgets.Splittable.Result;
import blue.endless.enoki.gui.widgets.quote.BlockQuoteWidget;
import blue.endless.enoki.markdown.styles.LayoutStyle;
import blue.endless.enoki.markdown.styles.properties.StyleProperties;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockContainerWidget extends AbstractContainerWidget implements Resizeable {
	protected List<ClickableWidget> children = new ArrayList<>();
	
	public BlockContainerWidget(int width, LayoutStyle style) {
		super(width, 1, style);
	}
	
	@Override
	public void add(ClickableWidget child) {
		// Note: For unknown widgets, default to treating them as non-splittable inline-blocks
		if (child instanceof AbstractMarkdownWidget w && !w.isInline()) {
			addBlock(child);
		} else {
			addInline(child);
		}
	}
	
	private void addBlock(ClickableWidget block) {
		LayoutStyle blockStyle = (block instanceof AbstractMarkdownWidget mkdn) ? mkdn.getStyle() : LayoutStyle.empty();
		int blockLeft = blockStyle.getOrDefault(StyleProperties.MARGIN_LEFT, 0);
		if (block instanceof BlockQuoteWidget) blockLeft += 8;
		//int blockRight = blockStyle.getOrDefault(StyleProperties.MARGIN_RIGHT, 0);
		//int blockWidth = this.width - blockLeft - blockRight;
		
		if (children.isEmpty()) {
			block.setPosition(blockLeft, 0);
		} else {
			block.setPosition(blockLeft, this.height);
		}
		
		children.add(block);
		
		if (block instanceof Resizeable r) r.setSize(this.width, -1);
		int bottomMargin = blockStyle.getOrDefault(StyleProperties.MARGIN_BOTTOM, 0);
		this.height = block.getBottom() + bottomMargin; // TODO: Add margin
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
					if (r.leftover() != null) lastLine = newLine(); // NOT GUARANTEED! THIS IS A HEURISTIC!
				} else {
					lastLine = newLine();
				}
				
				//lastLine = newLine();
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
				} else if (remainder instanceof Splittable sp) {
					Result r = sp.split(this.width, false);
					
					if (!r.success()) {
						r = sp.split(width, true);
					}
					
					lastLine.add(r.result());
					lastLine = newLine();
						
					remainder = r.leftover();
					
				} else {
					lastLine.add(remainder);
					remainder = null;
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
