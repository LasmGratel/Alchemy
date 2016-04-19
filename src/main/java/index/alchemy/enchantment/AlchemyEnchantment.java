package index.alchemy.enchantment;

import index.alchemy.client.AlchemyResourceLocation;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.IEventHandle;
import index.alchemy.core.IPlayerTickable;
import index.alchemy.core.IRegister;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

public class AlchemyEnchantment extends Enchantment implements IRegister {
	
	public static final EntityEquipmentSlot[] 
			SLOT_ARMOR = new EntityEquipmentSlot[]{
				EntityEquipmentSlot.HEAD, 
				EntityEquipmentSlot.CHEST, 
				EntityEquipmentSlot.LEGS, 
				EntityEquipmentSlot.FEET},
			SLOT_HANDS = new EntityEquipmentSlot[]{
				EntityEquipmentSlot.MAINHAND, 
				EntityEquipmentSlot.OFFHAND};
	
	protected int max_level;
	
	@Override
	public int getMinLevel() {
		return 1;
	}

	@Override
	public int getMaxLevel() {
		return max_level;
	}

	public AlchemyEnchantment(String name, Rarity rarity, EnumEnchantmentType type, int max_level, EntityEquipmentSlot... slots) {
		super(rarity, type, slots);
		this.max_level = max_level;
		setRegistryName(name);
		register();
	}
	
	@Override
	public void register() {
		AlchemyInitHook.init_impl(this);
	}

}
