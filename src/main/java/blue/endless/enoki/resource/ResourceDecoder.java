package blue.endless.enoki.resource;

import java.io.IOException;
import java.util.Optional;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

/**
 * Kind of an alias for BiFunction<Identifier, Resource, Optional<T>>, but if we did that directly, it would be awful.
 * @param <TResource> The kind of resource this function decodes
 */
@FunctionalInterface
public interface ResourceDecoder<TResource> {
	/**
	 * Attempts to decode a resource. If the decoder discovers that the resource is well-formed but unsupported, Empty
	 * should be returned. If an error occurs during the decoding, IOException should be thrown.
	 * @param id       the Identifier the resource can be looked up as (without the prefix or locale)
	 * @param resource the Resource that can be used to obtain an InputStream
	 * @return the decoded resource if possible, or empty if the resource is unsupported.
	 */
	Optional<TResource> decode(Identifier id, Resource resource) throws IOException;
}