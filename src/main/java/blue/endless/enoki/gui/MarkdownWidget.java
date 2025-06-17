package blue.endless.enoki.gui;

import blue.endless.enoki.EnokiClient;
import blue.endless.enoki.gui.widgets.AbstractMarkdownWidget;
import blue.endless.enoki.gui.widgets.BlockContainerWidget;
import blue.endless.enoki.gui.widgets.FlowContainerWidget;
import blue.endless.enoki.gui.widgets.HeadingWidget;
import blue.endless.enoki.gui.widgets.ImageWidget;
import blue.endless.enoki.gui.widgets.TextSpanWidget;
import blue.endless.enoki.markdown.DocNode;
import blue.endless.enoki.markdown.NodeType;
import blue.endless.enoki.markdown.styles.LayoutStyle;
import blue.endless.enoki.text.WordWrap;
import com.mojang.blaze3d.textures.GpuTexture;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Environment(EnvType.CLIENT)
public class MarkdownWidget extends ContainerWidget {
	private static final Logger LOGGER = LogManager.getLogger(MarkdownWidget.class);
	
	private DocNode document;
	private final WordWrap wordWrap;
	private TextRenderer font;
	private Map<@NotNull NodeType, LayoutStyle> layoutMap;
	
	private List<ClickableWidget> currentChildren = new ArrayList<>();
	
	public MarkdownWidget(int x, int y, int width) {
		super(x, y, width, 0, Text.empty());
		
		this.wordWrap = new WordWrap();
		this.font = MinecraftClient.getInstance().textRenderer;
		this.layoutMap = EnokiClient.styleManager.getStyleSheet(Identifier.of("enoki:styles/test.json")).get().bake();
		for (Map.Entry<NodeType, LayoutStyle> entry : this.layoutMap.entrySet()) {
			NodeType type = entry.getKey();
			LayoutStyle style = entry.getValue();
			
			LOGGER.info("{}: {}", type, style);
		}
	}
	
	/*
	public MarkdownWidget(int x, int y, int width, int height, boolean scrollable, Identifier documentId) {
		this(x, y, width, height, scrollable);
		
		this.setDocument(documentId);
	}*/
	
	public void setFont(TextRenderer font) {
		this.font = requireNonNull(font);
		this.rebuildWidgets();
	}
	
	/*
	public void setDocument(@NotNull Identifier documentId) {
		Optional<DocNode> document = EnokiClient.MARKDOWN_RESOURCES.get(documentId);
		DocNode document = MarkdownResources.getDocumentOrFallback(documentId);
		if (document == null) {
			LOGGER.error("Could not find document id {}!", documentId);
			return;
		}
		
		this.setDocument(document);
	}*/
	
	public void setDocument(@NotNull DocNode document) {
		this.document = document;
		this.rebuildWidgets();
	}
	
	public void setLayoutMap(Map<@NotNull NodeType, LayoutStyle> layoutMap) {
		this.layoutMap = layoutMap;
		this.rebuildWidgets();
	}
	
	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		this.rebuildWidgets();
	}
	
	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		/*
		dc.getMatrices().push();
		dc.fill(getX(), getY(), getX() + width, getY() + height, Colors.GRAY);
		
		for (ClickableWidget child : currentChildren) {
			child.render(dc, mouseX, mouseY, deltaTicks);
		}

		dc.getMatrices().pop();*/
		context.fill(getX(), getY(), getX() + width, getY() + height, Colors.GRAY);
		
		context.getMatrices().push();
		context.getMatrices().translate(this.getX(), this.getY() - this.getScrollY(), 0);
		
		for(ClickableWidget widget : currentChildren) {
			
			context.getMatrices().push();
			context.getMatrices().translate(widget.getX(), widget.getY(), 0);
			
			//context.enableScissor(0, 0, width, height);
			
			widget.render(context, mouseX - widget.getX(), mouseY - widget.getY(), deltaTicks);
			
			//context.disableScissor();
			
			context.getMatrices().pop();
		}
		
		context.getMatrices().pop();
	}
	
	

	//@Override
	//public void setY(int y) {
	//	for (ClickableWidget child : currentChildren) {
	//		child.setY(child.getY() + (y - this.getY()));
	//	}
		
	//	super.setY(y);
	//}

	private void rebuildWidgets() {
		
		
		
		//Deque<BlockContext> contexts = new ArrayDeque<>();
		//contexts.push(new BlockContext(getX() + 8, getY() + 8, getWidth() - 24)); // TODO: This should be controlled by insets, and should probably default to 8 on all sides.

		LayoutStyle innerStyle = this.layoutMap.get(NodeType.DOCUMENT);
		
		this.currentChildren.clear();
		ClickableWidget rootWidget = buildBlock(document, getWidth(), innerStyle);
		System.out.println("Built document block: "+rootWidget.getWidth()+" x "+rootWidget.getHeight());
		this.currentChildren.add(rootWidget);
		//Position finalPosition = buildWidgets(document, Position.of(getX(), getY()), contexts, NodeStyle.NORMAL);
		
		// Terminate the last line if there is one.
		//ScreenRect rect = contexts.peek().line().layout();
		//finalPosition = finalPosition.withOffset(0, rect.height());
		
		this.setHeight(rootWidget.getHeight());
	}
	
	private LayoutStyle getDefaultedInnerStyle(NodeType type, NodeType defaultType, LayoutStyle outerStyle) {
		LayoutStyle innerStyle = layoutMap.getOrDefault(type, null);
		if (innerStyle == null) {
			innerStyle = layoutMap.get(defaultType);
		}
		
		if (innerStyle == null) {
			innerStyle = LayoutStyle.defaulted();
		}

		innerStyle = innerStyle.copy();
		innerStyle.applyDefaults(outerStyle);
		return innerStyle;
	}
	
	private ClickableWidget buildBlock(DocNode node, int width, LayoutStyle externalStyle) {
		System.out.println("Building block of type "+node.type());
		AbstractMarkdownWidget result = switch(node.type()) {
			case H1, H2, H3, H4, H5, H6 -> new HeadingWidget(width, externalStyle);
			case IMAGE -> new ImageWidget(0, 0, width, 64, Text.literal(""), Identifier.of("minecraft:stone"), font, externalStyle);
			default -> new BlockContainerWidget(width, externalStyle);
		};
		
		
		if (result instanceof BlockContainerWidget block) for(DocNode child : node.children()) {
			LayoutStyle innerStyle = this.getDefaultedInnerStyle(child.type(), NodeType.TEXT, externalStyle);
			
			if (child.type().isBlock()) {
				//TODO: margins? Indents?
				ClickableWidget childBlock = buildBlock(child, this.getWidth(), innerStyle);
				block.add(childBlock);
			} else {
				ClickableWidget childFlow = buildFlow(child, this.getWidth(), innerStyle);
				
				block.add(childFlow);
			}
		}
		
		return result;
	}
	
	private ClickableWidget buildFlow(DocNode node, int width, LayoutStyle externalStyle) {
		System.out.println("Building flow element of type " + node.type() + " with style" + externalStyle);
		AbstractMarkdownWidget result = switch (node.type()) {
			case TEXT -> new TextSpanWidget(node.text(), externalStyle, MinecraftClient.getInstance().textRenderer);
			
			// TODO: Some more types
			default -> new FlowContainerWidget(externalStyle);
		};
		
		if (result instanceof FlowContainerWidget container) {
			for(DocNode child : node.children()) {
				LayoutStyle innerStyle = this.getDefaultedInnerStyle(child.type(), NodeType.TEXT, externalStyle);
				container.add(buildFlow(child, width, innerStyle));
			}
		}
		
		return result;
	}
	
	
	/*
	private Position buildWidgets(DocNode node, Position position, Deque<BlockContext> contexts, NodeStyle style) {
		if (node.type().isBlock()) {
			return buildBlockWidgets(node, position, contexts, style);
		}
		
		return buildInlineWidgets(node, position, contexts, style);
	}
	
	private Position buildBlockWidgets(DocNode node, Position nextPosition, Deque<BlockContext> contexts, NodeStyle externalStyle) {
		LayoutStyle layout = layoutMap.getOrDefault(node.type(), LayoutStyle.TEXT);
		
		NodeStyle newStyle = layout.style().withDefaults(externalStyle);
		Margins margins = layout.margins();
		
		BlockContext context = contexts.peek();
		if (context == null) return nextPosition;
		
		// If we have a line of flow elements pending, terminate it.
		ScreenRect rect = context.line().layout();
		nextPosition = nextPosition.withOffset(0, rect.height());
		context.line().clearLine();
		
		//context.line().advanceLine(context.line().layout());
		//nextPosition = context.line().startPosition();
		
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
		
		if (node.type() == NodeType.BLOCK_QUOTE) {
			nextPosition = buildBlockQuoteWidget(node, nextPosition, contexts, newStyle);
		} else {
			for (DocNode child : node.children()) {
				nextPosition = buildWidgets(child, nextPosition, contexts, newStyle);
			}
		}
		
		// Never flow from an inner block to an outer one.
		ScreenRect lastInnerLine = innerContext.line().layout();
		nextPosition = nextPosition.withOffset(0, lastInnerLine.height());
		
		contexts.pop();
		
		int nextY = nextPosition.y() + margins.bottom();
		if (nextPosition.x() != context.x()) {
			nextY += newStyle.applyScale(font.fontHeight);
		}
		
		Position result = Position.of(context.x(), nextY);
		
		context.line().setStartPosition(result);
		
		return result;
	}*/
	/*
	private Position buildBlockQuoteWidget(DocNode node, Position position, Deque<BlockContext> contexts, NodeStyle style) {
		// FIXME: Add padding to some better place (possibly LayoutStyle?)
		final int VERTICAL_PADDING = 2;
		
		List<ClickableWidget> previousChildren = this.currentChildren;
		this.currentChildren = new ArrayList<>();

		BlockContext outerContext = contexts.peek();
		assert outerContext != null; // Can't be null, since buildBlockWidgets will always push one for us.
		
		Position nextPosition = position.withOffset(ScreenAxis.VERTICAL, VERTICAL_PADDING);

		BlockQuoteInfo info = BlockQuoteInfo.of((String) node.attributes());
		if (!info.equals(BlockQuoteInfo.DEFAULT)) {
			BlockQuoteTitleWidget titleWidget = new BlockQuoteTitleWidget(nextPosition.x(), nextPosition.y(), info, this.font, style);
			this.currentChildren.add(titleWidget);
			nextPosition = nextPosition.withOffset(ScreenAxis.VERTICAL, style.applyScale(titleWidget.getHeight() + font.fontHeight));
		}

		nextPosition = buildChildren(node, nextPosition, contexts, style);
		
		ClickableWidget lastChild = this.currentChildren.isEmpty() ? null : this.currentChildren.getLast();
		if (lastChild == null) {
			// We don't need an empty widget.
			this.currentChildren = previousChildren;
			return nextPosition;
		}
		
		// There might be extraneous margins left over at the end. That looks ugly, so we calculate
		// the real size of the quote block and use that.
		int blockHeight = (lastChild.getY() + lastChild.getHeight()) - position.y() + VERTICAL_PADDING;
		nextPosition = Position.of(nextPosition.x(), outerContext.y() + blockHeight);
		
		BlockQuoteWidget widget = new BlockQuoteWidget(position.x(), position.y(), outerContext.width(), blockHeight, info.color(), style, this.currentChildren);
		this.currentChildren = previousChildren;
		this.currentChildren.add(widget);
		
		return nextPosition;
	}
	
	private Position buildChildren(DocNode node, Position position, Deque<BlockContext> contexts, NodeStyle style) {
		for (DocNode child : node.children()) {
			position = buildWidgets(child, position, contexts, style);
		}
		
		return position;
	}
	
	private Position buildInlineWidgets(DocNode node, Position position, Deque<BlockContext> contexts, NodeStyle externalStyle) {
		BlockContext context = contexts.peek();
		if (context == null) return position;

		LayoutStyle layout = layoutMap.getOrDefault(node.type(), LayoutStyle.TEXT);
		NodeStyle newStyle = layout.style().withDefaults(externalStyle);
		String nodeText = node.text();
		
		if (node.type() == NodeType.LINK) {
			// TODO: Link handlers happen in a different stage of the pipeline now - as a setting on the container widget itself
			newStyle = appendLinkHandlers(node, newStyle);
		}
		
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
		if (!(node.attributes() instanceof DocImageAttributes attributes)) {
			throw new IllegalStateException("Expected image attributes to be instanceof DocImageAttributes");
		}

		if (!attributes.isInline() && position.x() != context.x()) {
			position = Position.of(context.x(), position.y() + style.applyScale(font.fontHeight));
		}

		Identifier imageId = attributes.imageId();
		if (imageId == null) return position;
		
		Size childSize = getImageSize(attributes, style, imageId);
		if (childSize == null) {
			LOGGER.error("Failed to get image size for image id {}!",  imageId);
			return position;
		}

		Text altText = node.asText(NodeStyle.NORMAL,
			(type) -> layoutMap.getOrDefault(node.type(), LayoutStyle.TEXT).style());

		ClickableWidget child = new ImageWidget(position.x(), position.y(), childSize.width(), childSize.height(), altText, imageId, font, style);
		this.currentChildren.add(child);
		
		Position nextPosition = Position.of(position.x() + childSize.width(), position.y());
		
		if (!attributes.isInline() || ((nextPosition.x() - context.x()) > context.width())) {
			nextPosition = Position.of(context.x(), position.y() + childSize.height());
		}
		
		return nextPosition;
	}
	
	
	
	private Size getImageSize(DocImageAttributes attributes, NodeStyle style, Identifier imageId) {
		int width = attributes.size().width();
		int height = attributes.size().height();
		
		if (width != -1 && height != -1) {
			return attributes.size();
		}
		
		Size actualSize = getActualImageSize(imageId);
		if (actualSize == null) return null;

		// FIXME: Aspect ratio stuff
		return actualSize.scale(style.size());
	}
	
	private NodeStyle appendLinkHandlers(DocNode node, NodeStyle style) {
		if (node.type() != NodeType.LINK || !(node.attributes() instanceof LinkInfo linkInfo)) return style;
		return style;
		//return style.withOnClick(new ExternalLinkClickEventHandler(linkInfo.destination()));
	}
	
	private Position buildInlineText(Position position, NodeStyle style, String nodeText, BlockContext context) {
		Text lastLine = Text.empty();
		Position nextPosition = position;
		String remainingText = nodeText;
		
		while (!remainingText.isEmpty()) {
			int incomingIndent = nextPosition.x() - context.x();
			int availableLineWidth = context.width() - incomingIndent;
			
			//int availableLineWidth = context.line().remainingSpacePlusPadding();
			//System.out.println("Available width - Line: "+availableLineWidth+" Calc: "+(context.width() - incomingIndent));
			
			String nextLine = wordWrap.getFirstLine(font, availableLineWidth, remainingText, style);
			lastLine = Text.literal(nextLine).setStyle(style.asStyle());
			
			ClickableWidget child = new TextSpanWidget(nextPosition.x(), nextPosition.y(), lastLine, style, this.font);
			//context.line().add(child);
			this.currentChildren.add(child);

			// TODO: Check if just stripping spaces (and tabs?) is enough
			remainingText = remainingText.substring(nextLine.length()).stripLeading();
			if (!remainingText.isEmpty()) {
				// The last line might have some space left over, which could still be used by children.
				nextPosition = Position.of(context.x(), nextPosition.y() + style.applyScale(font.fontHeight));
			}
		}
		
		return nextPosition.withOffset(style.getTextWidth(lastLine, font), 0);
	}*/
	
	public static Size getActualImageSize(Identifier imageId) {
		try {
			GpuTexture tex = MinecraftClient.getInstance().getTextureManager().getTexture(imageId).getGlTexture();
			System.out.println("Sizeof "+imageId+": "+tex.getWidth(0)+" x "+tex.getHeight(0));
			return new Size(tex.getWidth(0), tex.getHeight(0));
		} catch (IllegalStateException ex) {
			return new Size(0,0);
		}
	}
	
	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		
	}

	@Override
	protected int getContentsHeightWithPadding() {
		return this.height;
	}

	@Override
	protected double getDeltaYPerScroll() {
		return 0.0;
	}

	@Override
	public List<? extends Element> children() {
		return this.currentChildren;
	}
}
