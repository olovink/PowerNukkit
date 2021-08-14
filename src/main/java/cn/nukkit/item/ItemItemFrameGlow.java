package cn.nukkit.item;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;

/**
 * @author LoboMetalurgico
 * @since 2021-08-13
 */

public class ItemItemFrameGlow extends StringItem  {
    @PowerNukkitOnly
    @Since("FUTURE")
    public ItemItemFrameGlow() {
        super(MinecraftItemID.GLOW_FRAME.getNamespacedId(), "Glow Item Frame");
        this.block = Block.get(BlockID.GLOW_FRAME);
    }
}
