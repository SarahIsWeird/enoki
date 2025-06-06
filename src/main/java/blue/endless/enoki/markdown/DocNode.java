package blue.endless.enoki.markdown;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.OrderedText;
import org.commonmark.node.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public record DocNode(NodeType type, Node node, String text, String value, List<DocNode> children) {
	public DocNode(NodeType type, Node node, List<DocNode> children) {
		this(type, node, "", children);
	}
	
	public DocNode(NodeType type, Node node, String text, List<DocNode> children) {
		this(type, node, text, "", children);
	}
	
	public OrderedText asText() {
		List<OrderedText> nodes = new ArrayList<>();
		
		if (text != null && !text.isEmpty()) {
			nodes.add(net.minecraft.text.Text.literal(text).asOrderedText());
		}
		
		for (DocNode node : children) {
			nodes.add(node.asText());
		}
		
		return OrderedText.concat(nodes);
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
			case Text text -> new DocNode(NodeType.TEXT, node, text.getLiteral(), children);
			case Code code -> new DocNode(NodeType.CODE, node, code.getLiteral(), children);
			case FencedCodeBlock fencedBlock -> new DocNode(NodeType.FENCED_CODE_BLOCK, node, fencedBlock.getLiteral(), fencedBlock.getInfo(), children);
			case HardLineBreak ignored -> new DocNode(NodeType.HARD_LINE_BREAK, node, "\n", children);
			case Heading heading -> new DocNode(NodeType.HEADING, node, "", "" + heading.getLevel(), children);
			case HtmlBlock htmlBlock -> new DocNode(NodeType.HTML_BLOCK, node, htmlBlock.getLiteral(), children);
			case HtmlInline htmlInline -> new DocNode(NodeType.HTML_INLINE, node, htmlInline.getLiteral(), children);
			case Image image -> new DocNode(NodeType.IMAGE, node, image.getTitle(), image.getDestination(), children);
			case IndentedCodeBlock indentedBlock -> new DocNode(NodeType.INDENTED_CODE_BLOCK, node, indentedBlock.getLiteral(), children);
			case Link link -> new DocNode(NodeType.LINK, node, link.getTitle(), link.getDestination(), children);
			// For now, we skip these. In HTML, this is rendered as an anchor.
			case LinkReferenceDefinition ref -> new DocNode(NodeType.LINK_REFERENCE_DEFINITION, node, "", ref.getLabel() + ":" + ref.getDestination() + ":" + ref.getTitle(), children);
			case OrderedList ordered -> new DocNode(NodeType.ORDERED_LIST, node, Objects.requireNonNullElse(ordered.getMarkerStartNumber(), 1).toString(), children);
			case SoftLineBreak ignored -> new DocNode(NodeType.SOFT_LINE_BREAK, node, " ", children);
			
			default -> new DocNode(NodeType.getByClass(node.getClass()), node, children);
		};
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
