package blue.endless.enoki.utils.resources;

import net.minecraft.util.Identifier;

import java.util.Map;

public record AsyncResource<R>(Identifier id, R resource) {
	public static <R> AsyncResource<R> of(Map.Entry<Identifier, R> entry) {
		return new AsyncResource<>(entry.getKey(), entry.getValue());
	}
	
	public AsyncResource<R> withId(Identifier id) {
		return new AsyncResource<>(id, this.resource);
	}
	
	public <T> AsyncResource<T> withResource(T resource) {
		return new AsyncResource<>(this.id, resource);
	}
}
