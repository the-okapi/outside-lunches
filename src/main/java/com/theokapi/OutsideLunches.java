package com.theokapi;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class OutsideLunches implements ModInitializer {
	public static final String MOD_ID = "outside-lunches";
	public static final TagKey<Block> VALID_ABOVE_PLAYER = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "valid_above_player"));

	void serverTickEvents() {
		ServerTickEvents.END_WORLD_TICK.register(serverWorld -> {
			long time = serverWorld.getTimeOfDay();
			List<ServerPlayerEntity> players = serverWorld.getPlayers();
			List<Integer> bellTimes = List.of(6000, 6200, 6700);
			if (bellTimes.contains(Math.toIntExact(time))) {
                for (ServerPlayerEntity player : players) {
					if (time == 6000) player.sendMessage(Text.literal("Outside Lunch will begin soon!"), true);
					else if (time == 6200) player.sendMessage(Text.literal("Outside Lunch has begun!"), true);
					else player.sendMessage(Text.literal("Outside Lunch has ended!"), true);
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
							player.kill(serverWorld);
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
		//hudLayerRegistrationCallback();
	}
}