package blue.endless.enoki.markdown.attributes;

import blue.endless.enoki.gui.Size;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record DocImageAttributes(@Nullable Identifier imageId, @NotNull Size size) {
}
