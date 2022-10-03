package cn.nukkit.block;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.utils.BlockColor;

@PowerNukkitOnly
public class BlockDriedKelpBlock extends BlockSolid {

    @PowerNukkitOnly
    public BlockDriedKelpBlock() {
        // Does nothing
    }
    
    @Override
    public int getId() {
        return DRIED_KELP_BLOCK;
    }
    
    @Override
    public String getName() {
        return "Dried Kelp Block";
    }
    
    @Override
    public double getHardness() {
        return 0.5F;
    }
    
    @Override
    public double getResistance() {
        return 2.5;
    }
    
    @Override
    public BlockColor getColor() {
        return BlockColor.GREEN_BLOCK_COLOR;
    }
    
}
