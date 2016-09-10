package index.alchemy.block;

import java.util.Random;

import index.alchemy.api.IRegister;
import index.alchemy.api.IResourceLocation;
import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.AlchemyResourceLocation;
import index.alchemy.item.AlchemyItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

public class AlchemyBlock extends Block implements IResourceLocation, IRegister {
	
	protected static final Random RANDOM = new Random();
	
	protected ResourceLocation icon_name;
	
	@Override
	public ResourceLocation getResourceLocation() {
		return icon_name == null ? getRegistryName() : icon_name;
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}
	
	public AlchemyBlock(String name, Material material) {
		this(name, material, null);
	}
	
	public AlchemyBlock(String name, Material material, String icon) {
		super(material);
		if (icon != null)
			icon_name = new AlchemyResourceLocation(icon);
		if (hasCreativeTab())
			setCreativeTab(AlchemyItem.CREATIVE_TABS);
		setUnlocalizedName(name);
		setRegistryName(name);
		register();
	}
	
	public boolean hasCreativeTab() {
		return true;
	}
	
	@Override
	public void register() {
		AlchemyInitHook.init_impl(this);
	}

	
}
