package cn.nukkit.block;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.utils.BlockColor;

/**
 * @author Pub4Game
 * @since 21.02.2016
 */
public class BlockSlime extends BlockSolid {

    public BlockSlime() {
        // Does nothing
    }

    @Override
    public double getHardness() {
        return 0;
    }

    @Override
    public String getName() {
        return "Slime Block";
    }

    @Override
    public int getId() {
        return SLIME_BLOCK;
    }

    @Override
    public double getResistance() {
        return 0;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.GRASS_BLOCK_COLOR;
    }

    @Since("1.4.0.0-PN")
    @PowerNukkitOnly
    @Override
    public int getLightFilter() {
        return 1;
    }
}
