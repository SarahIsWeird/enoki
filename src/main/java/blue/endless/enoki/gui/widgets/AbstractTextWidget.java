package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.markdown.NodeStyle;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

public abstract class AbstractTextWidget extends AbstractMarkdownWidget {
	protected final NodeStyle style;
	protected final TextRenderer font;
	
	protected AbstractTextWidget(int x, int y, Text message, NodeStyle style, TextRenderer font) {
		super(x, y, style.getTextWidth(message, font), style.applyScale(font.fontHeight), message, style);
		this.style = style;
		this.font = font;
	}

	@Override
	protected boolean hasClickBehavior() {
		return false;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.drawText(this.font, this.getMessage(), getX(), getY(), Colors.WHITE, this.style.shadow());
	}
}
