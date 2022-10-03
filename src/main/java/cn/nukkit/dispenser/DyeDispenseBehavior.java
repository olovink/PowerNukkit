package cn.nukkit.dispenser;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.block.*;
import cn.nukkit.item.Item;
import cn.nukkit.math.BlockFace;

@PowerNukkitOnly
public class DyeDispenseBehavior extends DefaultDispenseBehavior {

    @PowerNukkitOnly
    public DyeDispenseBehavior() {
        super();
    }

    @PowerNukkitOnly
    @Override
    public Item dispense(BlockDispenser block, BlockFace face, Item item) {
        Block target = block.getSide(face);

        if (item.isFertilizer()) {
            if (target instanceof BlockCrops || target instanceof BlockSapling || target instanceof BlockTallGrass
                    || target instanceof BlockDoublePlant || target instanceof BlockMushroom) {
                target.onActivate(item);

            } else {
                this.success = false;
            }

            return null;
        }

        return super.dispense(block, face, item);
    }
}
