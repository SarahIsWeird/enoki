package blue.endless.enoki.markdown;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

import blue.endless.enoki.util.NotNullByDefault;

import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@NotNullByDefault
public enum NodeType {
	BLOCK_QUOTE("block_quote", BlockQuote.class, true),
	BULLET_LIST("unordered_list", BulletList.class, true, List.of("list")),
	CODE("code_inline", Code.class, true, List.of("code")),
	CUSTOM_BLOCK(null, CustomBlock.class, true),
	CUSTOM_NODE(null, CustomNode.class, false),
	STRIKETHROUGH("strikethrough", CustomNode.class, false),
	DOCUMENT("document", Document.class, true),
	EMPHASIS("emphasis", Emphasis.class, false),
	FENCED_CODE_BLOCK("fenced_code_block", FencedCodeBlock.class, true, List.of("code", "code_block")),
	HARD_LINE_BREAK(null, HardLineBreak.class, false),
	H1("h1", Heading.class, true, List.of("header")),
	H2("h2", Heading.class, true, List.of("header")),
	H3("h3", Heading.class, true, List.of("header")),
	H4("h4", Heading.class, true, List.of("header")),
	H5("h5", Heading.class, true, List.of("header")),
	H6("h6", Heading.class, true, List.of("header")),
	HTML_BLOCK(null, HtmlBlock.class, true),
	HTML_INLINE(null, HtmlInline.class, false),
	IMAGE("image", Image.class, false),
	INDENTED_CODE_BLOCK("indented_code_block", IndentedCodeBlock.class, true, List.of("code", "code_block")),
	LINK("link", Link.class, false),
	LINK_REFERENCE_DEFINITION("link_reference", LinkReferenceDefinition.class, false),
	LIST_ITEM("list_item", ListItem.class, false),
	ORDERED_LIST("ordered_list", OrderedList.class, true, List.of("list")),
	PARAGRAPH("paragraph", Paragraph.class, true),
	SOFT_LINE_BREAK(null, SoftLineBreak.class, false),
	STRONG_EMPHASIS("strong_emphasis", StrongEmphasis.class, false),
	UNDERLINE("underline", StrongEmphasis.class, false),
	TEXT("text", Text.class, false),
	THEMATIC_BREAK(null, ThematicBreak.class, true),
	;
	
	private final @Nullable String name;
	private final Class<? extends Node> clazz;
	private final boolean isBlock;
	private final List<String> parentTypes;
	
	NodeType(@Nullable String name, Class<? extends Node> clazz, boolean isBlock) {
		this(name, clazz, isBlock, List.of());
	}
	
	NodeType(@Nullable String name, Class<? extends Node> clazz, boolean isBlock, List<String> parentTypes) {
		this.name = name;
		this.clazz = clazz;
		this.isBlock = isBlock;
		this.parentTypes = parentTypes;
	}
	
	public static NodeType getByClass(Class<? extends Node> clazz) {
		for (NodeType enumValue : values()) {
			if (enumValue.clazz == clazz) return enumValue;
		}
		
		return NodeType.CUSTOM_NODE;
	}
	
	public static List<NodeType> getByType(String type) {
		List<NodeType> types = new ArrayList<>();
		
		for (NodeType enumValue : values()) {
			if (enumValue.name == null) continue;
			
			if (enumValue.name.equals(type) || enumValue.parentTypes.contains(type)) {
				types.add(enumValue);
			}
		}
		
		return types;
	}
	
	public Class<? extends Node> getClazz() {
		return clazz;
	}
	
	public boolean isBlock() {
		return isBlock;
	}
	
	public boolean isInline() {
		return !isBlock;
	}
}
