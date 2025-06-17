package blue.endless.enoki.markdown.styles;

import blue.endless.enoki.markdown.NodeType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class LayoutStyleSheet {
	private static final Keyable KEYS = Keyable.forStrings(NodeCategory.KEYS::stream);
	
	public static final Codec<LayoutStyleSheet> CODEC =
		Codec.simpleMap(Codec.STRING.xmap(NodeCategory::getByName, NodeCategory::asString), LayoutStyle.CODEC, KEYS)
			.xmap(LayoutStyleSheet::new, LayoutStyleSheet::getStyles)
			.codec();

	private final Map<NodeCategory, LayoutStyle> styles;
	
	private LayoutStyleSheet() {
		this(new HashMap<>());
	}

	private LayoutStyleSheet(Map<NodeCategory, LayoutStyle> styles) {
		this.styles = new HashMap<>(styles);
	}

	public static LayoutStyleSheet empty() {
		return new LayoutStyleSheet();
	}

	private Map<NodeCategory, LayoutStyle> getStyles() {
		return this.styles;
	}
	
	public void put(@NotNull NodeCategory category, LayoutStyle style) {
		this.styles.put(requireNonNull(category), requireNonNull(style));
	}
	
	public Optional<LayoutStyle> get(@NotNull NodeCategory category) {
		return Optional.ofNullable(this.styles.get(category));
	}
	
	public void applyDefaults(LayoutStyleSheet defaults) {
		for (Map.Entry<NodeCategory, LayoutStyle> entry : defaults.styles.entrySet()) {
			NodeCategory category = entry.getKey();
			LayoutStyle style = entry.getValue();
			
			if (this.styles.containsKey(category)) {
				this.styles.get(category).applyDefaults(style);
			} else {
				this.styles.put(category, style);
			}
		}
	}
	
	public Map<NodeType, LayoutStyle> bake() {
		this.ensureDefaultCategoryIsFilledIn();
		this.fillInDefaultsFromParentCategories();
		
		Map<NodeType, LayoutStyle> bakedStyles = new HashMap<>();
		for (NodeType type : NodeType.values()) {
			NodeCategory category = NodeCategory.getByNodeType(type).orElse(NodeCategory.DEFAULT);
			
			// Must be present, we just filled it in!
			LayoutStyle style = this.styles.get(category);
			bakedStyles.put(type, style);
		}
		
		return bakedStyles;
	}
	
	private void fillInDefaultsFromParentCategories() {
		for (NodeCategory category : NodeCategory.values()) {
			LayoutStyle style = this.get(category).orElse(LayoutStyle.empty());

			for (NodeCategory hierarchyCategory : category.getHierarchy()) {
				if (this.styles.containsKey(hierarchyCategory)) {
					this.styles.get(hierarchyCategory).applyDefaults(style);
				} else {
					this.styles.put(hierarchyCategory, style);
				}
			}
		}
	}
	
	private void ensureDefaultCategoryIsFilledIn() {
		LayoutStyle defaults = LayoutStyle.defaulted();
		
		if (this.styles.containsKey(NodeCategory.DEFAULT)) {
			this.styles.get(NodeCategory.DEFAULT).applyDefaults(defaults);
		} else {
			this.styles.put(NodeCategory.DEFAULT, defaults);
		}
	}
}
