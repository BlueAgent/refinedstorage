package refinedstorage.container.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SlotFluidFilter extends SlotSpecimen {
    private boolean server;

    public SlotFluidFilter(boolean server, IItemHandler handler, int id, int x, int y) {
        super(handler, id, x, y);

        this.server = server;
    }

    @Override
    public ItemStack getStack() {
        return server ? super.getStack() : null;
    }
}