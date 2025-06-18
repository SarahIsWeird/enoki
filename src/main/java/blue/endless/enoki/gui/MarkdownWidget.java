package blue.endless.enoki.gui;

import blue.endless.enoki.EnokiClient;
import blue.endless.enoki.gui.widgets.AbstractMarkdownWidget;
import blue.endless.enoki.gui.widgets.BlockContainerWidget;
import blue.endless.enoki.gui.widgets.FlowContainerWidget;
import blue.endless.enoki.gui.widgets.HeadingWidget;
import blue.endless.enoki.gui.widgets.ImageWidget;
import blue.endless.enoki.gui.widgets.TextSpanWidget;
import blue.endless.enoki.gui.widgets.quote.BlockQuoteInfo;
import blue.endless.enoki.gui.widgets.quote.BlockQuoteWidget;
import blue.endless.enoki.markdown.DocNode;
import blue.endless.enoki.markdown.NodeType;
import blue.endless.enoki.markdown.styles.LayoutStyle;
import blue.endless.enoki.markdown.styles.LayoutStyleSheet;
import blue.endless.enoki.markdown.styles.properties.StyleProperties;

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
	//private final WordWrap wordWrap;
	private TextRenderer font;
	private Map<@NotNull NodeType, LayoutStyle> layoutMap;
	
	private List<ClickableWidget> currentChildren = new ArrayList<>();
	
	public MarkdownWidget(int x, int y, int width) {
		super(x, y, width, 0, Text.empty());
		
		//this.wordWrap = new WordWrap();
		this.font = MinecraftClient.getInstance().textRenderer;

		Identifier defaultSheetId = Identifier.of("enoki:default");
		Identifier testSheetId = Identifier.of("enoki:test");
		
		LayoutStyleSheet defaultSheet = EnokiClient.STYLE_MANAGER.getStyleSheet(defaultSheetId).orElse(LayoutStyleSheet.empty());
		LayoutStyleSheet testSheet = EnokiClient.STYLE_MANAGER.getStyleSheet(testSheetId).orElse(LayoutStyleSheet.empty());
		
		testSheet.applyDefaults(defaultSheet);
		this.layoutMap = testSheet.bake();
		
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
	
	

	private void rebuildWidgets() {
		
		LayoutStyle innerStyle = this.layoutMap.get(NodeType.DOCUMENT);
		if (innerStyle == null) innerStyle = LayoutStyle.empty();
		
		this.currentChildren.clear();
		ClickableWidget rootWidget = buildBlock(document, getWidth(), innerStyle);
		System.out.println("Built document block: "+rootWidget.getWidth()+" x "+rootWidget.getHeight());
		this.currentChildren.add(rootWidget);
		
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
		//System.out.println("Building block of type "+node.type());
		AbstractMarkdownWidget result = switch(node.type()) {
			case H1, H2, H3, H4, H5, H6 -> new HeadingWidget(width, externalStyle);
			case IMAGE -> {
				System.out.println("ImageWidget attributes: "+node.attributes());
				ImageWidget image = new ImageWidget(0, 0, width, 64, Text.literal(""), Identifier.of("minecraft:stone"), font, externalStyle);
				yield image;
			}
			case BLOCK_QUOTE -> {
				if (node.attributes() instanceof String str) {
					BlockQuoteInfo info = BlockQuoteInfo.of(str);
					yield new BlockQuoteWidget(width, info, externalStyle);
				} else {
					yield new BlockQuoteWidget(width, BlockQuoteInfo.DEFAULT, externalStyle);
				}
			}
			default -> new BlockContainerWidget(width, externalStyle);
		};
		
		if (!(result instanceof BlockContainerWidget block)) return result;
		
		for (DocNode child : node.children()) {
			LayoutStyle innerStyle = this.getDefaultedInnerStyle(child.type(), NodeType.TEXT, externalStyle);
			
			ClickableWidget childWidget;
			if (child.type().isBlock()) {
				//TODO: margins? Indents?
				
				int blockLeft = innerStyle.getOrDefault(StyleProperties.MARGIN_LEFT, 0);
				int blockRight = innerStyle.getOrDefault(StyleProperties.MARGIN_RIGHT, 0);
				int blockWidth = this.width - blockLeft - blockRight;
				
				childWidget = buildBlock(child, blockWidth, innerStyle);
			} else {
				childWidget = buildFlow(child, this.getWidth(), innerStyle);
			}
			
			block.add(childWidget);
		}
		
		return result;
	}
	
	private ClickableWidget buildFlow(DocNode node, int width, LayoutStyle externalStyle) {
		AbstractMarkdownWidget result = switch (node.type()) {
			case TEXT -> new TextSpanWidget(node.text(), externalStyle, MinecraftClient.getInstance().textRenderer);
			
			// TODO: Some more types
			default -> new FlowContainerWidget(externalStyle);
		};
		
		if (result instanceof FlowContainerWidget container) {
			for(DocNode child : node.children()) {
				LayoutStyle innerStyle;
				if (child.type() == NodeType.TEXT) {
					LayoutStyle textStyle = this.layoutMap.getOrDefault(NodeType.TEXT, LayoutStyle.empty());
					innerStyle = externalStyle.copy();
					innerStyle.applyDefaults(textStyle);
				} else {
					innerStyle = this.getDefaultedInnerStyle(child.type(), NodeType.TEXT, externalStyle);
				}
				
				container.add(buildFlow(child, width, innerStyle));
			}
		}
		
		return result;
	}
	
	public static Size getActualImageSize(Identifier imageId) {
		try {
			GpuTexture tex = MinecraftClient.getInstance().getTextureManager().getTexture(imageId).getGlTexture();
			//System.out.println("Sizeof "+imageId+": "+tex.getWidth(0)+" x "+tex.getHeight(0));
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
