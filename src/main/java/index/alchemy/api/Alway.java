package index.alchemy.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import index.alchemy.core.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class Alway {
	
	public static final int SEA_LEVEL = 62;
	
	public static final Map<Thread, Side> SIDE_MAPPING = new HashMap<Thread, Side>();
	
	public static boolean isAlchemyModLoaded() {
		return Loader.isModLoaded(Constants.MOD_ID);
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean isPlaying() {
		return Minecraft.getMinecraft().thePlayer != null;
	}
	
	public static Side getSide() {
		Side side = SIDE_MAPPING.get(Thread.currentThread());
		if (side == null)
			SIDE_MAPPING.put(Thread.currentThread(), side = FMLCommonHandler.instance().getEffectiveSide());
		return side;
	}
	
	public static boolean isServer() {
		return getSide().isServer();
	}
	
	public static boolean isClient() {
		return getSide().isClient();
	}
	
	public static Biome getCurrentBiome(EntityPlayer player) {
		return getCurrentBiome(player.worldObj, (int) player.posX, (int) player.posZ);
	}
	
	public static Biome getCurrentBiome(World world, int x, int z) {
		return world.getBiomeForCoordsBody(new BlockPos(x, 0, z));
	}
	
	public static ILocationProvider generateLocationProvider(final Entity entity, final double offsetY) {
		return new ILocationProvider() {
			@Override
			public Vec3d getLocation() {
				AxisAlignedBB aabb = entity.getEntityBoundingBox();
				return entity.getPositionVector().addVector((aabb.maxX - aabb.minX) / 2, offsetY * (aabb.maxY - aabb.minY) / 2, (aabb.maxZ - aabb.minZ) / 2);
			}
		};
	}
	
	public static ILocationProvider generateLocationProvider(BlockPos pos) {
		final Vec3d vec3d = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
		return new ILocationProvider() {
			@Override
			public Vec3d getLocation() {
				return vec3d;
			}
		};
	}
	
	public static IMaterialConsumer generateMaterialConsumer(final ItemStack material) {
		return new IMaterialConsumer() {
			@Override
			public boolean treatmentMaterial(List<ItemStack> items) {
				for (Iterator<ItemStack> iterator = items.iterator(); iterator.hasNext();) {
					ItemStack item = iterator.next();
					if (item.isItemEqualIgnoreDurability(material)) {
						if (item.stackSize >= material.stackSize)
							item.stackSize -= material.stackSize;
						if (item.stackSize == 0)
							iterator.remove();
						return true;
					}
				}
				return false;
			}
		};
	}
	
	public static IMaterialConsumer generateMaterialConsumer(final String material_str, final int size) {
		return new IMaterialConsumer() {
			@Override
			public boolean treatmentMaterial(List<ItemStack> items) {
				for (Iterator<ItemStack> iterator = items.iterator(); iterator.hasNext();) {
					ItemStack item = iterator.next();
					for (ItemStack material : OreDictionary.getOres(material_str))
						if (item.isItemEqualIgnoreDurability(item)) {
							if (item.stackSize >= size)
								item.stackSize -= size;
							if (item.stackSize == 0)
								iterator.remove();
							return true;
						}
				}
				return false;
			}
		};
	}
	
}