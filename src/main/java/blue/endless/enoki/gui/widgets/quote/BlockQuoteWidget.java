package blue.endless.enoki.gui.widgets.quote;

import blue.endless.enoki.EnokiClient;
import blue.endless.enoki.gui.widgets.BlockContainerWidget;
import blue.endless.enoki.markdown.styles.LayoutStyle;
import blue.endless.enoki.resource.StyleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class BlockQuoteWidget extends BlockContainerWidget {
	private static final int LINE_WIDTH = 2;
	
	private final int lineX;
	private final int color;
	
	public BlockQuoteWidget(int width, BlockQuoteInfo info, LayoutStyle style) {
		super(width, style);
		//this.lineX = -style.getOrDefault(StyleProperties.MARGIN_LEFT, 0);
		this.lineX = -LINE_WIDTH - 1;
		this.color = info.color();
		this.backgroundColor = 0;
		
		if (info.title() != null) {
			this.add(new BlockQuoteTitleWidget(0, 0, info, MinecraftClient.getInstance().textRenderer, style));
		}
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.fill(this.lineX, 0, this.lineX + LINE_WIDTH, getHeight(), this.color);
		
		super.renderWidget(context, mouseX, mouseY, deltaTicks);
	}
}
