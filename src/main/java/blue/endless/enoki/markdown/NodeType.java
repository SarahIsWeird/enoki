package blue.endless.enoki.markdown;

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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum NodeType {
	BLOCK_QUOTE(BlockQuote.class, true),
	BULLET_LIST(BulletList.class, true),
	CODE(Code.class, true),
	CUSTOM_BLOCK(CustomBlock.class, true),
	CUSTOM_NODE(CustomNode.class, false),
	STRIKETHROUGH(CustomNode.class, false),
	DOCUMENT(Document.class, true),
	EMPHASIS(Emphasis.class, false),
	FENCED_CODE_BLOCK(FencedCodeBlock.class, true),
	HARD_LINE_BREAK(HardLineBreak.class, false),
	H1(Heading.class, true),
	H2(Heading.class, true),
	H3(Heading.class, true),
	H4(Heading.class, true),
	H5(Heading.class, true),
	H6(Heading.class, true),
	HTML_BLOCK(HtmlBlock.class, true),
	HTML_INLINE(HtmlInline.class, false),
	IMAGE(Image.class, false),
	INDENTED_CODE_BLOCK(IndentedCodeBlock.class, true),
	LINK(Link.class, false),
	LINK_REFERENCE_DEFINITION(LinkReferenceDefinition.class, false),
	LIST_ITEM(ListItem.class, false),
	ORDERED_LIST(OrderedList.class, true),
	PARAGRAPH(Paragraph.class, true),
	SOFT_LINE_BREAK(SoftLineBreak.class, false),
	STRONG_EMPHASIS(StrongEmphasis.class, false),
	UNDERLINE(StrongEmphasis.class, false),
	TEXT(Text.class, false),
	THEMATIC_BREAK(ThematicBreak.class, true),
	;
	
	private final Class<? extends Node> clazz;
	private final boolean isBlock;
	
	NodeType(Class<? extends Node> clazz, boolean isBlock) {
		this.clazz = clazz;
		this.isBlock = isBlock;
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
	
	public boolean isBlock() {
		return isBlock;
	}
	
	public boolean isInline() {
		return !isBlock;
	}
}
