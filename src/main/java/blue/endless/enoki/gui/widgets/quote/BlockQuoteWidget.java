package blue.endless.enoki.gui.widgets.quote;

import blue.endless.enoki.markdown.LayoutStyle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.List;

public class BlockQuoteWidget extends ClickableWidget {
	private static final int LINE_WIDTH = 2;
	private static final int DEFAULT_LINE_COLOR = Colors.LIGHT_GRAY;
	
	private final List<ClickableWidget> children;
	
	private final int lineX;
	private final int color;
	
	public BlockQuoteWidget(int x, int y, int width, int height, int color, List<ClickableWidget> children) {
		super(x, y, width, height, Text.empty());
		this.children = children;
		this.lineX = x - LayoutStyle.BLOCK_QUOTE.indent();
		
		this.color = color;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.fill(this.lineX, getY(), this.lineX + LINE_WIDTH, getY() + getHeight(), this.color);
		
		for (ClickableWidget child : this.children) {
			child.render(context, mouseX, mouseY, deltaTicks);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
