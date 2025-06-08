package blue.endless.enoki.gui;

import blue.endless.enoki.MarkdownResourceReloadListener;
import blue.endless.enoki.gui.widgets.ImageWidget;
import blue.endless.enoki.gui.widgets.TextSpanWidget;
import blue.endless.enoki.markdown.DocNode;
import blue.endless.enoki.markdown.LayoutStyle;
import blue.endless.enoki.markdown.NodeStyle;
import blue.endless.enoki.markdown.NodeType;
import blue.endless.enoki.markdown.attributes.DocImageAttributes;
import blue.endless.enoki.text.WordWrap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Environment(EnvType.CLIENT)
public class MarkdownWidget extends ContainerWidget {
	private static final Logger LOGGER = LogManager.getLogger(MarkdownWidget.class);
	
	private DocNode document;
	private final WordWrap wordWrap;
	private TextRenderer font;
	private Map<NodeType, LayoutStyle> layoutMap = Map.of(
			NodeType.DOCUMENT, LayoutStyle.DOCUMENT,
			NodeType.H1, LayoutStyle.H1,
			NodeType.EMPHASIS, LayoutStyle.ITALIC,
			NodeType.STRONG_EMPHASIS, LayoutStyle.BOLD,
			NodeType.STRIKETHROUGH, LayoutStyle.STRIKETHROUGH,
			NodeType.UNDERLINE, LayoutStyle.UNDERLINE,
			NodeType.PARAGRAPH, LayoutStyle.PARAGRAPH
			);
	
	private final List<ClickableWidget> children = new ArrayList<>();
	private final boolean scrollable;
	private int contentHeight;
	
	public MarkdownWidget(int x, int y, int width, int height, boolean scrollable) {
		super(x, y, width, height, Text.empty());
		
		this.wordWrap = new WordWrap();
		this.font = MinecraftClient.getInstance().textRenderer;
		this.scrollable = scrollable;
	}
	
	public void setFont(TextRenderer font) {
		this.font = requireNonNull(font);
		this.rebuildWidgets();
	}
	
	public void setDocument(DocNode document) {
		this.document = document;
		this.rebuildWidgets();
	}
	
	public void setLayoutMap(Map<NodeType, LayoutStyle> layoutMap) {
		this.layoutMap = layoutMap;
		this.rebuildWidgets();
	}
	
	@Override
	public void renderWidget(DrawContext dc, int mouseX, int mouseY, float deltaTicks) {
		dc.getMatrices().push();
		dc.fill(getX(), getY(), getX() + width, getY() + height, Colors.GRAY);
		
		if (this.scrollable) {
			dc.enableScissor(getX(), getY(), getX() + width, getY() + height);
			dc.getMatrices().translate(0, -getScrollY(), 0);
		}
		
		for (ClickableWidget child : children) {
			child.render(dc, mouseX, mouseY, deltaTicks);
		}
		
		if (this.scrollable) {
			dc.disableScissor();
		}
		
		dc.getMatrices().pop();
		
		if (this.scrollable) {
			this.drawScrollbar(dc);
		}
	}
	
	private void rebuildWidgets() {
		Deque<BlockContext> contexts = new ArrayDeque<>();
		contexts.push(new BlockContext(getX() + 8, getY() + 8, getWidth() - 24)); // TODO: This should be controlled by insets, and should probably default to 8 on all sides.

		this.children.clear();
		Position finalPosition = buildWidgets(document, Position.of(getX(), getY()), contexts, NodeStyle.NORMAL);
		
		this.contentHeight = finalPosition.y() - this.getY();
	}
	
	private Position buildWidgets(DocNode node, Position position, Deque<BlockContext> contexts, NodeStyle style) {
		if (node.type().isBlock()) {
			return buildBlockWidgets(node, position, contexts, style);
		}
		
		return buildInlineWidgets(node, position, contexts, style);
	}
	
	private Position buildBlockWidgets(DocNode node, Position nextPosition, Deque<BlockContext> contexts, NodeStyle externalStyle) {
		LayoutStyle layout = layoutMap.getOrDefault(node.type(), LayoutStyle.TEXT);
		
		NodeStyle newStyle = layout.style().combined(externalStyle);
		Margins margins = layout.margins();
		
		BlockContext context = contexts.peek();
		if (context == null) return nextPosition;
		
		int blockX = context.x() + layout.indent();
		// TODO: Maybe bump top margin down to after we've figured out the line break behavior
		int blockY = nextPosition.y() + margins.top();
		int width = context.width() - layout.indent();
		
		if (nextPosition.x() != context.x()) {
			blockY += newStyle.applyScale(font.fontHeight);
		}
		
		BlockContext innerContext = new BlockContext(blockX, blockY, width);
		contexts.push(innerContext);
		nextPosition = Position.of(blockX, blockY);
		
		for (DocNode child : node.children()) {
			nextPosition = buildWidgets(child, nextPosition, contexts, newStyle);
		}
		
		contexts.pop();
		
		int nextY = nextPosition.y() + margins.bottom();
		if (nextPosition.x() != context.x()) {
			nextY += newStyle.applyScale(font.fontHeight);
		}
		
		return Position.of(context.x(), nextY);
	}
	
	private Position buildInlineWidgets(DocNode node, Position position, Deque<BlockContext> contexts, NodeStyle externalStyle) {
		BlockContext context = contexts.peek();
		if (context == null) return position;

		LayoutStyle layout = layoutMap.getOrDefault(node.type(), LayoutStyle.TEXT);
		NodeStyle newStyle = layout.style().combined(externalStyle);
		String nodeText = node.text();
		
		// FIXME: Should be configurable
		if (node.type() == NodeType.LIST_ITEM) {
			nodeText = "\u2022 " + nodeText;
		}

		Position nextPosition;
		if (node.type() == NodeType.IMAGE) {
			return buildImage(position, context, newStyle, node);
		} else {
			nextPosition = buildInlineText(position, newStyle, nodeText, context);
		}
		
		BlockContext innerContext = context;
		if (node.type() == NodeType.LIST_ITEM) {
			innerContext = new BlockContext(nextPosition.x(), nextPosition.y(), context.width() - nextPosition.x());
		}
		
		contexts.push(innerContext);
		
		for (DocNode child : node.children()) {
			nextPosition = buildWidgets(child, nextPosition, contexts, newStyle);
		}
		
		contexts.pop();

		boolean forceLineBreak = node.type() == NodeType.LIST_ITEM;
		if (forceLineBreak) {
			nextPosition = Position.of(context.x(), nextPosition.y());
		}
		
		return nextPosition;
	}
	
	private Position buildImage(Position position, BlockContext context, NodeStyle style, DocNode node) {
		if (position.x() != context.x()) {
			position = Position.of(context.x(), position.y() + style.applyScale(font.fontHeight));
		}
		
		if (!(node.attributes() instanceof DocImageAttributes attributes)) {
			throw new IllegalStateException("Expected image attributes to be instanceof DocImageAttributes");
		}

		Identifier imageId = attributes.imageId();
		if (imageId == null) return position;
		
		Size childSize = getImageSize(attributes, context, style, imageId);

		Text altText = node.asText(NodeStyle.NORMAL,
			(type) -> layoutMap.getOrDefault(node.type(), LayoutStyle.TEXT).style());

		ClickableWidget child = new ImageWidget(position.x(), position.y(), childSize.width(), childSize.height(), altText, imageId, font);
		this.children.add(child);
		
		return Position.of(context.x(), position.y() + childSize.height());
	}
	
	private Size getImageSize(DocImageAttributes attributes, BlockContext context, NodeStyle style, Identifier imageId) {
		int width = attributes.size().width();
		int height = attributes.size().height();
		
		if (width != -1 && height != -1) {
			return attributes.size();
		}

		// FIXME: Aspect ratio stuff
		return MarkdownResourceReloadListener.getImageSize(imageId).scale(style.size());
	}
	
	private Position buildInlineText(Position position, NodeStyle style, String nodeText, BlockContext context) {
		Text lastLine = Text.empty();
		Position nextPosition = position;
		String remainingText = nodeText;
		
		while (!remainingText.isEmpty()) {
			int incomingIndent = nextPosition.x() - context.x();
			int availableLineWidth = context.width() - incomingIndent;
			
			String nextLine = wordWrap.getFirstLine(font, availableLineWidth, remainingText, style);
			lastLine = Text.literal(nextLine).setStyle(style.asStyle());
			
			ClickableWidget child = new TextSpanWidget(nextPosition.x(), nextPosition.y(), lastLine, style, this.font);
			this.children.add(child);

			// TODO: Check if just stripping spaces (and tabs?) is enough
			remainingText = remainingText.substring(nextLine.length()).stripLeading();
			if (!remainingText.isEmpty()) {
				// The last line might have some space left over, which could still be used by children.
				nextPosition = Position.of(context.x(), nextPosition.y() + style.applyScale(font.fontHeight));
			}
		}
		
		return nextPosition.withOffset(style.getTextWidth(lastLine, font), 0);
	}
	
	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		
	}

	@Override
	protected int getContentsHeightWithPadding() {
		return contentHeight;
	}

	@Override
	protected double getDeltaYPerScroll() {
		return 10.0;
	}

	@Override
	public List<? extends Element> children() {
		return this.children;
	}
}
