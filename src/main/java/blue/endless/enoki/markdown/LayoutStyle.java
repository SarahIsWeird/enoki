package blue.endless.enoki.markdown;

import blue.endless.enoki.gui.Margins;
import net.minecraft.util.Formatting;

/**
 * Represents the layout and presentation for a node.
 * 
 * <p>We can rethink how this works, but there definitely needs to be an object here that works "like a css block".
 * For example,
 * <code><pre>
 * {
 *   margin: 0px 0px 4px 0px; // bottom margin of 4px
 *   color: AQUA;
 *   size: 1.5em;
 *   font-weight: bold;
 * }
 * </pre></code>
 * 
 * This block can then be attached to a NodeStyle to control its layout and presentation.
 * 
 * <p>This will require a little bit of rethink on the data normalization end: "bold" strong-emphasis will need to be
 * normalized to a different NodeType than "underline". Different heading levels will need to be normalized to unique
 * NodeTypes - I'm envisioning H1, H2, and H3, with higher heading levels being normalized to H3.
 * 
 * <p>Constants provided are DEFAULTS, and not meant to be prescriptive in any way.
 */
public record LayoutStyle(NodeStyle style, int indent, Margins margins) {
	public static LayoutStyle DOCUMENT = new LayoutStyle(NodeStyle.NORMAL, 0, Margins.DOCUMENT);
	public static LayoutStyle TEXT = new LayoutStyle(NodeStyle.NORMAL, 0, Margins.ZERO);
	public static LayoutStyle H1 = new LayoutStyle(
			NodeStyle.NORMAL.withSize(1.5f).withColor(Formatting.AQUA).withBold(),
			0,
			Margins.PAD_BELOW
			);
	public static LayoutStyle H2 = new LayoutStyle(
			NodeStyle.NORMAL.withSize(1.25f).withColor(Formatting.YELLOW).withBold(),
			0,
			Margins.PAD_BELOW
			);
	public static LayoutStyle H3 = new LayoutStyle(
			NodeStyle.NORMAL.withSize(1.1f).withColor(Formatting.LIGHT_PURPLE).withBold(),
			0,
			Margins.PAD_BELOW
			);
	public static LayoutStyle ITALIC = new LayoutStyle(NodeStyle.NORMAL.withItalic(), 0, Margins.ZERO);
	public static LayoutStyle BOLD = new LayoutStyle(NodeStyle.NORMAL.withBold(), 0, Margins.ZERO);
	public static LayoutStyle STRIKETHROUGH = new LayoutStyle(NodeStyle.NORMAL.withStrikethrough(), 0, Margins.ZERO);
	public static LayoutStyle UNDERLINE = new LayoutStyle(NodeStyle.NORMAL.withUnderline(), 0, Margins.ZERO);
	public static LayoutStyle PARAGRAPH = new LayoutStyle(NodeStyle.NORMAL, 0, Margins.PAD_BELOW);
}
