package blue.endless.enoki.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;


import net.minecraft.util.Identifier;

public class LocalizedRegistry<T> {
	protected final String locale;
	protected Map<Identifier, T> entries = new HashMap<>();
	protected boolean frozen = false;
	
	public LocalizedRegistry(String locale) {
		this.locale = locale;
	}
	
	public String locale() {
		return locale;
	}
	
	public void register(Identifier id, T entry) {
		if (frozen) throw new IllegalStateException("Registry is frozen.");
		this.entries.put(id, entry);
	}
	
	public @Nullable T get(Identifier id) {
		return entries.get(id);
	}
	
	public Optional<T> getOptional(Identifier id) {
		return Optional.ofNullable(entries.get(id));
	}
	
	public void clear() {
		if (frozen) throw new IllegalStateException("Registry is frozen.");
	}
	
	public void freeze() {
		frozen = true;
	}
}
