package cn.nukkit.blockentity;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author joserobjr
 * @since 2021-09-26
 */
@PowerNukkitOnly
@Since("FUTURE")
public class BlockEntityCustomDataStorage extends BlockEntity {

    @PowerNukkitOnly
    @Since("FUTURE")
    public BlockEntityCustomDataStorage(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public boolean isBlockEntityValid() {
        return !namedTag.getCompound(BlockEntity.CUSTOM_STORAGE).getCompound(BlockEntity.CUSTOM_STORAGE).isEmpty();
    }
}
