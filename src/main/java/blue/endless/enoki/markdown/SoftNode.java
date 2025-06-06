package blue.endless.enoki.markdown;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.OrderedText;
import org.commonmark.node.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public record SoftNode(SoftNodeType type, Node node, String text, String value, List<SoftNode> children) {
    public SoftNode(SoftNodeType type, Node node, List<SoftNode> children) {
        this(type, node, "", children);
    }

    public SoftNode(SoftNodeType type, Node node, String text, List<SoftNode> children) {
        this(type, node, text, "", children);
    }

    public OrderedText asText() {
        List<OrderedText> nodes = new ArrayList<>();

        if (text != null && !text.isEmpty()) {
            nodes.add(net.minecraft.text.Text.literal(text).asOrderedText());
        }

        for (SoftNode node : children) {
            nodes.add(node.asText());
        }

        return OrderedText.concat(nodes);
    }

    public void appendInto(StringBuilder builder) {
        if (text != null && !text.isEmpty()) {
            builder.append(text);
        }

        for (SoftNode child : children) {
            child.appendInto(builder);
        }
    }

    public String asString() {
        StringBuilder builder = new StringBuilder();
        appendInto(builder);
        return builder.toString();
    }

    public static SoftNode of(Node node) {
        List<SoftNode> children = normalizeChildren(node);

        return switch (node) {
            case Text text -> new SoftNode(SoftNodeType.TEXT, node, text.getLiteral(), children);
            case Code code -> new SoftNode(SoftNodeType.CODE, node, code.getLiteral(), children);
            case FencedCodeBlock fencedBlock -> new SoftNode(SoftNodeType.FENCED_CODE_BLOCK, node, fencedBlock.getLiteral(), fencedBlock.getInfo(), children);
            case HardLineBreak ignored -> new SoftNode(SoftNodeType.HARD_LINE_BREAK, node, "\n", children);
            case Heading heading -> new SoftNode(SoftNodeType.HEADING, node, "", "" + heading.getLevel(), children);
            case HtmlBlock htmlBlock -> new SoftNode(SoftNodeType.HTML_BLOCK, node, htmlBlock.getLiteral(), children);
            case HtmlInline htmlInline -> new SoftNode(SoftNodeType.HTML_INLINE, node, htmlInline.getLiteral(), children);
            case Image image -> new SoftNode(SoftNodeType.IMAGE, node, image.getTitle(), image.getDestination(), children);
            case IndentedCodeBlock indentedBlock -> new SoftNode(SoftNodeType.INDENTED_CODE_BLOCK, node, indentedBlock.getLiteral(), children);
            case Link link -> new SoftNode(SoftNodeType.LINK, node, link.getTitle(), link.getDestination(), children);
            // For now, we skip these. In HTML, this is rendered as an anchor.
            case LinkReferenceDefinition ref -> new SoftNode(SoftNodeType.LINK_REFERENCE_DEFINITION, node, "", ref.getLabel() + ":" + ref.getDestination() + ":" + ref.getTitle(), children);
            case OrderedList ordered -> new SoftNode(SoftNodeType.ORDERED_LIST, node, Objects.requireNonNullElse(ordered.getMarkerStartNumber(), 1).toString(), children);
            case SoftLineBreak ignored -> new SoftNode(SoftNodeType.SOFT_LINE_BREAK, node, " ", children);

            default -> new SoftNode(SoftNodeType.getByClass(node.getClass()), node, children);
        };
    }

    public static List<SoftNode> normalizeChildren(Node root) {
        Node node = root.getFirstChild();
        List<SoftNode> result = new ArrayList<>();
        while (node != null) {
            Node next = node.getNext();
            result.add(of(node));
            node = next;
        }
        return List.copyOf(result);
    }
}
