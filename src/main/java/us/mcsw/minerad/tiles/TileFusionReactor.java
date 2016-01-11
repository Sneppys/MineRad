package us.mcsw.minerad.tiles;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import us.mcsw.core.TileMultiblock;
import us.mcsw.core.util.ItemUtil;
import us.mcsw.minerad.MineRad;
import us.mcsw.minerad.init.ModItems;
import us.mcsw.minerad.items.ItemCoolantCore;
import us.mcsw.minerad.items.ItemEmptyCore;
import us.mcsw.minerad.items.ItemFusionCore;
import us.mcsw.minerad.recipes.FusionRecipes;
import us.mcsw.minerad.util.RadUtil;

public class TileFusionReactor extends TileMultiblock {

	int coreDamageCount = 0, oreProgressCount = 0, coolantDamageCount = 0, passiveCoolCount = 0;

	public Item source = null;
	public int maxNeeded = 0;

	public int heat = 0;

	public TileFusionReactor() {
		super(5);
	}

	@Override
	public void onUpdate() {
		if (hasCore() && !isCoreDepleted() && hasSource()) {
			RadUtil.setPowerAndReach(worldObj, xCoord, yCoord + 1, zCoord, 5, 1);
			if (++coreDamageCount > 7) {
				damageCore(1);
				coreDamageCount = 0;
			}
			incrProgress(1);
		} else {
			RadUtil.setPowerAndReach(worldObj, xCoord, yCoord + 1, zCoord, 0, 0);
			if (++passiveCoolCount > 5) {
				if (heat > 0) {
					heat--;
				}
			}
		}
		if (hasCoolant() && !isCoolantDepleted()) {
			if (hasCore() && !isCoreDepleted() && hasSource()) {
				if (++coolantDamageCount > 11) {
					damageCoolant(1);
					coolantDamageCount = 0;
				}
			}
			if (heat > 0) {
				heat -= 8;
				if (heat < 0)
					heat = 0;
				damageCoolant(1);
			}
		} else if (hasCore() && !isCoreDepleted()) {
			heat += 1;
		}
		if (!worldObj.isRemote) {
			if (heat > DANGER_HEAT_LEVEL) {
				if (Math.random() > 0.995) {
					this.worldObj.createExplosion(null, xCoord, yCoord + 1, zCoord, 20.0f, true);
				}
			}
		}
	}

	@Override
	public String getInventoryName() {
		return MineRad.MODID + ":fusionReactor";
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	public static final int DANGER_HEAT_LEVEL = 1000;

	public String getHeatLevel() {
		if (heat <= 20) {
			return "cool";
		} else if (heat <= 200) {
			return "warm";
		} else if (heat <= 500) {
			return "hot";
		} else if (heat <= DANGER_HEAT_LEVEL) {
			return "extreme";
		} else {
			return "danger";
		}
	}

	int updateCount = 0;

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (updateCount-- < 0) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			updateCount = 20;
		}
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot <= 1) {
			return FusionRecipes.hasRecipe(stack);
		} else if (slot == 3) {
			return stack.getItem() instanceof ItemFusionCore;
		} else if (slot == 4) {
			return stack.getItem() instanceof ItemCoolantCore;
		}
		return false;
	}

	@Override
	public boolean checkMultiblockForm() {
		int i = 0;
		for (int x = xCoord - 1; x <= xCoord + 1; x++)
			for (int y = yCoord; y <= yCoord + 2; y++)
				for (int z = zCoord - 1; z <= zCoord + 1; z++) {
					TileEntity tile = worldObj.getTileEntity(x, y, z);
					if (tile != null && tile instanceof TileFusionReactor) {
						TileFusionReactor tf = (TileFusionReactor) tile;
						if (this.isMaster()) {
							if (tf.hasMaster())
								i++;
						} else if (!tf.hasMaster())
							i++;
					}
				}

		return i >= 26 && worldObj.isAirBlock(xCoord, yCoord + 1, zCoord);
	}

	@Override
	public void setupStructure() {
		for (int x = xCoord - 1; x <= xCoord + 1; x++)
			for (int y = yCoord; y <= yCoord + 2; y++)
				for (int z = zCoord - 1; z <= zCoord + 1; z++) {
					TileEntity tile = worldObj.getTileEntity(x, y, z);
					boolean master = (x == xCoord && y == yCoord && z == zCoord);
					if (tile != null && tile instanceof TileFusionReactor) {
						TileFusionReactor tf = (TileFusionReactor) tile;
						tf.setMasterCoords(xCoord, yCoord, zCoord);
						tf.setIsMaster(master);
						tf.setHasMaster(true);
						tf.markDirty();
					}
				}
	}

	@Override
	public void resetStructure() {
		for (int x = xCoord - 1; x <= xCoord + 1; x++)
			for (int y = yCoord; y <= yCoord + 2; y++)
				for (int z = zCoord - 1; z <= zCoord + 1; z++) {
					TileEntity tile = worldObj.getTileEntity(x, y, z);
					if (tile != null && tile instanceof TileMultiblock) {
						((TileMultiblock) tile).reset();
					}
				}
	}

	public boolean hasCore() {
		return getStackInSlot(3) != null && getStackInSlot(3).stackSize > 0
				&& !(getStackInSlot(3).getItem() instanceof ItemEmptyCore);
	}

	public boolean hasSource() {
		return getStackInSlot(0) != null && getStackInSlot(0).stackSize > 0 && getStackInSlot(1) != null
				&& getStackInSlot(1).stackSize > 0
				&& FusionRecipes.getRecipeFor(getStackInSlot(0), getStackInSlot(1)) != null;
	}

	public boolean hasCoolant() {
		return getStackInSlot(4) != null && getStackInSlot(4).stackSize > 0;
	}

	public boolean isCoreDepleted() {
		if (!hasCore()) {
			return true;
		}
		return getStackInSlot(3).getItemDamage() > getStackInSlot(3).getMaxDamage();
	}

	public boolean isSourceCompleted() {
		if (!hasSource()) {
			return false;
		}
		return ItemUtil.getInteger("FusionProgress", getStackInSlot(0)) > ItemUtil.getInteger("FusionMaximum",
				getStackInSlot(0))
				&& ItemUtil.getInteger("FusionProgress", getStackInSlot(1)) > ItemUtil.getInteger("FusionMaximum",
						getStackInSlot(1));
	}

	public boolean isCoolantDepleted() {
		if (!hasCoolant()) {
			return true;
		}
		return getStackInSlot(4).getItemDamage() > getStackInSlot(4).getMaxDamage();
	}

	public void damageCore(int amount) {
		if (!isCoreDepleted()) {
			getStackInSlot(3).setItemDamage(getStackInSlot(3).getItemDamage() + amount);
		}
		if (isCoreDepleted()) {
			setInventorySlotContents(3, new ItemStack(ModItems.emptyCore));
		}
	}

	public void incrProgress(int amount) {
		if (hasSource()) {
			int ticks = FusionRecipes.getRecipeFor(getStackInSlot(0), getStackInSlot(1)).getTicks();
			ItemUtil.setInteger("FusionMaximum", getStackInSlot(0), ticks);
			ItemUtil.setInteger("FusionMaximum", getStackInSlot(1), ticks);
			if (!isSourceCompleted()) {
				if (ItemUtil.getInteger("FusionProgress", getStackInSlot(0)) < ItemUtil.getInteger("FusionMaximum",
						getStackInSlot(0))) {
					ItemUtil.setInteger("FusionProgress", getStackInSlot(0),
							ItemUtil.getInteger("FusionProgress", getStackInSlot(0)) + amount);
				}
				if (ItemUtil.getInteger("FusionProgress", getStackInSlot(1)) < ItemUtil.getInteger("FusionMaximum",
						getStackInSlot(1))) {
					ItemUtil.setInteger("FusionProgress", getStackInSlot(1),
							ItemUtil.getInteger("FusionProgress", getStackInSlot(1)) + amount);
				}
			}
		}
		if (isSourceCompleted() && getStackInSlot(2) == null) {
			ItemStack result = FusionRecipes.getRecipeFor(getStackInSlot(0), getStackInSlot(1)).getResult();
			setInventorySlotContents(2, result.copy());
			setInventorySlotContents(0, null);
			setInventorySlotContents(1, null);
		}
	}

	public void damageCoolant(int amount) {
		if (!isCoolantDepleted()) {
			getStackInSlot(4).setItemDamage(getStackInSlot(4).getItemDamage() + amount);
			if (isCoolantDepleted()) {
				setInventorySlotContents(4, new ItemStack(ModItems.emptyCore));
			}
		}
	}

	@Override
	public void masterWriteToNBT(NBTTagCompound tag) {
	}

	@Override
	public void masterWriteSyncable(NBTTagCompound data) {
		data.setInteger("heat", heat);
	}

	@Override
	public void masterReadFromNBT(NBTTagCompound tag) {
	}

	@Override
	public void masterReadSyncable(NBTTagCompound data) {
		heat = data.getInteger("heat");
	}
}
