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
package net.runelite.client.plugins.skillcalculator.banked.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import net.runelite.client.game.AsyncBufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.skillcalculator.SkillCalculator;
import net.runelite.client.plugins.skillcalculator.banked.CriticalItem;
import net.runelite.client.plugins.skillcalculator.banked.beans.Activity;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;

public class OptionPanel extends JPanel
{

	public OptionPanel(Map.Entry<CriticalItem, List<Activity>> entry, ItemManager itemManager, SkillCalculator parent)
	{
		setLayout(new BorderLayout());

		JLabel label = new JLabel(entry.getKey().getComposition().getName());

		MaterialTabGroup group = new MaterialTabGroup();
		group.setLayout(new GridLayout(0, 6, 0, 2));
		group.setBorder(new MatteBorder(1, 1, 1, 1, Color.BLACK));

		for (Activity option : entry.getValue())
		{
			AsyncBufferedImage icon = itemManager.getImage(option.getIcon());
			MaterialTab matTab = new MaterialTab("", group, null);
			matTab.setHorizontalAlignment(SwingUtilities.RIGHT);
			matTab.setToolTipText(option.getName());

			matTab.setOnSelectEvent(() ->
			{
				parent.activitySelected(entry.getKey(), option);
				return true;
			});

			Runnable resize = () ->
				matTab.setIcon(new ImageIcon(icon.getScaledInstance(24, 21, Image.SCALE_SMOOTH)));
			icon.onChanged(resize);
			resize.run();

			group.addTab(matTab);
		}

		group.select(group.getTab(0)); // Select first option;

		add(label, BorderLayout.NORTH);
		add(group, BorderLayout.SOUTH);
	}
}
