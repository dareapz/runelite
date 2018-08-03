/*
 * Copyright (c) 2018, TheStonedTurtle <www.github.com/TheStonedTurtle>
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
package net.runelite.client.plugins.loottracker.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import net.runelite.client.game.ItemStack;

public class LootRecord
{
	@Getter
	private final int id;
	@Getter
	private final String name;
	@Getter
	private final int level;
	@Getter
	private final int killCount;
	@Getter
	final Collection<ItemStack> drops;

	public LootRecord(int id, String name, int level, int kc, Collection<ItemStack> drops)
	{
		this.id = id;
		this.name = name;
		this.level = level;
		this.killCount = kc;
		this.drops = (drops == null ? new ArrayList<>() : drops);
	}

	/**
	 * Add the requested ItemStack to this LootRecord
	 * @param drop ItemStack to add
	 */
	public void addDropEntry(ItemStack drop)
	{
		drops.add(drop);
	}

	/**
	 * Consolidates all LootRecords into a single LootRecord by `name`
	 * This will combine the `drops` for all entries and sets the new record's killCount to -1
	 * @param entries Collection of LootRecords to Consolidate
	 * @return Collection of Consolidated LootRecords
	 */
	public static Collection<LootRecord> consolidateLootRecordsByName(Collection<LootRecord> entries)
	{
		Map<String, LootRecord> map = new HashMap<>();

		for (LootRecord e : entries)
		{
			// Grab loot entry from map or create it. Don't pass drops to prevent duplicate adds
			LootRecord entry = map.computeIfAbsent(e.getName(), v -> new LootRecord(e.getId(), e.getName(), e.getLevel(), -1, null));
			entry.getDrops().addAll(e.getDrops());
			map.put(e.getName(), entry);
		}

		return map.values();
	}

	/**
	 * Consolidates all LootRecords into a single LootRecord by `id`
	 * This will combine the `drops` for all entries and sets the new record's killCount to -1
	 * @param entries Collection of LootRecords to Consolidate
	 * @return Collection of Consolidated LootRecords
	 */
	public static Collection<LootRecord> consolidateLootRecordsById(Collection<LootRecord> entries)
	{
		Map<Integer, LootRecord> map = new HashMap<>();

		for (LootRecord e : entries)
		{
			// Grab loot entry from map or create it. Don't pass drops to prevent duplicate adds
			LootRecord entry = map.computeIfAbsent(e.getId(), v -> new LootRecord(e.getId(), e.getName(), e.getLevel(), -1, null));
			entry.getDrops().addAll(e.getDrops());
			map.put(e.getId(), entry);
		}

		return map.values();
	}

	/**
	 * Consolidate all `drops` by item id and returns an updated LootRecord
	 * @param r LootRecord to consolidate drops for
	 * @return LootRecord with Consolidated drops
	 */
	public static LootRecord consildateDropEntries(LootRecord r)
	{
		LootRecord result = new LootRecord(r.getId(), r.getName(), r.getLevel(), r.getKillCount(), null);

		Map<Integer, Integer> trueItems = new HashMap<>();
		for (ItemStack i : r.getDrops())
		{
			int count = trueItems.computeIfAbsent(i.getId(), v -> 0);
			trueItems.put(i.getId(), count + i.getQuantity());
		}

		for (Map.Entry<Integer, Integer> e : trueItems.entrySet())
		{
			result.addDropEntry(new ItemStack(e.getKey(), e.getValue()));
		}

		return result;
	}

	@Override
	public String toString()
	{
		StringBuilder m = new StringBuilder();
		m.append("LootRecord{id=")
				.append(id)
				.append(",name=")
				.append(name)
				.append(",killCount=")
				.append(killCount)
				.append(",drops=[");

		boolean addComma = false;
		for (ItemStack d : drops)
		{
			if (addComma)
			{
				m.append(",");
			}

			m.append(d.toString());
			addComma = true;
		}
		m.append("]}");

		return m.toString();
	}
}