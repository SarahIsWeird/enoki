package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.markdown.NodeStyle;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public abstract class AbstractMarkdownWidget extends ClickableWidget {
	protected NodeStyle style;
	
	public AbstractMarkdownWidget(int x, int y, int width, int height, Text message, NodeStyle style) {
		super(x, y, width, height, message);
		this.style = style;
	}
	
	protected abstract boolean hasClickBehavior();

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (style.clickHandler() != null || hasClickBehavior()) {
			return super.mouseClicked(mouseX, mouseY, button);
		}
		
		return false;
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		if (style.clickHandler() != null) {
			style.clickHandler().handle(mouseX, mouseY);
		}
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		
	}
}
