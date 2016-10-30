package index.alchemy.api;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface IAlchemyRecipe {
	
	ResourceLocation getAlchemyName();
	
	int getAlchemyTime();
	
	int getAlchemyColor();
	
	ItemStack getAlchemyResult();
	
	List<IMaterialConsumer> getAlchemyMaterials();

}