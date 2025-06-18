package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.gui.MarkdownWidget;
import blue.endless.enoki.gui.Size;
import blue.endless.enoki.markdown.styles.LayoutStyle;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;

public class ImageWidget extends AbstractContainerWidget implements Resizeable {
	private final TextRenderer font;
	private final Identifier image;
	private final Size imageSize;
	private MutableText tooltip = null;

	public ImageWidget(int x, int y, int width, int height, Text altText, Identifier image, TextRenderer font, LayoutStyle style) {
		super(width, height, style);
		this.font = font;
		this.image = image;
		this.imageSize = MarkdownWidget.getActualImageSize(image);
		this.width = imageSize.width();
		this.height = imageSize.height();
	}
	
	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		//context.getMatrices().push();
		context.fill(0, 0, width, height, Colors.CYAN);
		context.drawTexture(
			RenderLayer::getGuiTextured,
			this.image,
			0, 0,
			0f, 0f,
			this.width, this.height,
			imageSize.width(), imageSize.height(),
			imageSize.width(), imageSize.height()
		);
		
		if (isMouseOver(mouseX, mouseY) && tooltip != null) {
			context.drawTooltip(font, tooltip, mouseX, mouseY);
		}

		//context.getMatrices().pop();
	}
	
	public void add(ClickableWidget w) {
		if (w instanceof AbstractMarkdownWidget amw) {
			if (tooltip == null) tooltip = Text.empty();

			tooltip.append(amw.getAsText());
		}
	}

	@Override
	@NotNull
	public Iterator<ClickableWidget> iterator() {
		return Collections.emptyIterator();
	}
}
