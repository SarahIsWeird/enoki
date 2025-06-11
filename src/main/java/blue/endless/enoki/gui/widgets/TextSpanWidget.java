package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.markdown.NodeStyle;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.random.Random;

public class TextSpanWidget extends ClickableWidget {
	Random random = Random.create();
	private final NodeStyle style;
	private final TextRenderer font;
	
	private int backgroundColor = -1;
	
	public TextSpanWidget(int x, int y, Text message, NodeStyle style, TextRenderer font) {
		super(x, y, font.getWidth(message), font.fontHeight, message);
		this.style = style;
		this.font = font;
		
		int r = random.nextBetween(128, 255);
		int g = random.nextBetween(128, 255);
		int b = random.nextBetween(128, 255);
		backgroundColor = 0xFF_000000 |
				(r << 16) |
				(g <<  8) |
				(b      );
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		
		
		if (backgroundColor != 0) context.fill(getX(), getY(), getX() + width, getY() + height, backgroundColor);
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
