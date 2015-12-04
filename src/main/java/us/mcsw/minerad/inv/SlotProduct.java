package us.mcsw.minerad.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import us.mcsw.minerad.init.ModItems;

public class SlotProduct extends Slot {

	private EntityPlayer player = null;

	public SlotProduct(IInventory inv, int i, int x, int y) {
		super(inv, i, x, y);
		if (inv instanceof InventoryPlayer) {
			player = ((InventoryPlayer) inv).player;
		}
	}

	@Override
	public boolean isItemValid(ItemStack it) {
		return false;
	}

	@Override
	public void onPickupFromSlot(EntityPlayer pl, ItemStack it) {
		this.onCrafting(it);
		super.onPickupFromSlot(pl, it);
	}

	@Override
	protected void onCrafting(ItemStack it) {
		if (player != null) {
			// achievements and such
		}
		super.onCrafting(it);
	}

}