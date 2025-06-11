package blue.endless.enoki.gui.widgets.quote;

import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record BlockQuoteInfo(@Nullable Identifier iconId, @Nullable Text title, int color) {
	public static final BlockQuoteInfo DEFAULT = new BlockQuoteInfo(Colors.LIGHT_GRAY);
	
	private static final Map<String, Integer> COLORS = Map.of(
		"NOTE", requireNonNull(Formatting.BLUE.getColorValue()),
		"TIP", requireNonNull(Formatting.DARK_GREEN.getColorValue()),
		"IMPORTANT", requireNonNull(Formatting.DARK_PURPLE.getColorValue()),
		"WARNING", requireNonNull(Formatting.GOLD.getColorValue()),
		"CAUTION", requireNonNull(Formatting.DARK_RED.getColorValue())
	);
	
	public BlockQuoteInfo(int color) {
		this(null, null, color);
	}
	
	public static BlockQuoteInfo of(@Nullable String type) {
		if (type == null) return DEFAULT;
		
		Integer color = COLORS.get(type);
		if (color == null) return DEFAULT;
		
		type = type.toLowerCase();
		Text titleText = Text.translatable("enoki.md.block_quote." + type).withColor(color);
		Identifier iconId = Identifier.of("enoki", "textures/markdown/icons/" + type + ".png");
		return new BlockQuoteInfo(iconId, titleText, color + 0xff000000);
	}
}
