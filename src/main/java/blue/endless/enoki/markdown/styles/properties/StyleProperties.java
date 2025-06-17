package blue.endless.enoki.markdown.styles.properties;

import java.util.List;

public class StyleProperties {
	public static final FloatStyleProperty SIZE = FloatStyleProperty.positiveOnly("size");
	
	public static final IntStyleProperty COLOR = IntStyleProperty.of("color");
	public static final IntStyleProperty BACKGROUND_COLOR = IntStyleProperty.of("backgroundColor");
	
	public static final BooleanStyleProperty BOLD = BooleanStyleProperty.of("bold");
	public static final BooleanStyleProperty ITALIC = BooleanStyleProperty.of("italic");
	public static final BooleanStyleProperty UNDERLINE = BooleanStyleProperty.of("underline");
	public static final BooleanStyleProperty STRIKETHROUGH = BooleanStyleProperty.of("strikethrough");
	public static final BooleanStyleProperty SHADOW = BooleanStyleProperty.of("shadow");
	
	public static final IntStyleProperty INDENT = IntStyleProperty.nonNegativeOnly("indent");
	
	public static final IntStyleProperty MARGIN_TOP = IntStyleProperty.of("margin_top");
	public static final IntStyleProperty MARGIN_RIGHT = IntStyleProperty.of("margin_right");
	public static final IntStyleProperty MARGIN_BOTTOM = IntStyleProperty.of("margin_bottom");
	public static final IntStyleProperty MARGIN_LEFT = IntStyleProperty.of("margin_left");
	
	public static final List<StyleProperty<?>> PROPERTIES =
		List.of(
			SIZE,
			COLOR, BACKGROUND_COLOR,
			BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, SHADOW,
			INDENT,
			MARGIN_TOP, MARGIN_RIGHT, MARGIN_BOTTOM, MARGIN_LEFT
		);
}
