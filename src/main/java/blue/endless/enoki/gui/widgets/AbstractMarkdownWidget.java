package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.markdown.styles.LayoutStyle;
import blue.endless.enoki.util.NotNullByDefault;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

@NotNullByDefault
public abstract class AbstractMarkdownWidget extends ClickableWidget {
	protected LayoutStyle style;
	protected int backgroundColor = 0;
	protected ClickHandler onClick = (x, y, button) -> false;
	
	public AbstractMarkdownWidget(int x, int y, int width, int height, Text message, LayoutStyle style) {
		super(x, y, width, height, message);
		this.style = style;
		this.backgroundColor = 0;
	}
	
	public Text getAsText() {
		Text result = this.getMessage();
		if (result == null) return Text.empty();
		return result;
	}
	
	/*
	private int randomColor() {
		Random rng = Random.create();
		int r = rng.nextBetween(64, 180);
		int g = rng.nextBetween(64, 180);
		int b = rng.nextBetween(64, 180);
		
		return 0x33_000000 |
				(r << 16) |
				(g <<  8) |
				(b      );
	}*/
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!active || !visible) return false;
		
		if (this.isValidClickButton(button)) {
			if (isMouseOver(mouseX, mouseY)) {
				boolean result = this.onClick.apply(mouseX, mouseY, button);
				if (result) {
					this.playDownSound(MinecraftClient.getInstance().getSoundManager());
				}
				return result;
			}
		}
		
		return false;
	}
	
	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		if ((backgroundColor & 0xFF_000000) != 0) {
			context.fill(0, 0, this.getWidth(), this.getHeight(), backgroundColor);
		}
	}
	
	public LayoutStyle getStyle() {
		return style;
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= 0 && mouseY >= 0 && mouseX < width && mouseY < height;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		
	}
	
	/**
	 * Some widgets can be conditionally treated as either block widgets or inline widgets. If isInline is true, the
	 * widget will be placed inline with text. If isInline is false, the widget will always be laid out with the full
	 * width of its container.
	 * @return true if this Widget should be placed inline with text and other flow items.
	 */
	public boolean isInline() {
		return true;
	}
}
