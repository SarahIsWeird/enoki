package blue.endless.enoki_test;

import blue.endless.enoki.gui.ScrollableMarkdownWidget;
import blue.endless.enoki.markdown.DocNode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.commonmark.node.Document;

@Environment(EnvType.CLIENT)
public class TestScreen extends Screen {
	private ScrollableMarkdownWidget markdownWidget;

	public TestScreen() {
		super(Text.of("Test"));
	}
	
	@Override
	protected void init() {
		super.init();
		clearChildren();
		
		if (this.markdownWidget == null) {
			markdownWidget = new ScrollableMarkdownWidget(50, 50, width - 100, height - 100);
			DocNode document = EnokiTestClient.MARKDOWN_RESOURCES.get(Identifier.of("enoki_test:test")).or(DocNode.of(new Document()));
			markdownWidget.setDocument(document);
		}
		
		addDrawableChild(markdownWidget);
	}

	@Override
	protected void refreshWidgetPositions() {
		this.markdownWidget.setWidth(width - 100);
	}
}
