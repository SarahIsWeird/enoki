package blue.endless.enoki.gui.widgets;

import blue.endless.enoki.markdown.NodeStyle;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public class TextSpanWidget extends AbstractTextWidget {
	public TextSpanWidget(int x, int y, Text message, NodeStyle style, TextRenderer font) {
		super(x, y, message, style, font);
	}
}
