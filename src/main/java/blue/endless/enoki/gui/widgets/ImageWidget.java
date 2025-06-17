package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.gui.MarkdownWidget;
import blue.endless.enoki.gui.Size;
import blue.endless.enoki.markdown.styles.LayoutStyle;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;

public class ImageWidget extends AbstractMarkdownWidget implements Resizeable {
	private final TextRenderer font;
	private final Identifier image;
	private final Size imageSize;

	public ImageWidget(int x, int y, int width, int height, Text altText, Identifier image, TextRenderer font, LayoutStyle style) {
		super(x, y, width, height, altText, style);
		this.font = font;
		this.image = image;
		this.imageSize = MarkdownWidget.getActualImageSize(image);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.getMatrices().push();
		context.fill(getX(), getY(), getX() + width, getY() + height, Colors.CYAN);
		context.drawTexture(
			RenderLayer::getGuiTextured,
			this.image,
			this.getX(), this.getY(),
			0f, 0f,
			this.width, this.height,
			imageSize.width(), imageSize.height(),
			imageSize.width(), imageSize.height()
		);

		if (isMouseOver(mouseX, mouseY)) {
			context.drawTooltip(font, getMessage(), mouseX, mouseY);
		}

		context.getMatrices().pop();
	}

	@Override
	@NotNull
	public Iterator<ClickableWidget> iterator() {
		return Collections.emptyIterator();
	}
}
