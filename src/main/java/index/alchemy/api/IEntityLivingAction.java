package index.alchemy.api;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;

@FunctionalInterface
public interface IEntityLivingAction {
	
	void apply(@Nullable EntityLivingBase src, EntityLivingBase living, float amplify);

}