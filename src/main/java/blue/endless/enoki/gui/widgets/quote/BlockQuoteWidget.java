package blue.endless.enoki.gui.widgets.quote;

import blue.endless.enoki.gui.widgets.BlockContainerWidget;
import blue.endless.enoki.markdown.styles.LayoutStyle;
import blue.endless.enoki.markdown.styles.properties.StyleProperties;
import net.minecraft.client.gui.DrawContext;

public class BlockQuoteWidget extends BlockContainerWidget {
	private static final int LINE_WIDTH = 2;
	
	private final int lineX;
	private final int color;
	
	public BlockQuoteWidget(int width, int color, LayoutStyle style) {
		super(width, style);
		this.lineX = -style.getOrDefault(StyleProperties.MARGIN_LEFT, 0);
		this.color = color;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.fill(this.lineX, getY(), this.lineX + LINE_WIDTH, getY() + getHeight(), this.color);
		
		super.renderWidget(context, mouseX, mouseY, deltaTicks);
	}
}
