package blue.endless.enoki.markdown.styles;

import blue.endless.enoki.markdown.NodeType;
import blue.endless.enoki.util.NotNullByDefault;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@NotNullByDefault
public class LayoutStyleSheet {
	private static final Keyable KEYS = Keyable.forStrings(NodeCategory.KEYS::stream);
	
	public static final Codec<@NotNull LayoutStyleSheet> CODEC =
		Codec.simpleMap(Codec.STRING.xmap(NodeCategory::getByName, NodeCategory::asString), LayoutStyle.CODEC, KEYS)
			.xmap(LayoutStyleSheet::new, LayoutStyleSheet::getStyles)
			.codec();

	private final Map<@NotNull NodeCategory, @NotNull LayoutStyle> styles;
	
	public LayoutStyleSheet() {
		this.styles = new EnumMap<>(NodeCategory.class);
	}

	public LayoutStyleSheet(Map<NodeCategory, LayoutStyle> styles) {
		/*
		 * We call the no-args constructor first, because the EnumMap constructor throws if the map is empty.
		 * It needs to know the type of the enum to build the internal array, but it's valid for someone
		 * to supply an empty map to this constructor!
		 */
		this();
		
		// we can just put all the entries into the map, and it should Just Work™️.
		this.styles.putAll(styles);
	}

	public static LayoutStyleSheet empty() {
		return new LayoutStyleSheet();
	}

	@SuppressWarnings("null") // Map and Map.Entry issues
	public LayoutStyleSheet copy() {
		LayoutStyleSheet copy = new LayoutStyleSheet();

		for (Map.Entry<NodeCategory, LayoutStyle> entry : this.styles.entrySet()) {
			copy.put(entry.getKey(), entry.getValue().copy());
		}

		return copy;
	}

	private Map<@NotNull NodeCategory, @NotNull LayoutStyle> getStyles() {
		return this.styles;
	}

	public Optional<@NotNull LayoutStyle> get(NodeCategory category) {
		return Optional.ofNullable(this.styles.get(category));
	}
	
	public void put(NodeCategory category, LayoutStyle style) {
		this.styles.put(requireNonNull(category), requireNonNull(style));
	}
	
	@SuppressWarnings("null") // Map and Map.Entry issues
	public void applyDefaults(LayoutStyleSheet defaults) {
		for (Map.Entry<@NotNull NodeCategory, @NotNull LayoutStyle> entry : defaults.styles.entrySet()) {
			NodeCategory category = entry.getKey();
			LayoutStyle style = entry.getValue();
			
			if (this.styles.containsKey(category)) {
				this.styles.get(category).applyDefaults(style);
			} else {
				this.styles.put(category, style);
			}
		}
	}
	
	public Map<@NotNull NodeType, @NotNull LayoutStyle> bake() {
		this.fillInDefaultsFromParentCategories();
		return this.buildNodeTypeMap();
	}
	
	private void fillInDefaultsFromParentCategories() {
		for (NodeCategory category : NodeCategory.values()) {
			@SuppressWarnings("null") // Optional unwrap is ok
			LayoutStyle style = this.get(category).orElse(LayoutStyle.empty());

			for (NodeCategory hierarchyCategory : category.getHierarchy()) {
				this.get(hierarchyCategory).ifPresent(style::applyDefaults);
			}
			
			this.put(category, style);
		}
	}
	
	@SuppressWarnings("null") // More map shenanigans
	private Map<@NotNull NodeType, @NotNull LayoutStyle> buildNodeTypeMap() {
		Map<@NotNull NodeType, @NotNull LayoutStyle> bakedStyles = new EnumMap<>(NodeType.class);
		for (NodeType type : NodeType.values()) {
			NodeCategory category = NodeCategory.getByNodeType(type).orElse(NodeCategory.DEFAULT);

			// Must be present, we just filled it in!
			LayoutStyle style = this.styles.get(category);
			bakedStyles.put(type, style);
		}
		
		return bakedStyles;
	}
}
