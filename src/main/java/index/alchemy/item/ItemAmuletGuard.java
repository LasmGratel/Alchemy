package index.alchemy.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemAmulet;

public class ItemAmuletGuard extends AlchemyItemAmulet {
	
	public static final int RECOVERY_INTERVAL = 20 * 2, RECOVERY_CD = 20 * 5;
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (living.ticksExisted % RECOVERY_INTERVAL == 0 && living.getLastAttackerTime() - living.ticksExisted > RECOVERY_CD) {
			PotionEffect effect = living.getActivePotionEffect(MobEffects.ABSORPTION);
			int amplifier = effect == null ? 0 : effect.getAmplifier() + 1;
			if (amplifier < 5)
				living.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 2, amplifier));
		}
	}

	public ItemAmuletGuard() {
		super("amulet_guard", 0xFFCC00);
	}

}