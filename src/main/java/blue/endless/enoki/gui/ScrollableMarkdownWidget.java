package blue.endless.enoki.gui;

import blue.endless.enoki.markdown.DocNode;
import blue.endless.enoki.markdown.NodeType;
import blue.endless.enoki.markdown.styles.LayoutStyle;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ScrollableMarkdownWidget extends ContainerWidget {
	private final MarkdownWidget markdown;
	private final SimplePositioningWidget layout;
	
	public ScrollableMarkdownWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Text.empty());
		this.markdown = new MarkdownWidget(x, y, width);
		this.layout = new SimplePositioningWidget(x, y, width, height);
		this.layout.add(this.markdown);
	}

	public void setFont(TextRenderer font) {
		this.markdown.setFont(font);
	}

	public void setDocument(@NotNull DocNode document) {
		this.markdown.setDocument(document);
	}

	public void setLayoutMap(Map<@NotNull NodeType, LayoutStyle> layoutMap) {
		this.markdown.setLayoutMap(layoutMap);
	}
	
	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		this.markdown.setWidth(width);
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		this.markdown.setHeight(height);
	}

	public void setSize(int width, int height) {
		super.setWidth(width);
		super.setHeight(height);
		
		this.markdown.setWidth(width);
	}

	@Override
	public List<? extends Element> children() {
		return List.of(this.markdown);
	}

	@Override
	protected int getContentsHeightWithPadding() {
		return this.markdown.getHeight();
	}

	@Override
	protected double getDeltaYPerScroll() {
		return 10.0;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.getMatrices().push();
		context.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
		
		this.markdown.renderWidget(context, mouseX, mouseY, deltaTicks);
		
		context.disableScissor();
		context.getMatrices().pop();
		
		this.drawScrollbar(context);
	}

	@Override
	public void setScrollY(double scrollY) {
		super.setScrollY(scrollY);
		this.layout.setY(this.getY() - (int) scrollY);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		this.markdown.appendClickableNarrations(builder);
	}
}
