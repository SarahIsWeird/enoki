package blue.endless.enoki.gui;

import blue.endless.enoki.gui.widgets.TextSpanWidget;
import blue.endless.enoki.markdown.DocNode;
import blue.endless.enoki.markdown.LayoutStyle;
import blue.endless.enoki.markdown.NodeStyle;
import blue.endless.enoki.markdown.NodeType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class MarkdownWidget extends ClickableWidget {
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
	
	public MarkdownWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Text.empty());
		
		this.wordWrap = new WordWrap();
		this.font = MinecraftClient.getInstance().textRenderer;
	}
	
	public void setFont(TextRenderer font) {
		this.font = Objects.requireNonNull(font);
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
		dc.fill(getX(), getY(), getX() + width, getY() + height, Colors.GRAY);
		
		for (ClickableWidget child : children) {
			child.render(dc, mouseX, mouseY, deltaTicks);
		}
	}
	
	private void rebuildWidgets() {
		Deque<BlockContext> contexts = new ArrayDeque<>();
		contexts.push(new BlockContext(getX() + 8, getY() + 8, getWidth() - 24)); // TODO: This should be controlled by insets, and should probably default to 8 on all sides.

		this.children.clear();
		buildWidgets(document, Position.of(getX(), getY()), contexts, NodeStyle.NORMAL);
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
		LayoutStyle layout = layoutMap.getOrDefault(node.type(), LayoutStyle.TEXT);
		
		NodeStyle newStyle = layout.style().combined(externalStyle);
		
		Margins margins = layout.margins();
		
		BlockContext context = contexts.peek();
		if (context == null) return position;
		
		String nodeText = node.text();
		
		// FIXME: Should be configurable
		if (node.type() == NodeType.LIST_ITEM) {
			nodeText = "\u2022 " + nodeText;
		}

		Position nextPosition = buildInlineText(position, newStyle, nodeText, context);
		
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
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}
}
