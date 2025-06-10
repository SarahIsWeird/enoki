package blue.endless.enoki_test;

import org.commonmark.node.Document;

import blue.endless.enoki.gui.MarkdownWidget;
import blue.endless.enoki.markdown.DocNode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TestScreen extends Screen {
	public TestScreen() {
		super(Text.of("Test"));
	}
	
	@Override
	protected void init() {
		super.init();
		clearChildren();
		
		MarkdownWidget markdownWidget = new MarkdownWidget(50, 50, width - 100, height - 100, true);
		DocNode document = EnokiTestClient.MARKDOWN_RESOURCES.get(Identifier.of("enoki_test:test")).or(DocNode.of(new Document()));
		markdownWidget.setDocument(document);
		
		addDrawableChild(markdownWidget);
	}
}
