package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.markdown.styles.LayoutStyle;
import com.google.common.collect.Iterators;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlowContainerWidget extends AbstractMarkdownWidget implements Splittable {
	protected List<ClickableWidget> children = new ArrayList<>();
	
	public FlowContainerWidget(LayoutStyle style) {
		super(0, 0, 0, 0, Text.empty(), style);
	}

	public void add(ClickableWidget child) {
		if (children.isEmpty()) {
			child.setPosition(0, 0);
		} else {
			child.setPosition(children.getLast().getRight(), 0);
		}
		children.add(child);
		this.width = child.getRight();
		this.height = Math.max(this.height, child.getHeight());
	}
	
	@Override
	@NotNull
	public Iterator<ClickableWidget> iterator() {
		return Iterators.unmodifiableIterator(children.iterator());
	}

	@Override
	public Result split(int lineWidth, boolean force) {
		// Assume if we're empty that we'll fit.
		if (children.isEmpty()) return Result.everythingFits(this);
		
		int firstOnNextLine = getFirstNonFittingWidget(lineWidth);
		System.out.println("Splitting FlowContainer. First non-fitting widget: "+firstOnNextLine);
		
		// If everything fits, we have an easy solution.
		if (firstOnNextLine == -1) return Result.everythingFits(this);
		
		// If we're force-splitting one unsplittable child, let's just put ourselves on the line.
		if (force && children.size() < 2 && !(children.getFirst() instanceof Splittable)) return Result.everythingFits(this);
		
		List<ClickableWidget> fittingChildren = new ArrayList<>();
		List<ClickableWidget> splitChildren = new ArrayList<>();
		if (firstOnNextLine > 0) fittingChildren.addAll(children.subList(0, firstOnNextLine));
		
		int copyIndex = firstOnNextLine;
		// Is there a child at index firstOnNextLine, and can we split it?
		if (children.size() > firstOnNextLine && children.get(firstOnNextLine) instanceof Splittable s) {
			int splitPosition = 0;
			if (!fittingChildren.isEmpty()) splitPosition = fittingChildren.getLast().getRight();
			int childSplitWidth = this.width - splitPosition;
			Result r = s.split(childSplitWidth, force);
			if (r.success()) {
				fittingChildren.add(r.result());
				if (r.leftover() != null) splitChildren.add(r.leftover());
				copyIndex++;
			} // If not, we just continue.
		}
		
		if (fittingChildren.isEmpty()) {
			// First element didn't fit, and we couldn't split it
			if (force) {
				fittingChildren.add(children.getFirst());
				copyIndex++;
			} else {
				return Result.nothingFits(this);
			}
		}
		
		if (children.size() > copyIndex) splitChildren.addAll(children.subList(copyIndex, children.size()));
		
		if (splitChildren.isEmpty()) return Result.everythingFits(this); // Shouldn't happen
		
		return new Result(copyWithChildren(fittingChildren), copyWithChildren(splitChildren));
	}
	
	protected int getFirstNonFittingWidget(int targetWidth) {
		for(int i=0; i<children.size(); i++) {
			ClickableWidget w = children.get(i);
			if (w.getX() + w.getWidth() >= targetWidth) {
				return i;
			}
		}
		return -1;
	}
	
	public FlowContainerWidget copyWithChildren(List<ClickableWidget> children) {
		FlowContainerWidget flow = new FlowContainerWidget(style);
		for(ClickableWidget child : children) flow.add(child);
		//flow.children.addAll(children);
		return flow;
	}
	
}
