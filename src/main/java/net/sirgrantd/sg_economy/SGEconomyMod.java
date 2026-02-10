package net.sirgrantd.sg_economy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.Tuple;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.sirgrantd.sg_economy.capabilities.CoinsBagCapabilities;
import net.sirgrantd.sg_economy.config.ClientConfig;
import net.sirgrantd.sg_economy.config.ServerConfig;
import net.sirgrantd.sg_economy.network.SGNetwork;
import net.sirgrantd.sg_economy.network.payload.SyncServerConfigS2C;

@Mod("sg_economy")
public class SGEconomyMod {
	public static final Logger LOGGER = LogManager.getLogger(SGEconomyMod.class);
	public static final String MOD_ID = "sg_economy";
	public static final String MG_COINS_ID = "magic_coins";

	public SGEconomyMod(IEventBus modEventBus, ModContainer modContainer) {
		NeoForge.EVENT_BUS.register(SGEconomyMod.class);

		modEventBus.addListener(SGNetwork::registerNetworking);

		SGNetwork.addNetworkMessage(
				SyncServerConfigS2C.TYPE,
				SyncServerConfigS2C.STREAM_CODEC,
				SyncServerConfigS2C::handle);

		CoinsBagCapabilities.ATTACHMENT_TYPES.register(modEventBus);

		modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.Config.SPEC,
				String.format("%s-server.toml", MOD_ID));
		modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.Config.SPEC,
				String.format("%s-client.toml", MOD_ID));
	}

	@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
	public static class ClientProxy {
		@SubscribeEvent
		public static void setupClient(FMLClientSetupEvent event) {
		}
	}

	private static final Collection<Tuple<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

	public static void queueServerWork(int tick, Runnable action) {
		workQueue.add(new Tuple<>(action, tick));
	}

	@SubscribeEvent
	public static void tick(ServerTickEvent.Post event) {
		List<Tuple<Runnable, Integer>> actions = new ArrayList<>();
		workQueue.forEach(work -> {
			work.setB(work.getB() - 1);
			if (work.getB() == 0)
				actions.add(work);
		});
		actions.forEach(e -> e.getA().run());
		workQueue.removeAll(actions);
	}
}