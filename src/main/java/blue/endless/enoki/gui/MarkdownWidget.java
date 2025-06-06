package blue.endless.enoki.gui;

import blue.endless.enoki.markdown.DocNode;
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
import net.minecraft.util.Formatting;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.node.StrongEmphasis;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class MarkdownWidget extends ClickableWidget {
	private DocNode document;
	private final WordWrap wordWrap;
	private TextRenderer font;
	
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
		
		Deque<BlockContext> contexts = new LinkedList<>();
		contexts.push(new BlockContext(getX() + 8, getY() + 8, getWidth() - 24));
		
		render(document, dc, Position.of(getX(), getY()), contexts, NodeStyle.NORMAL);
	}
	
	private Position render(DocNode node, DrawContext dc, Position position, Deque<BlockContext> contexts, NodeStyle style) {
		if (node.type().isBlock()) {
			return renderBlock(node, dc, position, contexts, style);
		}
		
		return renderInline(node, dc, position, contexts, style);
	}
	
	private Position renderBlock(DocNode node, DrawContext dc, Position nextPosition, Deque<BlockContext> contexts, NodeStyle externalStyle) {
		BlockContext context = contexts.peek();
		if (context == null) return nextPosition;
		
		int indent = node.type().getIndent() * 8; // FIXME: Why 8?
				int blockX = context.x() + indent;
		int blockY = nextPosition.y();
		int width = context.width() - indent;
		
		if (nextPosition.x() != context.x()) {
			// FIXME: Configurable yAdvance
			blockY += 16;
		}
		
		BlockContext innerContext = new BlockContext(blockX, blockY, width);
		contexts.push(innerContext);
		nextPosition = Position.of(blockX, blockY);
		
		// FIXME: Customizable style provider that can set sizes
		NodeStyle newStyle = switch (node.type()) {
			case HEADING -> getHeadingStyle(externalStyle, node);
			default -> externalStyle;
		};
		
		for (DocNode child : node.children()) {
			nextPosition = render(child, dc, nextPosition, contexts, newStyle);
		}
		
		contexts.pop();
		
		// FIXME: When, if ever, would this not be `context`?
		BlockContext outerContext = Objects.requireNonNullElse(contexts.peek(), context);
		
		int nextY = nextPosition.y() + node.type().getBottomMargin();
		if (nextPosition.x() != context.x()) {
			nextY += font.fontHeight;
		}
		
		return Position.of(outerContext.x(), nextY);
	}
	
	private static NodeStyle getHeadingStyle(NodeStyle outerStyle, DocNode node) {
		return switch (node.value()) {
			case "1" -> outerStyle.withBold().withColor(Formatting.AQUA);
			case "2" -> outerStyle.withColor(Formatting.LIGHT_PURPLE);
			default -> outerStyle.withBold().withColor(Formatting.BLUE);
		};
	}
	
	private Position renderInline(DocNode node, DrawContext dc, Position position, Deque<BlockContext> contexts, NodeStyle externalStyle) {
		BlockContext context = contexts.peek();
		if (context == null) return position;
		
		String nodeText = node.text();
		
		// FIXME: Should be configurable
		if (node.type() == NodeType.LIST_ITEM) {
			nodeText = "\u2022 " + nodeText;
		}
		
		// FIXME: Should be customizable
		NodeStyle newStyle = switch (node.type()) {
			case NodeType.EMPHASIS -> externalStyle.withItalic();
			case NodeType.STRONG_EMPHASIS -> ((StrongEmphasis) node.node()).getOpeningDelimiter().startsWith("*") ? externalStyle.withBold() : externalStyle.withItalic();
			case NodeType.CUSTOM_NODE -> node.node() instanceof Strikethrough ? externalStyle.withStrikethrough() : externalStyle;
			default -> externalStyle;
		};

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
