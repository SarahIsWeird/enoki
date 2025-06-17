package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.markdown.styles.LayoutStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

public abstract class AbstractMarkdownWidget extends ClickableWidget implements Iterable<ClickableWidget> {
	protected LayoutStyle style;
	protected int backgroundColor = 0;
	protected ClickHandler onClick = (x, y, button) -> false;
	// TODO: tooltip handler
	
	public AbstractMarkdownWidget(int x, int y, int width, int height, Text message, LayoutStyle style) {
		super(x, y, width, height, message);
		this.style = style;
		this.backgroundColor = randomColor();
	}
	
	private int randomColor() {
		Random rng = Random.create();
		int r = rng.nextBetween(64, 180);
		int g = rng.nextBetween(64, 180);
		int b = rng.nextBetween(64, 180);
		
		return 0x33_000000 |
				(r << 16) |
				(g <<  8) |
				(b      );
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.active && this.visible) {
			for(ClickableWidget w : this) {
				if (w.mouseClicked(mouseX - w.getX(), mouseY - w.getY(), button)) return true;
			}
			
			if (this.isValidClickButton(button)) {
				boolean hovered = this.isMouseOver(mouseX, mouseY);
				if (hovered) {
					boolean result = this.onClick.apply(mouseX, mouseY, button);
					if (result) {
						this.playDownSound(MinecraftClient.getInstance().getSoundManager());
					}
					return result;
				}
			}

			return false;
		} else {
			return false;
		}
	}
	
	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		if ((backgroundColor & 0xFF_000000) != 0) {
			context.fill(0, 0, this.getWidth(), this.getHeight(), backgroundColor);
		}
		
		for(ClickableWidget widget : this) {
			
			context.getMatrices().push();
			context.getMatrices().translate(widget.getX(), widget.getY(), 0);
			
			//context.enableScissor(0, 0, width, height);
			
			widget.render(context, mouseX - widget.getX(), mouseY - widget.getY(), deltaTicks);
			
			//context.disableScissor();
			
			context.getMatrices().pop();
		}
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
