package blue.endless.enoki;

import blue.endless.enoki.gui.MarkdownWidget;
import blue.endless.enoki.markdown.DocNode;
import blue.endless.enoki.resource.ResourceDecoder;

import com.sarahisweird.commonmark.ext.alerts.AlertExtension;
import com.sarahisweird.commonmark.ext.image_attributes.ImageAttributesExtension;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import java.io.Reader;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Enoki {
	public static final String MOD_ID = "enoki";

	/**
	 * The default extensions. If you want to build your own parser, add these to the extensions.
	 * 
	 * <pre><code>
	 * Parser myParser = Parser.builder()
	 *     .extensions(Enoki.DEFAULT_EXTENSIONS)
	 *     .build();
	 * </code></pre>
	 * 
	 * @see #DEFAULT_PARSER
	 */
	public static final List<Extension> DEFAULT_EXTENSIONS = List.of(
		StrikethroughExtension.create(),
		ImageAttributesExtension.create(Set.of("width", "height", "fill", "inline"), Set.of("fill", "inline")),
		AlertExtension.create()
	);

	/**
	 * The default Enoki parser. Can be used to parse the Markdown text into a Node.
	 * 
	 * Use {@link #parseMarkdown(String)} if you don't need to change behavior.
	 * 
	 * @see DocNode#of(Node) 
	 * @see #DEFAULT_EXTENSIONS
	 */
	public static final Parser DEFAULT_PARSER = Parser.builder()
		.extensions(DEFAULT_EXTENSIONS)
		.build();
	
	public static final ResourceDecoder<DocNode> DEFAULT_DECODER = (id, res) -> {
			try(Reader reader = res.getReader()) {
				Node node = Enoki.DEFAULT_PARSER.parseReader(reader);
				return Optional.of(DocNode.of(node));
			}
		};
	
	/**
	 * Utility method that turns a Markdown document into a DocNode for use with a {@link MarkdownWidget}.
	 * 
	 * @param markdown The markdown to parse
	 * @return The {@link DocNode} representation of the document
	 */
	public static DocNode parseMarkdown(String markdown) {
		Node document = DEFAULT_PARSER.parse(markdown);
		return DocNode.of(document);
	}
}
