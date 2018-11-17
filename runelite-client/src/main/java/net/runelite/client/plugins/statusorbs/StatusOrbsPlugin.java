/*
 * Copyright (c) 2018 TheStonedTurtle <https://github.com/TheStonedTurtle>
 * Regen Meter Contributors:
 * Copyright (c) 2018 Abex
 * Copyright (c) 2018, Zimaya <https://github.com/Zimaya>
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * Run Energy Contributors:
 * Copyright (c) 2018, Sean Dewar <https://github.com/seandewar>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.statusorbs;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import static net.runelite.api.ItemID.*;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.StringUtils;

@PluginDescriptor(
	name = "Status Orbs",
	description = "Settings for the Minimap orbs (HP, Run, & Special Attack)",
	tags = {"hp", "heart", "minimap", "run", "energy"}
)
@Slf4j
public class StatusOrbsPlugin extends Plugin
{
	private static final ImmutableSet<Integer> ALL_GRACEFUL_HOODS = ImmutableSet.of(
		GRACEFUL_HOOD_11851, GRACEFUL_HOOD_13579, GRACEFUL_HOOD_13580, GRACEFUL_HOOD_13591, GRACEFUL_HOOD_13592,
		GRACEFUL_HOOD_13603, GRACEFUL_HOOD_13604, GRACEFUL_HOOD_13615, GRACEFUL_HOOD_13616, GRACEFUL_HOOD_13627,
		GRACEFUL_HOOD_13628, GRACEFUL_HOOD_13667, GRACEFUL_HOOD_13668, GRACEFUL_HOOD_21061, GRACEFUL_HOOD_21063
	);
	private static final ImmutableSet<Integer> ALL_GRACEFUL_TOPS = ImmutableSet.of(
		GRACEFUL_TOP_11855, GRACEFUL_TOP_13583, GRACEFUL_TOP_13584, GRACEFUL_TOP_13595, GRACEFUL_TOP_13596,
		GRACEFUL_TOP_13607, GRACEFUL_TOP_13608, GRACEFUL_TOP_13619, GRACEFUL_TOP_13620, GRACEFUL_TOP_13631,
		GRACEFUL_TOP_13632, GRACEFUL_TOP_13671, GRACEFUL_TOP_13672, GRACEFUL_TOP_21067, GRACEFUL_TOP_21069
	);
	private static final ImmutableSet<Integer> ALL_GRACEFUL_LEGS = ImmutableSet.of(
		GRACEFUL_LEGS_11857, GRACEFUL_LEGS_13585, GRACEFUL_LEGS_13586, GRACEFUL_LEGS_13597, GRACEFUL_LEGS_13598,
		GRACEFUL_LEGS_13609, GRACEFUL_LEGS_13610, GRACEFUL_LEGS_13621, GRACEFUL_LEGS_13622, GRACEFUL_LEGS_13633,
		GRACEFUL_LEGS_13634, GRACEFUL_LEGS_13673, GRACEFUL_LEGS_13674, GRACEFUL_LEGS_21070, GRACEFUL_LEGS_21072
	);
	private static final ImmutableSet<Integer> ALL_GRACEFUL_GLOVES = ImmutableSet.of(
		GRACEFUL_GLOVES_11859, GRACEFUL_GLOVES_13587, GRACEFUL_GLOVES_13588, GRACEFUL_GLOVES_13599, GRACEFUL_GLOVES_13600,
		GRACEFUL_GLOVES_13611, GRACEFUL_GLOVES_13612, GRACEFUL_GLOVES_13623, GRACEFUL_GLOVES_13624, GRACEFUL_GLOVES_13635,
		GRACEFUL_GLOVES_13636, GRACEFUL_GLOVES_13675, GRACEFUL_GLOVES_13676, GRACEFUL_GLOVES_21073, GRACEFUL_GLOVES_21075
	);
	private static final ImmutableSet<Integer> ALL_GRACEFUL_BOOTS = ImmutableSet.of(
		GRACEFUL_BOOTS_11861, GRACEFUL_BOOTS_13589, GRACEFUL_BOOTS_13590, GRACEFUL_BOOTS_13601, GRACEFUL_BOOTS_13602,
		GRACEFUL_BOOTS_13613, GRACEFUL_BOOTS_13614, GRACEFUL_BOOTS_13625, GRACEFUL_BOOTS_13626, GRACEFUL_BOOTS_13637,
		GRACEFUL_BOOTS_13638, GRACEFUL_BOOTS_13677, GRACEFUL_BOOTS_13678, GRACEFUL_BOOTS_21076, GRACEFUL_BOOTS_21078
	);
	// Agility skill capes and the non-cosmetic Max capes also count for the Graceful set effect
	private static final ImmutableSet<Integer> ALL_GRACEFUL_CAPES = ImmutableSet.of(
		GRACEFUL_CAPE_11853, GRACEFUL_CAPE_13581, GRACEFUL_CAPE_13582, GRACEFUL_CAPE_13593, GRACEFUL_CAPE_13594,
		GRACEFUL_CAPE_13605, GRACEFUL_CAPE_13606, GRACEFUL_CAPE_13617, GRACEFUL_CAPE_13618, GRACEFUL_CAPE_13629,
		GRACEFUL_CAPE_13630, GRACEFUL_CAPE_13669, GRACEFUL_CAPE_13670, GRACEFUL_CAPE_21064, GRACEFUL_CAPE_21066,
		AGILITY_CAPE, AGILITY_CAPET, MAX_CAPE
	);


	private static final BufferedImage HEART_NORMAL;
	private static final BufferedImage HEART_DISEASE;
	private static final BufferedImage HEART_POISON;
	private static final BufferedImage HEART_VENOM;
	static
	{
		HEART_NORMAL = ImageUtil.resizeCanvas(ImageUtil.getResourceStreamFromClass(StatusOrbsPlugin.class, "1067-NORMAL.png"), 26, 26);
		HEART_DISEASE = ImageUtil.resizeCanvas(ImageUtil.getResourceStreamFromClass(StatusOrbsPlugin.class, "1067-DISEASE.png"), 26, 26);
		HEART_POISON = ImageUtil.resizeCanvas(ImageUtil.getResourceStreamFromClass(StatusOrbsPlugin.class, "1067-POISON.png"), 26, 26);
		HEART_VENOM = ImageUtil.resizeCanvas(ImageUtil.getResourceStreamFromClass(StatusOrbsPlugin.class, "1067-VENOM.png"), 26, 26);
	}

	private static final int SPEC_REGEN_TICKS = 50;
	private static final int NORMAL_HP_REGEN_TICKS = 100;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private StatusOrbsConfig config;;

	@Inject
	private StatusOrbsOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Getter
	private double hitpointsPercentage;

	@Getter
	private double specialPercentage;

	// HeartDisplay
	private BufferedImage currentHeart;

	// RegenMeter
	private int ticksSinceSpecRegen;
	private int ticksSinceHPRegen;
	private boolean wasRapidHeal;

	// Run Energy
	private boolean localPlayerRunningToDestination;
	private WorldPoint prevLocalPlayerLocation;

	@Provides
	StatusOrbsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StatusOrbsConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		migrateConfigs();
		overlayManager.add(overlay);
		if (config.dynamicHpHeart())
		{
			clientThread.invoke(this::checkHealthIcon);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		if (config.dynamicHpHeart())
		{
			clientThread.invoke(this::resetHealthIcon);
		}

		localPlayerRunningToDestination = false;
		prevLocalPlayerLocation = null;
		if (config.replaceOrbText())
		{
			resetRunOrbText();
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged c)
	{
		if (c.getGroup().equals("statusorbs"))
		{
			switch (c.getKey())
			{
				case "dynamicHpHeart":
					if (config.dynamicHpHeart())
					{
						checkHealthIcon();
					}
					else
					{
						resetHealthIcon();
					}
					break;
				case "replaceOrbText":
					if (!config.replaceOrbText())
					{
						resetRunOrbText();
					}
					break;
				default:
					return;
			}
		}
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged e)
	{
		// Dynamic HP Heart
		checkHealthIcon();

		// RegenMeter
		boolean isRapidHeal = client.isPrayerActive(Prayer.RAPID_HEAL);
		if (wasRapidHeal != isRapidHeal)
		{
			ticksSinceHPRegen = 0;
		}
		wasRapidHeal = isRapidHeal;
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged ev)
	{
		if (ev.getGameState() == GameState.HOPPING || ev.getGameState() == GameState.LOGIN_SCREEN)
		{
			ticksSinceHPRegen = -2; // For some reason this makes this accurate
			ticksSinceSpecRegen = 0;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) == 1000)
		{
			// The recharge doesn't tick when at 100%
			ticksSinceSpecRegen = 0;
		}
		else
		{
			ticksSinceSpecRegen = (ticksSinceSpecRegen + 1) % SPEC_REGEN_TICKS;
		}
		specialPercentage = ticksSinceSpecRegen / (double) SPEC_REGEN_TICKS;


		int ticksPerHPRegen = NORMAL_HP_REGEN_TICKS;
		if (client.isPrayerActive(Prayer.RAPID_HEAL))
		{
			ticksPerHPRegen /= 2;
		}

		ticksSinceHPRegen = (ticksSinceHPRegen + 1) % ticksPerHPRegen;
		hitpointsPercentage = ticksSinceHPRegen / (double) ticksPerHPRegen;

		int currentHP = client.getBoostedSkillLevel(Skill.HITPOINTS);
		int maxHP = client.getRealSkillLevel(Skill.HITPOINTS);
		if (currentHP == maxHP && !config.showWhenNoChange())
		{
			hitpointsPercentage = 0;
		}
		else if (currentHP > maxHP)
		{
			// Show it going down
			hitpointsPercentage = 1 - hitpointsPercentage;
		}

		// Run Energy
		localPlayerRunningToDestination =
			prevLocalPlayerLocation != null &&
				client.getLocalDestinationLocation() != null &&
				prevLocalPlayerLocation.distanceTo(client.getLocalPlayer().getWorldLocation()) > 1;

		prevLocalPlayerLocation = client.getLocalPlayer().getWorldLocation();

		if (config.replaceOrbText())
		{
			setRunOrbText(getEstimatedRunTimeRemaining(true));
		}
	}

	/**
	 * Check player afflictions to determine health icon
	 */
	private void checkHealthIcon()
	{
		int poison = client.getVar(VarPlayer.IS_POISONED);
		boolean isVenomed = poison >= 1000000;
		boolean isPoisoned = !isVenomed && poison > 0;
		boolean isDiseased = client.getVar(VarPlayer.DISEASE_VALUE) > 0;

		BufferedImage old = currentHeart;

		currentHeart = isVenomed ? HEART_VENOM : isPoisoned ? HEART_POISON : isDiseased ? HEART_DISEASE : HEART_NORMAL;

		if (old != currentHeart)
		{
			client.getWidgetSpriteCache().reset();
			client.getSpriteOverrides().put(SpriteID.MINIMAP_ORB_HITPOINTS_ICON, ImageUtil.getImageSpritePixels(currentHeart, client));
		}
	}

	/**
	 * Ensure the HP Heart is the default Sprite
	 */
	private void resetHealthIcon()
	{
		client.getWidgetSpriteCache().reset();
		client.getSpriteOverrides().remove(SpriteID.MINIMAP_ORB_HITPOINTS_ICON);
		currentHeart = null;
	}


	private void setRunOrbText(String text)
	{
		Widget runOrbText = client.getWidget(WidgetInfo.MINIMAP_RUN_ORB_TEXT);

		if (runOrbText != null)
		{
			runOrbText.setText(text);
		}
	}

	private void resetRunOrbText()
	{
		setRunOrbText(Integer.toString(client.getEnergy()));
	}

	/**
	 * Calculates the estimated time the player can continue running for
	 * @param inSeconds
	 * @return
	 */
	String getEstimatedRunTimeRemaining(boolean inSeconds)
	{
		// Calculate the amount of energy lost every 2 ticks (0.6 seconds).
		// Negative weight has the same depletion effect as 0 kg.
		final int effectiveWeight = Math.max(client.getWeight(), 0);
		double lossRate = (Math.min(effectiveWeight, 64) / 100.0) + 0.64;

		if (client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0)
		{
			lossRate *= 0.3; // Stamina effect reduces energy depletion to 30%
		}

		// Calculate the number of seconds left
		final double secondsLeft = (client.getEnergy() * 0.6) / lossRate;

		// Return the text
		if (inSeconds)
		{
			return Integer.toString((int) Math.floor(secondsLeft)) + "s";
		}
		else
		{
			final int minutes = (int) Math.floor(secondsLeft / 60.0);
			final int seconds = (int) Math.floor(secondsLeft - (minutes * 60.0));

			return Integer.toString(minutes) + ":" + StringUtils.leftPad(Integer.toString(seconds), 2, "0");
		}
	}

	private boolean isLocalPlayerWearingFullGraceful()
	{
		final ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

		if (equipment == null)
		{
			return false;
		}

		final Item[] items = equipment.getItems();

		// Check that the local player is wearing enough items to be using full Graceful
		// (the Graceful boots will have the highest slot index in the worn set).
		if (items == null || items.length <= EquipmentInventorySlot.BOOTS.getSlotIdx())
		{
			return false;
		}

		return (ALL_GRACEFUL_HOODS.contains(items[EquipmentInventorySlot.HEAD.getSlotIdx()].getId()) &&
			ALL_GRACEFUL_TOPS.contains(items[EquipmentInventorySlot.BODY.getSlotIdx()].getId()) &&
			ALL_GRACEFUL_LEGS.contains(items[EquipmentInventorySlot.LEGS.getSlotIdx()].getId()) &&
			ALL_GRACEFUL_GLOVES.contains(items[EquipmentInventorySlot.GLOVES.getSlotIdx()].getId()) &&
			ALL_GRACEFUL_BOOTS.contains(items[EquipmentInventorySlot.BOOTS.getSlotIdx()].getId()) &&
			ALL_GRACEFUL_CAPES.contains(items[EquipmentInventorySlot.CAPE.getSlotIdx()].getId()));
	}

	int getEstimatedRecoverTimeRemaining()
	{
		if (localPlayerRunningToDestination)
		{
			return -1;
		}

		// Calculate the amount of energy recovered every second
		double recoverRate = (48 + client.getBoostedSkillLevel(Skill.AGILITY)) / 360.0;

		if (isLocalPlayerWearingFullGraceful())
		{
			recoverRate *= 1.3; // 30% recover rate increase from Graceful set effect
		}

		// Calculate the number of seconds left
		final double secondsLeft = (100 - client.getEnergy()) / recoverRate;
		return (int) secondsLeft;
	}

	/**
	 * Migrates configs from runenergy and regenmeter to this plugin and deletes the old config values.
	 */
	private void migrateConfigs()
	{
		// Run Energy
		migrateConfig("runenergy", "replaceOrbText");

		// Regen Meter Configs
		migrateConfig("regenmeter", "showHitpoints");
		migrateConfig("regenmeter", "showSpecial");
		migrateConfig("regenmeter", "showWhenNoChange");
	}

	private void migrateConfig(String group, String key)
	{
		String value = configManager.getConfiguration(group, key);
		if (value != null)
		{
			configManager.setConfiguration("statusorbs", key, value);
			configManager.unsetConfiguration(group, key);
		}
	}
}
