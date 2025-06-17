package blue.endless.enoki.markdown.styles.properties;

public class IntStyleProperty extends StyleProperty<Integer> {
	private final int lowerBoundInclusive;
	private final int upperBoundInclusive;
	
	private IntStyleProperty(String name, int lowerBoundInclusive, int upperBoundInclusive) {
		super(Integer.class, name);
		
		this.lowerBoundInclusive = lowerBoundInclusive;
		this.upperBoundInclusive = upperBoundInclusive;
	}
	
	public static IntStyleProperty of(String name, int lowerBoundInclusive, int upperBoundInclusive) {
		return new IntStyleProperty(name, lowerBoundInclusive, upperBoundInclusive);
	}

	public static IntStyleProperty of(String name) {
		return IntStyleProperty.of(name, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	public static IntStyleProperty nonNegativeOnly(String name) {
		return IntStyleProperty.of(name, 0, Integer.MAX_VALUE);
	}
	
	public static IntStyleProperty positiveOnly(String name) {
		return IntStyleProperty.of(name, 1, Integer.MAX_VALUE);
	}

	@Override
	public boolean isValid(Integer value) {
		if (value == null) return false;
		return lowerBoundInclusive <= value && value <= upperBoundInclusive;
	}
}
