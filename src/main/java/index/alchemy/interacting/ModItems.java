package index.alchemy.interacting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import javax.annotation.Nullable;

import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Source;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.util.FinalFieldSetter;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Omega
@Init(state = ModState.PREINITIALIZED, index = -1)
public class ModItems {
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface ItemTransform {
		
		public String value() default "";

	}
	
	//  Biomes O' Plenty

	@Deprecated
	@Source("biomesoplenty.api.item.BOPItems")
	public static final Item
			bop$gem = null;
	
	@ItemTransform("bop$gem")
	public static final ItemStack
			bop$gem_amethyst = null,							//  末影紫晶 ---- 空间
			bop$gem_ruby = null,								//  红宝石 ---- 生命
			bop$gem_peridot = null,								//  橄榄石 ---- 坚韧
			bop$gem_topaz = null,								//  黄玉 ---- 守护
			bop$gem_tanzanite = null,							//  坦桑石 ---- 祝福
			bop$gem_malachite = null,							//  孔雀石 ---- 生灵
			bop$gem_sapphire = null,							//  蓝宝石  ---- 净化
			bop$gem_amber = null;								//  琥珀  ---- 时间
	
	@Deprecated
	@Source("biomesoplenty.api.block.BOPBlocks")
	public static final Item
			bop$mushroom = null,
			bop$flower_0 = null,
			bop$flower_1 = null;
	
	@ItemTransform( "bop$mushroom")
	public static final ItemStack
			bop$mushroom_toadstool = null,						//  毒菌
			bop$mushroom_portobello = null,						//  双胞蘑菇
			bop$mushroom_blue_milk_cap = null,					//  蓝牛奶伞菌
			bop$mushroom_glowshroom = null,						//  夜光菇
			bop$mushroom_flat_mushroom = null,					//  平菇
			bop$mushroom_shadow_shroom = null;					//  影菇
	
	@ItemTransform( "bop$flower_0")
	public static final ItemStack
			bop$flower_clover = null,							//  苜蓿 ---- 坚韧
			bop$flower_swampflower = null,						//  沼泽花 ---- 黑暗
			bop$flower_deathbloom = null,						//  死亡花 ---- 堕化
			bop$flower_glowflower = null,						//  闪光花 ---- 光明
			bop$flower_blue_hydrangea = null,					//  蓝绣球花 ---- 亲和
			bop$flower_orange_cosmos = null,					//  黄波斯菊 ---- 奇迹
			bop$flower_pink_daffodil = null,					//  粉水仙 ---- 净化
			bop$flower_wildflower = null,						//  野花 ---- 时间
			bop$flower_violet = null,							//  紫罗兰  ---- 牺牲
			bop$flower_white_anemone = null,					//  银莲花 ---- 包容
			bop$flower_enderlotus = null,						//  末影莲花 ----空间
			bop$flower_bromeliad = null,						//  凤梨 --- 破瘴
			bop$flower_wilted_lily = null,						//  凋零百合 ---- 绝望
			bop$flower_pink_hibiscus = null,					//  粉木槿 ---- 真理
			bop$flower_lily_of_the_valley = null,				//  谷百合 ---- 祝福
			bop$flower_burning_blossom = null;					//  火焰花 ---- 狂暴
	
	@ItemTransform("bop$flower_1")
	public static final ItemStack
			bop$flower_lavender = null,							//  熏衣草 ---- 和平
			bop$flower_goldenrod = null,						//  秋麒麟草 ---- 永恒
			bop$flower_bluebells = null,						//  蓝铃花 ---- 活性
			bop$flower_miners_delight = null,					//  乐矿花 ---- 幸运
			bop$flower_icy_iris = null,							//  冰虹膜花 ----神性
			bop$flower_rose = null;								//  玫瑰 ---- 爱情
	
	//  Botania
	
	@Nullable
	@Source("vazkii.botania.common.item.ModItems")
	public static final Item
			botania$waterRod = null;
	
	@Nullable
	@Deprecated
	@Source("vazkii.botania.common.block.ModBlocks")
	public static final Item
			botania$livingwood = null;
	
	@Nullable
	@ItemTransform("botania$livingwood")
	public static final ItemStack
			botania$livingwood_log = null,
			botania$livingwood_plank = null,
			botania$livingwood_plank_mossy = null,
			botania$livingwood_plank_framed = null,
			botania$livingwood_plank_framed_pattern = null,
			botania$livingwood_plank_glimmering = null;
	
	public static void init() throws Exception {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		String last = null;
		for (Field field : ModItems.class.getFields()) {
			Source source = field.getAnnotation(Source.class);
			if (source != null) {
				Class<?> clazz = Tool.forName(source.value(), true);
				if (clazz != null) {
					Object obj = clazz.getField(field.getName().replaceAll(".*\\$", "")).get(null);
					FinalFieldSetter.instance().setStatic(field, obj instanceof Block ? Item.getItemFromBlock((Block) obj) : obj);
				}
			}
		}
		
		int index = 0;
		Item item = null;
		for (Field field : ModItems.class.getFields()) {
			ItemTransform transform = field.getAnnotation(ItemTransform.class);
			if (transform != null) {
				if (!transform.value().equals(last)) {
					last = transform.value();
					item = (Item) ModItems.class.getField(last).get(null);
					index = 0;
				}
				if (item != null)
					FinalFieldSetter.instance().setStatic(field, new ItemStack(item, 1, index++));
			}
		}
		
		for (Field field : ModItems.class.getFields()) {
			Object obj = field.get(null);
			AlchemyModLoader.info(field.getType(), field.getName() + " - " + (obj instanceof Item ? 
					((Item) obj).getUnlocalizedName() : obj instanceof ItemStack ? ((ItemStack) obj).getDisplayName() : ""));
		}
	}
	
}