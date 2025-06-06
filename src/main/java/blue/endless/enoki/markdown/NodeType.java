package blue.endless.enoki.markdown;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.commonmark.node.*;

@Environment(EnvType.CLIENT)
public enum NodeType {
	BLOCK_QUOTE(BlockQuote.class, 1, true, 2),
	BULLET_LIST(BulletList.class, 1, true, 4),
	CODE(Code.class, 0, true, 2),
	CUSTOM_BLOCK(CustomBlock.class, 0, true, 2),
	CUSTOM_NODE(CustomNode.class, 0, false, 0),
	DOCUMENT(Document.class, 0, true, 0),
	EMPHASIS(Emphasis.class, 0, false, 0),
	FENCED_CODE_BLOCK(FencedCodeBlock.class, 0, true, 2),
	HARD_LINE_BREAK(HardLineBreak.class, 0, false, 0),
	HEADING(Heading.class, 0, true, 4),
	HTML_BLOCK(HtmlBlock.class, 1, true, 2),
	HTML_INLINE(HtmlInline.class, 0, false, 0),
	IMAGE(Image.class, 0, false, 0),
	INDENTED_CODE_BLOCK(IndentedCodeBlock.class, 1, true, 2),
	LINK(Link.class, 0, false, 0),
	LINK_REFERENCE_DEFINITION(LinkReferenceDefinition.class, 0, false, 0),
	LIST_ITEM(ListItem.class, 0, false, 2),
	ORDERED_LIST(OrderedList.class, 1, true, 4),
	PARAGRAPH(Paragraph.class, 0, true, 4),
	SOFT_LINE_BREAK(SoftLineBreak.class, 0, false, 0),
	STRONG_EMPHASIS(StrongEmphasis.class, 0, false, 0),
	TEXT(Text.class, 0, false, 0),
	THEMATIC_BREAK(ThematicBreak.class, 0, true, 4),
	;
	
	private final Class<? extends Node> clazz;
	private final int indent;
	private final boolean isBlock;
	private final int marginBottom;
	
	NodeType(Class<? extends Node> clazz, int indent, boolean isBlock, int marginBottom) {
		this.clazz = clazz;
		this.indent = indent;
		this.isBlock = isBlock;
		this.marginBottom = marginBottom;
	}
	
	public static NodeType getByClass(Class<? extends Node> clazz) {
		for (NodeType enumValue : values()) {
			if (enumValue.clazz == clazz) return enumValue;
		}
		
		return NodeType.CUSTOM_NODE;
	}
	
	public Class<? extends Node> getClazz() {
		return clazz;
	}
	
	public int getIndent() {
		return indent;
	}
	
	public boolean isBlock() {
		return isBlock;
	}
	
	public boolean isInline() {
		return !isBlock;
	}
	
	public int getBottomMargin() {
		return marginBottom;
	}
}
