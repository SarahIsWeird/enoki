package blue.endless.enoki.gui.widgets.quote;

import java.util.Iterator;
import java.util.List;

import blue.endless.enoki.gui.MarkdownWidget;
import blue.endless.enoki.gui.Size;
import blue.endless.enoki.gui.widgets.AbstractMarkdownWidget;
import blue.endless.enoki.markdown.styles.LayoutStyle;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Colors;

public class BlockQuoteTitleWidget extends AbstractMarkdownWidget {
	private final BlockQuoteInfo info;
	private final TextRenderer font;
	private final int spaceWidth;
	private final Size iconSize;
	
	public BlockQuoteTitleWidget(int x, int y, BlockQuoteInfo info, TextRenderer font, LayoutStyle style) {
		super(x, y, getTitleWidth(info, font), font.fontHeight, info.title(), style);
		this.info = info;
		this.font = font;
		this.spaceWidth = font.getWidth(" ");
		
		this.iconSize = MarkdownWidget.getActualImageSize(info.iconId());
		
		// Right now I'm adding clearance between the title/icon usind PADDING and not margins
		this.height = Math.max(iconSize.height(), font.fontHeight) + 4;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		int x = 0;
		int y = (this.iconSize.height() - font.fontHeight) / 2 + 1;
		
		x += renderIcon(context);
		
		context.drawText(this.font, this.info.title(), x, y, Colors.WHITE, false);
	}
	
	private int renderIcon(DrawContext context) {
		if (this.info.iconId() == null) return 0;
		
		context.drawTexture(
			RenderLayer::getGuiTextured,
			this.info.iconId(),
			0, 0,
			0f, 0f,
			iconSize.width(), iconSize.height(),
			iconSize.width(), iconSize.height(),
			iconSize.width(), iconSize.height(),
			this.info.color()
		);
		
		return iconSize.width() + spaceWidth;
	}

	private static int getTitleWidth(BlockQuoteInfo info, TextRenderer font) {
		return font.getWidth(info.title()) + font.getWidth(" ") + font.fontHeight;
	}

	@Override
	public Iterator<ClickableWidget> iterator() {
		return List.<ClickableWidget>of().iterator();
	}
}
