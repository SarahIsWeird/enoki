package blue.endless.enoki.gui;

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
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.ArrayDeque;
import java.util.Deque;
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
	
	public MarkdownWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Text.empty());
		
		this.wordWrap = new WordWrap();
		this.font = MinecraftClient.getInstance().textRenderer;
	}
	
	public void setFont(TextRenderer font) {
		this.font = Objects.requireNonNull(font);
	}
	
	public void setDocument(DocNode document) {
		this.document = document;
	}
	
	@Override
	public void renderWidget(DrawContext dc, int mouseX, int mouseY, float deltaTicks) {
		dc.fill(getX(), getY(), getX() + width, getY() + height, Colors.GRAY);
		
		Deque<BlockContext> contexts = new ArrayDeque<>();
		contexts.push(new BlockContext(getX() + 8, getY() + 8, getWidth() - 24)); // TODO: This should be controlled by insets, and should probably default to 8 on all sides.
		
		render(document, dc, Position.of(getX(), getY()), contexts, NodeStyle.NORMAL);
	}
	
	private Position render(DocNode node, DrawContext dc, Position position, Deque<BlockContext> contexts, NodeStyle style) {
		if (node.type().isBlock()) {
			return renderBlock(node, dc, position, contexts, style);
		}
		
		return renderInline(node, dc, position, contexts, style);
	}
	
	private Position renderBlock(DocNode node, DrawContext dc, Position nextPosition, Deque<BlockContext> contexts, NodeStyle externalStyle) {
		LayoutStyle layout = layoutMap.getOrDefault(node.type(), LayoutStyle.TEXT);
		
		NodeStyle newStyle = layout.style().withDefaults(externalStyle);
		Margins margins = layout.margins();
		
		BlockContext context = contexts.peek();
		if (context == null) return nextPosition;
		
		int blockX = context.x() + layout.indent();
		// TODO: Maybe bump top margin down to after we've figured out the line break behavior
		int blockY = nextPosition.y() + margins.top();
		int width = context.width() - layout.indent();
		
		if (nextPosition.x() != context.x()) {
			blockY += font.fontHeight;
		}
		
		BlockContext innerContext = new BlockContext(blockX, blockY, width);
		contexts.push(innerContext);
		nextPosition = Position.of(blockX, blockY);
		
		for (DocNode child : node.children()) {
			nextPosition = render(child, dc, nextPosition, contexts, newStyle);
		}
		
		contexts.pop();
		
		int nextY = nextPosition.y() + margins.bottom();
		if (nextPosition.x() != context.x()) {
			nextY += font.fontHeight;
		}
		
		return Position.of(context.x(), nextY);
	}
	
	private Position renderInline(DocNode node, DrawContext dc, Position position, Deque<BlockContext> contexts, NodeStyle externalStyle) {
		LayoutStyle layout = layoutMap.getOrDefault(node.type(), LayoutStyle.TEXT);
		
		NodeStyle newStyle = layout.style().withDefaults(externalStyle);
		Margins margins = layout.margins();
		
		BlockContext context = contexts.peek();
		if (context == null) return position;
		
		String nodeText = node.text();
		
		// FIXME: Should be configurable
		if (node.type() == NodeType.LIST_ITEM) {
			nodeText = "\u2022 " + nodeText;
		}

		Position nextPosition = renderInlineText(dc, position, newStyle, nodeText, context);
		
		boolean forceLineBreak = node.type() == NodeType.LIST_ITEM;
		if (forceLineBreak) {
			nextPosition = Position.of(context.x(), nextPosition.y() + font.fontHeight);
		}
		
		for (DocNode child : node.children()) {
			nextPosition = render(child, dc, nextPosition, contexts, newStyle);
		}
		
		return nextPosition;
	}
	
	private Position renderInlineText(DrawContext dc, Position position, NodeStyle style, String nodeText, BlockContext context) {
		OrderedText lastLine = OrderedText.EMPTY;
		Position nextPosition = position;
		String remainingText = nodeText;
		
		while (!remainingText.isEmpty()) {
			int incomingIndent = nextPosition.x() - context.x();
			int availableLineWidth = context.width() - incomingIndent;
			
			String nextLine = wordWrap.getFirstLine(font, availableLineWidth, remainingText, style.asStyle());
			lastLine = Text.literal(nextLine).setStyle(style.asStyle()).asOrderedText();

			dc.drawText(font, lastLine, nextPosition.x(), nextPosition.y(), Colors.WHITE, style.shadow());

			// TODO: Check if just stripping spaces (and tabs?) is enough
			remainingText = remainingText.substring(nextLine.length()).stripLeading();
			if (!remainingText.isEmpty()) {
				// The last line might have some space left over, which could still be used by children.
				nextPosition = Position.of(context.x(), nextPosition.y() + font.fontHeight);
			}
		}
		
		return nextPosition.withOffset(font.getWidth(lastLine), 0);
	}
	
	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}
}
