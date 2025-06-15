package blue.endless.enoki.gui.widgets.quote;

import blue.endless.enoki.gui.widgets.AbstractMarkdownWidget;
import blue.endless.enoki.markdown.LayoutStyle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.Iterator;
import java.util.List;

import org.spongepowered.include.com.google.common.collect.Iterators;

public class BlockQuoteWidget extends AbstractMarkdownWidget {
	private static final int LINE_WIDTH = 2;
	
	private final List<ClickableWidget> children;
	
	private final int lineX;
	private final int color;
	
	public BlockQuoteWidget(int x, int y, int width, int height, int color, LayoutStyle style, List<ClickableWidget> children) {
		super(x, y, width, height, Text.empty(), style);
		this.children = children;
		this.lineX = x - LayoutStyle.BLOCK_QUOTE.indent();
		
		this.color = color;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.fill(this.lineX, getY(), this.lineX + LINE_WIDTH, getY() + getHeight(), this.color);
		
		super.renderWidget(context, mouseX, mouseY, deltaTicks);
	}

	@Override
	public void setY(int y) {
		for (ClickableWidget child : this.children) {
			child.setY(child.getY() + (y - this.getY()));
		}
		
		super.setY(y);
	}

	@Override
	public Iterator<ClickableWidget> iterator() {
		return Iterators.unmodifiableIterator(children.iterator());
	}
}
