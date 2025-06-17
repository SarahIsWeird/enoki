package blue.endless.enoki.markdown.styles.properties;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public abstract class StyleProperty<T extends Comparable<T>> {
	private final Class<?> typeClass;
	private final String name;
	private final int hashCode;
	
	protected StyleProperty(Class<?> typeClass, String name) {
		this.typeClass = typeClass;
		this.name = name;
		
		// We do this, because:
		// 1. StyleProperties are more or less exclusively used as keys, so it's important for it to be fast
		// 2. I'm copying from Property :^)
		this.hashCode = Objects.hash(name, typeClass);
	}

	public abstract boolean isValid(T value);
	
	public boolean isCompatible(Object object) {
		// noinspection unchecked
		return this.typeClass.isInstance(object) && this.isValid((T) object);
	}
	
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("name", this.name)
			.add("typeClass", this.typeClass)
			.toString();
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof StyleProperty<?> otherProperty)) return false;
		
		return this.typeClass.equals(otherProperty.typeClass) && this.name.equals(otherProperty.name);
	}
	
	public record Value<T extends Comparable<T>>(StyleProperty<T> property, T value) {
		public Value {
			if (!property.isCompatible(value)) {
				throw new IllegalArgumentException("Value %s is not compatible with property '%s'!".formatted(value, property));
			}
		}
	}
}
