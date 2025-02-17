package com.theokapi;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OutsideLunches implements ModInitializer {
	public static final String MOD_ID = "outside-lunches";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final RegistryKey<DamageType> GOING_INSIDE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID, "going_inside"));
	public static final TagKey<Block> VALID_ABOVE_PLAYER = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "valid_above_player"));

	void hudLayerRegistrationCallback() {
		HudLayerRegistrationCallback.EVENT.register(layeredDrawerWrapper -> {
			IdentifiedLayer layer = new IdentifiedLayer() {
				@Override
				public Identifier id() {
					return null;
				}

				@Override
				public void render(DrawContext context, RenderTickCounter tickCounter) {
					MinecraftClient client = MinecraftClient.getInstance();
					ClientWorld world = client.world;
                    assert world != null;
                    long time = world.getTimeOfDay();
					if (time > 6199 && time < 6701) {
						context.drawItem(new ItemStack(Items.GRASS_BLOCK), 2, 2);
						context.drawText(client.textRenderer, "Outside Lunch", 20, 6, 0xFFFFFFFF, false);
					} else if (time > 5999 && time < 6200) {
						context.drawItem(new ItemStack(Items.BREAD), 2, 2);
						context.drawText(client.textRenderer, "Lunch", 20, 6, 0xFFFFFFFF, false);
					}
				}
			};
			layeredDrawerWrapper.addLayer(layer);
		});
	}

	void serverTickEvents() {
		ServerTickEvents.END_WORLD_TICK.register(serverWorld -> {
			DamageSource damageSourceGoingInside = new DamageSource(
					serverWorld.getRegistryManager()
							.getOrThrow(RegistryKeys.DAMAGE_TYPE)
							.getEntry(GOING_INSIDE.getValue()).get()
			);
			long time = serverWorld.getTimeOfDay();
			List<ServerPlayerEntity> players = serverWorld.getPlayers();
			List<Integer> bellTimes = List.of(6000, 6200, 6700);
			if (bellTimes.contains(Math.toIntExact(time))) {
                for (ServerPlayerEntity player : players) {
                    serverWorld.playSound(
							null,
							player.getBlockPos(),
							SoundEvents.BLOCK_BELL_USE,
							SoundCategory.PLAYERS,
							3f,
							1f);
                }
			}
			if (time > 6199 && time < 6701) {
				for (ServerPlayerEntity player : players) {
					for (int i = player.getBlockY()+1; i < 512; i++) {
						BlockState block = serverWorld.getBlockState(new BlockPos(player.getBlockX(), i, player.getBlockZ()));
						if (!block.isIn(VALID_ABOVE_PLAYER) && player.getWorld().getDimension().bedWorks()) {
							LOGGER.info(String.valueOf(serverWorld.getBlockState(new BlockPos(player.getBlockX(), i, player.getBlockZ()))));
							player.damage(serverWorld, damageSourceGoingInside, 6f);
							break;
						}
					}
				}
			}
		});
	}

	@Override
	public void onInitialize() {
		serverTickEvents();
		hudLayerRegistrationCallback();
	}
}