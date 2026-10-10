package ar.laplata;

import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/** Items tematicos de Estudiantes de La Plata. */
public final class ModItems {
	public static final Item PELOTA = register("pelota", Item::new, new Item.Properties());
	public static final Item BUFANDA = register("bufanda", Item::new, new Item.Properties().stacksTo(1));
	public static final Item CAMISETA = register("camiseta", Item::new, new Item.Properties().stacksTo(1));

	private ModItems() {
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, LaPlataMod.id(name));
		Item item = factory.apply(props.setId(key));
		Registry.register(BuiltInRegistries.ITEM, key, item);
		return item;
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(tab -> {
			tab.accept(PELOTA);
			tab.accept(BUFANDA);
			tab.accept(CAMISETA);
		});
	}
}
