package blue.endless.enoki.gui.widgets.quote;

import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record BlockQuoteInfo(@Nullable Identifier iconId, @Nullable Text title, int color) {
	public static final BlockQuoteInfo DEFAULT = new BlockQuoteInfo(Colors.LIGHT_GRAY);
	
	public BlockQuoteInfo(int color) {
		this(null, null, color);
	}
}
