package blue.endless.enoki.markdown.styles.properties;

public class FloatStyleProperty extends StyleProperty<Float> {
	private final float lowerBoundInclusive;
	private final float upperBoundInclusive;
	
	private FloatStyleProperty(String name, float lowerBoundInclusive, float upperBoundInclusive) {
		super(Float.class, name);
		
		this.lowerBoundInclusive = lowerBoundInclusive;
		this.upperBoundInclusive = upperBoundInclusive;
	}
	
	public static FloatStyleProperty of(String name, float lowerBoundInclusive, float upperBoundInclusive) {
		return new FloatStyleProperty(name, lowerBoundInclusive, upperBoundInclusive);
	}

	public static FloatStyleProperty of(String name) {
		return FloatStyleProperty.of(name, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
	}

	public static FloatStyleProperty nonNegativeOnly(String name) {
		return FloatStyleProperty.of(name, 0, Float.MAX_VALUE);
	}
	
	public static FloatStyleProperty positiveOnly(String name) {
		float lowerBound = Float.MIN_VALUE; // Check the JavaDocs on Float.MIN_VALUE!
		
		return FloatStyleProperty.of(name, lowerBound, Float.MAX_VALUE);
	}

	@Override
	public boolean isValid(Float value) {
		if (value == null) return false;
		return lowerBoundInclusive <= value && value <= upperBoundInclusive;
	}
}
