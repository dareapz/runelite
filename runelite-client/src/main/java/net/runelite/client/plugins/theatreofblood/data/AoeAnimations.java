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
package net.runelite.client.plugins.theatreofblood.data;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.AnimationID;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;

@AllArgsConstructor
@Slf4j
public enum AoeAnimations
{
	THROW_CHINCHOMPA(AnimationID.THROW_CHINCHOMPA, 9, 3, 3),
	CAST_BURST_BARRAGE(AnimationID.CAST_BURST_BARRAGE, 9, 3, 3),
	HALBERD_SPECIAL_ATTACK(AnimationID.HALBERD_SPECIAL_ATTACK, 10, 3, 1),
	D2H_SPECIAL_ATTACK(AnimationID.D2H_SPECIAL_ATTACK, 14, 3, 3, false),
	SCYTHE_OF_VITUR_ATTACK(AnimationID.SCYTHE_OF_VITUR_ATTACK, 3, 3, 1);

	private int animationID;
	private int maxTargets;
	private int xRange;
	private int yRange;
	private boolean basedOnTargetLoc;

	AoeAnimations(int id, int max, int x, int y)
	{
		this(id, max, x, y, true);
	}

	public List<Tile> getAoeTiles(Tile[][][] tiles, Actor localPlayer, Actor targetActor)
	{
		List<Tile> aoeTiles = new ArrayList<>();

		log.info("Local Player WP: {}", localPlayer.getWorldLocation());
		log.info("Target Actor WP: {}", targetActor.getWorldLocation());
		Actor target = (basedOnTargetLoc ? targetActor : localPlayer);
		log.info("Selected Starting WP: {}", target.getWorldLocation());
		log.info("Target Orientation: {}", target.getOrientation());

		LocalPoint loc = target.getLocalLocation();
		Tile[][] t = tiles[target.getWorldLocation().getPlane()];

		int sceneX = loc.getSceneX();
		int sceneY = loc.getSceneY();

		Tile targetTile = t[sceneX][sceneY];
		log.info("Target Tile: {}", targetTile.getWorldLocation());

		for (int y = 0; y < yRange; y++)
		{
			int realY = adjustRange(y);
			for (int x = 0; x < xRange; x++)
			{
				int realX = adjustRange(x);
				aoeTiles.add(t[sceneX + realX][sceneY + realY]);
			}
		}

		log.info("Tiles within AOE range?: {}", aoeTiles);

		return aoeTiles;
	}

	private int adjustRange(int value)
	{
		if (value < 2)
		{
			return value;
		}

		if (value % 2 == 0)
		{
			return (value / 2) * -1;
		}

		return value;
	}

	public static AoeAnimations getByAnimationId(int id)
	{
		for (AoeAnimations a : values())
		{
			if (a.animationID == id)
			{
				return a;
			}
		}

		return null;
	}

}
