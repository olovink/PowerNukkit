package cn.nukkit.block;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;

/**
 * @author LoboMetalurgico
 * @since 2021-06-11
 */

@PowerNukkitOnly
@Since("FUTURE")
public class BlockCopperOxidizedWaxed extends BlockCopperOxidized {
    @PowerNukkitOnly
    @Since("FUTURE")
    public BlockCopperOxidizedWaxed() {
        // Does nothing
    }

    @Override
    public String getName() {
        return "Waxed Oxidized Copper";
    }

    @Override
    public int getId() {
        return WAXED_OXIDIZED_COPPER;
    }

    @Since("FUTURE")
    @PowerNukkitOnly
    @Override
    public boolean isWaxed() {
        return true;
    }
}
