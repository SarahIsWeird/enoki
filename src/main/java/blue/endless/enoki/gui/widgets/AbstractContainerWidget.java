package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.markdown.styles.LayoutStyle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;

public abstract class AbstractContainerWidget extends AbstractMarkdownWidget implements Iterable<ClickableWidget> {
	public AbstractContainerWidget(int width, int height, LayoutStyle style) {
		super(0, 0, width, height, null, style);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.active && this.visible) {
			for(ClickableWidget w : this) {
				if (w.mouseClicked(mouseX - w.getX(), mouseY - w.getY(), button)) return true;
			}
			
			return super.mouseClicked(mouseX, mouseY, button);
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
			
			// If we do scissor, we'll need to compute and include the margins
			//context.enableScissor(0, 0, width, height);
			
			widget.render(context, mouseX - widget.getX(), mouseY - widget.getY(), deltaTicks);
			
			//context.disableScissor();
			
			context.getMatrices().pop();
		}
	}
	
	public LayoutStyle getStyle() {
		return style;
	}
	
	public abstract void add(ClickableWidget w);
}
