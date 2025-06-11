package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.markdown.NodeStyle;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

public class TextSpanWidget extends ClickableWidget {
	private final NodeStyle style;
	private final TextRenderer font;
	
	public TextSpanWidget(int x, int y, Text message, NodeStyle style, TextRenderer font) {
		super(x, y, font.getWidth(message), font.fontHeight, message);
		this.style = style;
		this.font = font;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.drawText(this.font, this.getMessage(), getX(), getY(), Colors.WHITE, this.style.shadow());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}
}
