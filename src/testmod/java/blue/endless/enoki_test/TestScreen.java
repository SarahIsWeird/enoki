package blue.endless.enoki_test;

import blue.endless.enoki.gui.MarkdownWidget;
import blue.endless.enoki.markdown.DocNode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import java.util.List;

@Environment(EnvType.CLIENT)
public class TestScreen extends Screen {
	private static final List<Extension> EXTENSIONS = List.of(
		StrikethroughExtension.create(),
		ImageAttributesExtension.create());
	private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
	
	public TestScreen() {
		super(Text.of("Test"));
	}
	
	@Override
	protected void init() {
		super.init();
		clearChildren();
		
		MarkdownWidget markdownWidget = new MarkdownWidget(50, 50, width - 100, height - 100, true);
		
		@SuppressWarnings("MarkdownUnresolvedFileReference")
		Node rawDocument = PARSER.parse("""
				# Hello, world! *:3*
				This is a test.
				
				![This is just a *little* goober.](enoki_test:textures/markdown_images/goober.png){width=300 height=200}
				
				Anyways, this is a *real* long line to test out how it behaves when trying to wrap stuff.
				Throw in a line break just for good measure, which it should be fine with?
				
				A B **Did you know? Sarah can be malicious as well :3 Frick your ~~chicken strips~~ text widths!**
				
				Things I hate:
				- Minecraft's text rendering
					- Wowie
				- Minecraft's text renderer
				
				## Okay
				
				A b c
				
				**D**
				
				E""");
		
		DocNode document = DocNode.of(rawDocument);
		markdownWidget.setDocument(document);
		
		addDrawableChild(markdownWidget);
	}
}
