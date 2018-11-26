/*
 * Copyright (c) 2018, Forsco <https://github.com/forsco>
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

package net.runelite.client.plugins.telemetry;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.TelemetryManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.telemetry.data.NpcLootTelemetry;
import net.runelite.client.plugins.telemetry.data.NpcSpawnedTelemetry;
import net.runelite.client.task.Schedule;

@PluginDescriptor(
	name = "Telemetry Plugin",
	enabledByDefault = false
)
@Slf4j
public class TelemetryPlugin extends Plugin
{
	private static final int MAX_SPAWN_TILE_RANGE = 10;
	// 5 Minute in Milliseconds
	private static final int TIME_EXPIRE_PERIOD = 5 * 60 * 1000;

	private boolean ignoreTick;
	private int tickCount;

	@Inject
	private Client client;

	@Inject
	private TelemetryManager telemetryManager;

	@Override
	protected void shutDown() throws Exception
	{
		telemetryManager.clear();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGIN_SCREEN:
				ignoreTick = true;
				telemetryManager.flush();
				break;
			case LOADING:
				ignoreTick = true;
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (ignoreTick && tickCount < 1)
		{
			tickCount++;
			return;
		}

		tickCount = 0;
		ignoreTick = false;
	}

	@Subscribe
	public void onNpcLootReceived(final NpcLootReceived e)
	{
		telemetryManager.submit(new NpcLootTelemetry(e.getNpc().getId(), e.getItems()));
	}

	@Subscribe
	public void onNpcSpawned(final NpcSpawned npcSpawned)
	{
		NPC n = npcSpawned.getNpc();
		if (n.getId() == -1)
		{
			return;
		}

		if (client.getLocalPlayer().getWorldLocation().distanceTo(n.getWorldLocation()) <= MAX_SPAWN_TILE_RANGE && !ignoreTick)
		{
			telemetryManager.submit(new NpcSpawnedTelemetry(n.getId(), n.getWorldLocation()));
		}

	}

	@Schedule(
		unit = ChronoUnit.MINUTES,
		period = 1
	)
	public void checkFlush()
	{
		Date expires = telemetryManager.getLastSubmitDate();
		if (expires == null)
		{
			return;
		}

		long expireTime = expires.getTime() + TIME_EXPIRE_PERIOD;
		if (new Date().getTime() >= expireTime)
		{
			telemetryManager.flush();
		}
	}
}
