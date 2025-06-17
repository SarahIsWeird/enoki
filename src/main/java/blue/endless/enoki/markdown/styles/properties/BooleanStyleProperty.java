package blue.endless.enoki.markdown.styles.properties;

public class BooleanStyleProperty extends StyleProperty<Boolean> {
	private BooleanStyleProperty(String name) {
		super(Boolean.class, name);
	}
	
	public static BooleanStyleProperty of(String name) {
		return new BooleanStyleProperty(name);
	}

	@Override
	public boolean isValid(Boolean value) {
		return value != null;
	}
}
