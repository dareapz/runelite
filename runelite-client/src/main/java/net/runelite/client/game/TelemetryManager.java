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
package net.runelite.client.game;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.PluginManager;
import net.runelite.http.api.telemetry.TelemetryClient;
import net.runelite.http.api.telemetry.TelemetryData;
import net.runelite.http.api.telemetry.TelemetryType;

@Singleton
@Slf4j
public class TelemetryManager
{
	private final PluginManager pluginManager;
	private final TelemetryClient telemetryClient = new TelemetryClient();
	private List<TelemetryData> queue = new ArrayList<>();

	@Inject
	private TelemetryManager(PluginManager pluginManager)
	{
		this.pluginManager = pluginManager;
	}

	public void submit(TelemetryType type, Object data)
	{
		if (pluginManager.isPluginEnabled(TelemetryPlugin.class))
		{
			log.info("Telemetry data is disabled.");
			queue.clear();
			return;
		}

		log.info("Received {} Telemetry data: {}", type, data);
		queue.add(new TelemetryData(new Date(), type, data));
		if (queue.size() >= 2)
		{
			flush();
		}
	}

	private void clear()
	{
		queue.clear();
	}

	public void flush()
	{
		List<TelemetryData> data = new ArrayList<>(queue);
		queue.clear();

		log.info("Flushing queued Telemetry data: {}", data);
		telemetryClient.submit(data);
		log.info("Telemetry data flushed!");
	}
}
