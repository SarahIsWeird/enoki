package blue.endless.enoki.markdown.styles;

import blue.endless.enoki.markdown.NodeType;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum NodeCategory implements StringIdentifiable {
	DEFAULT(null),
	DOCUMENT(NodeType.DOCUMENT),
	
	BLOCK_QUOTE(NodeType.BLOCK_QUOTE),
	IMAGE(NodeType.IMAGE),
	PARAGRAPH(NodeType.PARAGRAPH),
	
	CODE(null),
	INLINE_CODE(NodeType.CODE, CODE),
	
	CODE_BLOCK(null, CODE),
	FENCED_CODE_BLOCK(NodeType.FENCED_CODE_BLOCK, CODE_BLOCK),
	INDENTED_CODE_BLOCK(NodeType.INDENTED_CODE_BLOCK, CODE_BLOCK),
	
	HEADING(null),
	H1(NodeType.H1, HEADING),
	H2(NodeType.H2, HEADING),
	H3(NodeType.H3, HEADING),
	H4(NodeType.H4, HEADING),
	H5(NodeType.H5, HEADING),
	H6(NodeType.H6, HEADING),

	LIST(null),
	ORDERED_LIST(NodeType.ORDERED_LIST, LIST),
	UNORDERED_LIST(NodeType.BULLET_LIST, LIST),
	
	EMPHASIS(NodeType.EMPHASIS),
	LINK(NodeType.LINK),
	LINK_REFERENCE_DEFINITION(NodeType.LINK_REFERENCE_DEFINITION),
	STRIKETHROUGH(NodeType.STRIKETHROUGH),
	STRONG_EMPHASIS(NodeType.STRONG_EMPHASIS),
	UNDERLINE(NodeType.UNDERLINE),
	;
	
	public static final Codec<NodeCategory> CODEC =
		StringIdentifiable.createCodec(NodeCategory::values, String::toUpperCase);
	
	public static final List<String> KEYS = Arrays.stream(NodeCategory.values())
		.map(Enum::name)
		.map(String::toLowerCase)
		.collect(Collectors.toList());
	
	@Nullable private final NodeType actualType;
	@Nullable private final List<NodeCategory> parents;
	
	NodeCategory(@Nullable NodeType actualType) {
		this(actualType, List.of());
	}

	NodeCategory(@Nullable NodeType actualType, NodeCategory ...parents) {
		this(actualType, List.of(parents));
	}
	
	NodeCategory(@Nullable NodeType actualType, @Nullable List<NodeCategory> parents) {
		this.actualType = actualType;
		this.parents = parents;
	}
	
	public static NodeCategory getByName(String name) {
		return NodeCategory.valueOf(name.toUpperCase());
	}
	
	public static Optional<NodeCategory> getByNodeType(@NotNull NodeType type) {
		for (NodeCategory category : NodeCategory.values()) {
			if (type.equals(category.actualType)) {
				return Optional.of(category);
			}
		}
		
		return Optional.empty();
	}
	
	@Nullable
	public NodeType getActualType() {
		return actualType;
	}
	
	@NotNull
	public List<NodeCategory> getHierarchy() {
		if (this == NodeCategory.DEFAULT) return new ArrayList<>();
		
		List<NodeCategory> hierarchy = new ArrayList<>();
		if (this.parents != null) hierarchy.add(NodeCategory.DEFAULT);
		
		fillInParents(hierarchy);
		return hierarchy;
	}
	
	private void fillInParents(List<NodeCategory> hierarchy) {
		if (this.parents != null) {
			for (NodeCategory parent : this.parents) {
				parent.fillInParents(hierarchy);
			}
		}
		
		if (!hierarchy.contains(this)) {
			hierarchy.add(this);
		}
	}

	@Override
	public String asString() {
		return this.toString().toLowerCase();
	}
}
