package blue.endless.enoki.markdown.attributes;

import blue.endless.enoki.gui.Size;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record DocImageAttributes(@Nullable Identifier imageId, @NotNull Size size, FillType fillType, boolean isInline) {
	public enum FillType {
		NONE,
		WIDTH,
		HEIGHT,
		BOTH,
		;
		
		@Nullable
		public static FillType of(String value) {
			try {
				return valueOf(value.toUpperCase());
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private Identifier imageId = null;
		private int width = -1;
		private int height = -1;
		private FillType fillType = FillType.NONE;
		private boolean isInline = false;
		
		private Builder() {}
		
		public DocImageAttributes build() {
			return new DocImageAttributes(this.imageId, new Size(width, height), fillType, isInline);
		}
		
		public Builder imageId(@Nullable Identifier imageId) {
			this.imageId = imageId;
			return this;
		}
		
		public Builder width(int width) {
			this.width = width;
			return this;
		}
		
		public Builder height(int height) {
			this.height = height;
			return this;
		}
		
		public Builder fillType(FillType fillType) {
			this.fillType = fillType;
			return this;
		}
		
		public Builder isInline(boolean isInline) {
			this.isInline = isInline;
			return this;
		}
	}
}
