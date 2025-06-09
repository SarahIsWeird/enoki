package blue.endless.enoki_test;

import blue.endless.enoki.gui.MarkdownWidget;
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
		markdownWidget.setDocument(Identifier.of("enoki_test:test"));
		
		addDrawableChild(markdownWidget);
	}
}
