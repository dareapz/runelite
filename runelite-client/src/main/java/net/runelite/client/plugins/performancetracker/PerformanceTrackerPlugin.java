/*
 * Copyright (c) 2018, TheStonedTurtle <https://github.com/TheStonedTurtle>
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
package net.runelite.client.plugins.performancetracker;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ExperienceChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.performancetracker.data.NpcExpModifier;
import net.runelite.client.plugins.performancetracker.overlays.GenericOverlay;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Performance Tracker",
	description = "Shows your current performance stats",
	tags = {"performance", "stats", "tracker", "theatre", "blood", "tob"}
)
@Slf4j
public class PerformanceTrackerPlugin extends Plugin
{
	// For every damage point dealt, 1.33 experience is given to the player's hitpoints (base rate)
	private static final double HITPOINT_RATIO = 1.33;

	@Inject
	private Client client;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private GenericOverlay genericOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PerformanceTrackerConfig config;

	private Actor oldTarget;
	private int region = 0;
	private double hpExp = 0;
	// Current stats
	@Getter
	private double dealt = 0;
	@Getter
	private double taken = 0;

	@Provides
	PerformanceTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PerformanceTrackerConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(genericOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(genericOverlay);
	}

	// Determine Region change
	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}


		int oldRegion = region;
		region = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID();
		if (oldRegion != region)
		{
			handleRegionChange();
		}
	}

	// Calculate Damage Dealt
	@Subscribe
	protected void onExperienceChanged(ExperienceChanged c)
	{
		if (c.getSkill().equals(Skill.HITPOINTS))
		{
			double oldExp = hpExp;
			hpExp = client.getSkillExperience(Skill.HITPOINTS);
			double diff = hpExp - oldExp;
			if (diff < 1)
			{
				return;
			}
			double damageDealt = calculateDamageDealt(diff);

			// Add damage dealt to the current logs
			log.debug("Damage Dealt: {} | Exact: {}", Math.round(damageDealt), damageDealt);
			dealt += damageDealt;
		}
	}

	// Calculate Damage Taken
	@Subscribe
	protected void onHitsplatApplied(HitsplatApplied e)
	{
		if (e.getActor().equals(client.getLocalPlayer()))
		{
			taken += e.getHitsplat().getAmount();
		}
	}

	// Ensure we know who we were last interacting with.
	@Subscribe
	public void onGameTick(GameTick tick)
	{
		oldTarget = client.getLocalPlayer().getInteracting();
	}

	/**
	 * Handles region changes while inside Theatre of Blood
	 *
	 */
	private void handleRegionChange()
	{
		switch (region)
		{
			// TODO: Add regions here for activity specific tracking
			default:
		}

		dealt = 0;
		taken = 0;
	}

	/**
	 * Calculates damage dealt based on HP xp gained
	 * @param diff HP xp gained
	 * @return damage dealt
	 */
	private double calculateDamageDealt(double diff)
	{
		double damageDealt = diff / HITPOINT_RATIO;

		// Determine which NPC we attacked.
		NPC target = (NPC) client.getLocalPlayer().getInteracting();
		if (target == null)
		{
			// If we are interacting with nothing we may have clicked away at the perfect time
			// Fall back to the actor we were interacting with last game tick
			if (oldTarget == null)
			{
				log.warn("Couldn't find current or past target...");
				return damageDealt;
			}
			target = (NPC) oldTarget;
		}

		// Account for NPCs phases by NPC id
		String targetName = getRealNpcName(target);
		log.debug("Attacking NPC named: {}", targetName);

		return damageDealt;
	}

	/**
	 * Return the NPC name accounting for special use cases
	 * @param target target NPC
	 * @return
	 */
	private String getRealNpcName(NPC target)
	{
		String name = target.getName();

		switch (name.toUpperCase())
		{
			// TODO: Add special cases
			default:
				return name;
		}
	}
}
