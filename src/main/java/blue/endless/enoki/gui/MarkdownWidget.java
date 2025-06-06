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
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.node.StrongEmphasis;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
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
		
		// FIXME: This was named `presumedIndent`. When is this indent calculation incorrect?
		int indent = position.x() - context.x();
		int lineWidth = context.width() - indent;
		
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
		
		// FIXME: Why does the first line take the indent into account, but the remaining lines don't?
		String firstLine = wordWrap.getFirstLine(font, lineWidth, nodeText, newStyle.asStyle());
		String remainingText = nodeText.substring(firstLine.length());
		List<String> remainingLines = wordWrap.wrap(font, context.width(), remainingText, newStyle.asStyle());
		
		OrderedText firstLineText = Text.literal(firstLine).setStyle(newStyle.asStyle()).asOrderedText();
		dc.drawText(font, firstLineText, position.x(), position.y(), Colors.WHITE, newStyle.shadow());
		
		boolean forceLineBreak = node.type() == NodeType.LIST_ITEM;
		
		Position endPosition = position;
		
		if (remainingLines.isEmpty()) {
			endPosition = (forceLineBreak) ?
					Position.of(context.x(), position.y() + font.fontHeight + node.type().getBottomMargin())
					:
						Position.of(position.x() + font.getWidth(firstLineText), position.y());
		} else {
			position = Position.of(context.x(), position.y() + font.fontHeight);
			
			OrderedText lastLine = null;
			for (int i = 0; i < remainingLines.size(); i++) {
				String line = remainingLines.get(i);
				lastLine = Text.literal(line).setStyle(newStyle.asStyle()).asOrderedText();
				dc.drawText(font, lastLine, position.x(), position.y(), Colors.WHITE, newStyle.shadow());
				
				if (i < remainingLines.size() - 1) {
					position = Position.of(position.x(), position.y() + font.fontHeight);
				}
			}
			
			endPosition = (forceLineBreak) ?
					Position.of(context.x(), position.y() + node.type().getBottomMargin())
					:
						Position.of(position.x() + font.getWidth(lastLine), position.y());
		}
		
		for (DocNode child : node.children()) {
			endPosition = render(child, dc, endPosition, contexts, newStyle);
		}
		
		return endPosition;
	}
	
	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}
}
