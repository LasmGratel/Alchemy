package index.alchemy.item;

import index.alchemy.api.Alway;
import index.alchemy.api.IEventHandle;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyEventSystem.EventType;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemBelt;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemBeltTough extends AlchemyItemBelt implements IEventHandle {
	
	public static final int RECOVERY_INTERVAL = 20 * 6;
	public static final float BALANCE_COEFFICIENT = 0.3F;
	
	public static final AttributeModifier KNOCKBACK_RESISTANCE =  new AttributeModifier("belt_tough_bonus", 1D, 0);
	
	@Override
	public void onEquipped(ItemStack item, EntityLivingBase living) {
		living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).applyModifier(KNOCKBACK_RESISTANCE);
	}

	@Override
	public void onUnequipped(ItemStack item, EntityLivingBase living) {
		living.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).removeModifier(KNOCKBACK_RESISTANCE);
	}
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (Alway.isServer() && living.ticksExisted % RECOVERY_INTERVAL == 0)
			living.heal(1F);
	}
	
	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingHurt(LivingHurtEvent event) {
		EntityLivingBase living = event.getEntityLiving();
		if (isEquipmented(living))
			event.setAmount(event.getAmount() * (1 - (1 - living.getHealth() / living.getMaxHealth()) * BALANCE_COEFFICIENT));
	}

	public ItemBeltTough() {
		super("belt_tough", 0x00CC00);
	}

}