package ar.laplata;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** Bloques tematicos de Estudiantes de La Plata (rojo y blanco). */
public final class ModBlocks {
	public static final Block ROJO_PINCHA = register("rojo_pincha", MapColor.COLOR_RED);
	public static final Block BLANCO_PINCHA = register("blanco_pincha", MapColor.SNOW);
	public static final Block RAYAS_PINCHA = register("rayas_pincha", MapColor.COLOR_RED);

	private ModBlocks() {
	}

	private static Block register(String name, MapColor color) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, LaPlataMod.id(name));
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, LaPlataMod.id(name));

		Block block = new Block(BlockBehaviour.Properties.of()
			.mapColor(color)
			.strength(1.5f, 6.0f)
			.sound(SoundType.STONE)
			.setId(blockKey));
		Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

		BlockItem item = new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(itemKey));
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);

		return block;
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(tab -> {
			tab.accept(ROJO_PINCHA.asItem());
			tab.accept(BLANCO_PINCHA.asItem());
			tab.accept(RAYAS_PINCHA.asItem());
		});
	}
}
