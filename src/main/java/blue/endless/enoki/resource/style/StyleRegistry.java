package blue.endless.enoki.resource.style;

import blue.endless.enoki.markdown.styles.LayoutStyleSheet;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StyleRegistry {
	protected final Map<Identifier, LayoutStyleSheet> styleSheets = new HashMap<>();
	
	public Optional<LayoutStyleSheet> getStyleSheet(Identifier id) {
		return Optional.ofNullable(styleSheets.getOrDefault(id, null))
			.map(LayoutStyleSheet::copy);
	}
	
	public void registerStyleSheet(Identifier id, LayoutStyleSheet style) {
		styleSheets.put(id, style);
	}

	void clear() {
		this.styleSheets.clear();
	}
}
