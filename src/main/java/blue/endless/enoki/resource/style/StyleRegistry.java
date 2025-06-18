package blue.endless.enoki.resource.style;

import blue.endless.enoki.markdown.styles.LayoutStyleSheet;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StyleRegistry {
	protected final Map<Identifier, LayoutStyleSheet> styleSheets = new HashMap<>();

	/**
	 * Resolves a previously registered style sheet by its id.
	 * 
	 * @param id The style sheet id
	 * @return An {@link Optional} possibly containing the resolved style sheet.
	 */
	public Optional<LayoutStyleSheet> getStyleSheet(Identifier id) {
		return Optional.ofNullable(styleSheets.getOrDefault(id, null))
			.map(LayoutStyleSheet::copy);
	}
	
	public void registerStyleSheet(Identifier id, LayoutStyleSheet style) {
		styleSheets.put(id, style);
	}

	/**
	 * Clears all registered style sheets in preparation for a resource reload. To prevent misuse,
	 * it's package-private.
	 * <p>
	 * This method is used internally in {@link StyleReloadListener#beforeApply()}.
	 */
	void clear() {
		this.styleSheets.clear();
	}
}
