package blue.endless.enoki.markdown;

import blue.endless.enoki.gui.Size;
import blue.endless.enoki.markdown.attributes.DocImageAttributes;
import blue.endless.enoki.utils.IntUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.image.attributes.ImageAttributes;
import org.commonmark.node.Code;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public record DocNode(NodeType type, String text, Object attributes, List<DocNode> children) {
	private static final Logger NORM_LOGGER = LogManager.getLogger("DocNode/Normalization");
	
	public DocNode(NodeType type, List<DocNode> children) {
		this(type, "", children);
	}
	
	public DocNode(NodeType type, String text, List<DocNode> children) {
		this(type, text, null, children);
	}
	
	public net.minecraft.text.Text asText(NodeStyle outerStyle, Function<NodeType, NodeStyle> styleGetter) {
		MutableText styledText = net.minecraft.text.Text.empty();
		NodeStyle style = styleGetter.apply(type).combined(outerStyle);
		
		if (text != null && !text.isEmpty()) {
			MutableText ownText = net.minecraft.text.Text.literal(text).setStyle(style.asStyle());
			styledText.append(ownText);
		}
		
		for (DocNode node : children) {
			styledText.append(node.asText(style, styleGetter));
		}
		
		return styledText;
	}
	
	public void appendInto(StringBuilder builder) {
		if (text != null && !text.isEmpty()) {
			builder.append(text);
		}
		
		for (DocNode child : children) {
			child.appendInto(builder);
		}
	}
	
	public String asString() {
		StringBuilder builder = new StringBuilder();
		appendInto(builder);
		return builder.toString();
	}
	
	public static DocNode of(Node node) {
		List<DocNode> children = normalizeChildren(node);
		
		return switch (node) {
			case Text text -> new DocNode(NodeType.TEXT, text.getLiteral(), children);
			case Code code -> new DocNode(NodeType.CODE, code.getLiteral(), children);
			case FencedCodeBlock fencedBlock -> new DocNode(NodeType.FENCED_CODE_BLOCK, fencedBlock.getLiteral(), fencedBlock.getInfo(), children);
			case HardLineBreak ignored -> new DocNode(NodeType.HARD_LINE_BREAK, "\n", children);
			case Heading heading -> {
				NodeType levelHeading = switch(heading.getLevel()) {
					case 1 -> NodeType.H1;
					case 2 -> NodeType.H2;
					case 3 -> NodeType.H3;
					case 4 -> NodeType.H4;
					case 5 -> NodeType.H5;
					default -> NodeType.H6;
				};
				yield new DocNode(levelHeading, "", "", children);
			}
			case HtmlBlock htmlBlock -> new DocNode(NodeType.HTML_BLOCK, htmlBlock.getLiteral(), children);
			case HtmlInline htmlInline -> new DocNode(NodeType.HTML_INLINE, htmlInline.getLiteral(), children);
			case Image image -> normalizeImageNode(image, children);
			case IndentedCodeBlock indentedBlock -> new DocNode(NodeType.INDENTED_CODE_BLOCK, indentedBlock.getLiteral(), children);
			case Link link -> new DocNode(NodeType.LINK, link.getTitle(), link.getDestination(), children);
			// For now, we skip these. In HTML, this is rendered as an anchor.
			case LinkReferenceDefinition ref -> new DocNode(NodeType.LINK_REFERENCE_DEFINITION, "", ref.getLabel() + ":" + ref.getDestination() + ":" + ref.getTitle(), children);
			case OrderedList ordered -> new DocNode(NodeType.ORDERED_LIST, "", Objects.requireNonNullElse(ordered.getMarkerStartNumber(), 1).toString(), children);
			case SoftLineBreak ignored -> new DocNode(NodeType.SOFT_LINE_BREAK, " ", children);
			case Strikethrough strike -> new DocNode(NodeType.STRIKETHROUGH, "", children);
			
			default -> new DocNode(NodeType.getByClass(node.getClass()), children);
		};
	}
	
	private static DocNode normalizeImageNode(Image image, List<DocNode> children) {
		Size imageSize = Size.DEFAULT;
		
		Node lastChild = image.getLastChild();
		if (lastChild instanceof ImageAttributes attributes) {
			int width = IntUtils.parseIntOrDefault(attributes.getAttributes().get("width"), -1);
			int height = IntUtils.parseIntOrDefault(attributes.getAttributes().get("height"), -1);
			imageSize = new Size(width, height);
		}

		Identifier imageLocation = Identifier.tryParse(image.getDestination());
		if (imageLocation == null) {
			NORM_LOGGER.warn("Image location {} is not a valid Identifier", image.getDestination());
		}
		
		DocImageAttributes attrs = new DocImageAttributes(imageLocation, imageSize);
		return new DocNode(NodeType.IMAGE, image.getTitle(), attrs, children);
	}

	public static List<DocNode> normalizeChildren(Node root) {
		Node node = root.getFirstChild();
		List<DocNode> result = new ArrayList<>();
		while (node != null) {
			Node next = node.getNext();
			result.add(of(node));
			node = next;
		}
		return List.copyOf(result);
	}
}
