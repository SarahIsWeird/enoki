package blue.endless.enoki.gui.widgets.quote;

import blue.endless.enoki.gui.MarkdownWidget;
import blue.endless.enoki.gui.Size;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Colors;

public class BlockQuoteTitleWidget extends ClickableWidget {
	private final BlockQuoteInfo info;
	private final TextRenderer font;
	private final int spaceWidth;
	private final Size iconSize;
	
	public BlockQuoteTitleWidget(int x, int y, BlockQuoteInfo info, TextRenderer font) {
		super(x, y, getTitleWidth(info, font), font.fontHeight, info.title());
		this.info = info;
		this.font = font;
		this.spaceWidth = font.getWidth(" ");
		
		this.iconSize = MarkdownWidget.getActualImageSize(info.iconId());
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		int x = this.getX();
		int y = this.getY() + (this.iconSize.height() - font.fontHeight) / 2 + 1;
		
		x += renderIcon(context);
		
		context.drawText(this.font, this.info.title(), x, y, Colors.WHITE, false);
	}
	
	private int renderIcon(DrawContext context) {
		if (this.info.iconId() == null) return 0;
		
		context.drawTexture(
			RenderLayer::getGuiTextured,
			this.info.iconId(),
			this.getX(), this.getY(),
			0f, 0f,
			iconSize.width(), iconSize.height(),
			iconSize.width(), iconSize.height(),
			iconSize.width(), iconSize.height(),
			this.info.color()
		);
		
		return iconSize.width() + spaceWidth;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		
	}

	private static int getTitleWidth(BlockQuoteInfo info, TextRenderer font) {
		return font.getWidth(info.title()) + font.getWidth(" ") + font.fontHeight;
	}
}
