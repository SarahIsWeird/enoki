package blue.endless.enoki.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

/**
 * A little Map-backed registry that isn't really part of Minecraft's Registry system. Does not carry the weight of
 * entries, RegistryKeys, holders, tags, indexing, streams, etc... just holds the data and looks it up. Can be frozen.
 * 
 * @param <TResource> the type of objects that can be registered and looked up
 */
public class MiniRegistry<TResource> {
	protected Map<Identifier, TResource> entries = new HashMap<>();
	protected boolean frozen = false;
	
	/**
	 * Looks up a previously registered or resource-loaded object from this registry.
	 * 
	 * @param id   the registry id of the object to find
	 * @return     the object if it exists, or null if no object exists with that id
	 */
	public @Nullable TResource get(Identifier id) {
		return entries.get(id);
	}
	
	/**
	 * Looks up a previously registered or resource-loaded object from this registry.
	 * 
	 * @param id   the registry id of the object to find
	 * @return     an {@link Optional} containing the object if it exists, otherwise {@link Optional#empty()}
	 */
	public Optional<TResource> getOptional(Identifier id) {
		return Optional.ofNullable(entries.get(id));
	}
	
	/**
	 * Looks up a previously registered or resource-loaded object from this registry.
	 * 
	 * @param id            the registry id of the object to find
	 * @param defaultValue  the value to provide if no object exists with that id
	 * @return              the object if it exists, otherwise {@code defaultValue}
	 */
	public TResource getOrDefault(Identifier id, TResource defaultValue) {
		return entries.getOrDefault(id, defaultValue);
	}
	
	/**
	 * Looks up a previously registered or resource-loaded object from this registry. If no object exists with the
	 * provided registry id, the supplier is invoked to create one. This new object is returned but *not* registered.
	 * 
	 * @param id
	 * @param defaultValueSupplier
	 * @return
	 */
	public TResource getOrSupply(Identifier id, Supplier<TResource> defaultValueSupplier) {
		TResource result = entries.get(id);
		if (result != null) return result;
		return defaultValueSupplier.get();
	}
	
	/**
	 * Registers an object to this registry.
	 * 
	 * @param id    the registry id this object will be known as
	 * @param entry the object to register
	 * @throws IllegalStateException if this registry is frozen.
	 */
	public void register(Identifier id, TResource entry) {
		if (frozen) throw new IllegalStateException("Registry is frozen.");
		this.entries.put(id, entry);
	}
	
	/**
	 * Removes all mappings and resets this registry to its initial state.
	 * 
	 * @throws IllegalStateException if the registry is frozen
	 */
	public void clear() {
		if (frozen) throw new IllegalStateException("Registry is frozen.");
		entries.clear();
	}
	
	/**
	 * Freezes this registry, preventing any modifications unless {@link #unfreeze()} is called.
	 */
	public void freeze() {
		frozen = true;
	}
	
	/**
	 * Unfreezes this registry, allowing registration and removal of entries. Generally unsafe to call unless you are
	 * the creator of the registry.
	 */
	public void unfreeze() {
		frozen = false;
	}
}
