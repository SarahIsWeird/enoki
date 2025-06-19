package blue.endless.enoki.markdown.styles.properties;

import org.jetbrains.annotations.NotNull;

import blue.endless.enoki.util.NotNullByDefault;

@NotNullByDefault
public class BooleanStyleProperty extends StyleProperty<@NotNull Boolean> {
	private BooleanStyleProperty(String name) {
		super(Boolean.class, name);
	}
	
	public static BooleanStyleProperty of(String name) {
		return new BooleanStyleProperty(name);
	}

	@Override
	public boolean isValid(Boolean value) {
		return true;
	}
}
