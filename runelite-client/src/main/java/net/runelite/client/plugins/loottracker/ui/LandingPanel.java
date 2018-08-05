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
package net.runelite.client.plugins.loottracker.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.game.AsyncBufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.loottracker.LootTrackerPanel;
import net.runelite.client.plugins.loottracker.data.Tab;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;

@Getter
public class LandingPanel extends JPanel
{
	private Set<String> names;
	private LootTrackerPanel parent;
	private ItemManager itemManager;

	// Panel Colors
	private final static Color BACKGROUND_COLOR = ColorScheme.DARK_GRAY_COLOR;
	private final static Color BUTTON_COLOR = ColorScheme.DARKER_GRAY_COLOR;
	private final static Color BUTTON_HOVER_COLOR = ColorScheme.DARKER_GRAY_HOVER_COLOR;

	public LandingPanel(Set<String> names, LootTrackerPanel parent, ItemManager itemManager)
	{
		this.names = names == null ? new HashSet<>() : names;
		this.parent = parent;
		this.itemManager = itemManager;

		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(0, 8, 0, 0));
		this.setBackground(ColorScheme.DARK_GRAY_COLOR);

		createPanel();
	}

	private void createPanel()
	{
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;


		Set<String> categories = Tab.categories;
		for (String categoryName : categories)
		{
			MaterialTabGroup thisTabGroup = new MaterialTabGroup();
			thisTabGroup.setLayout(new GridLayout(0, 4, 7, 7));
			thisTabGroup.setBorder(new EmptyBorder(4, 0, 0, 0));

			JLabel name = new JLabel(categoryName);
			name.setBorder(new EmptyBorder(8, 0, 0, 0));
			name.setForeground(Color.WHITE);
			name.setVerticalAlignment(SwingConstants.CENTER);

			ArrayList<Tab> categoryTabs = Tab.getByCategoryName(categoryName);
			for (Tab tab : categoryTabs)
			{
				// Create tab (with hover effects/text)
				MaterialTab materialTab = new MaterialTab("", thisTabGroup, null);
				materialTab.setName(tab.getName());
				materialTab.setToolTipText(tab.getBossName());
				materialTab.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseEntered(MouseEvent e)
					{
						materialTab.setBackground(BUTTON_HOVER_COLOR);
					}

					@Override
					public void mouseExited(MouseEvent e)
					{
						materialTab.setBackground(BUTTON_COLOR);
					}
				});

				// Attach Icon to the Tab
				AsyncBufferedImage image = itemManager.getImage(tab.getItemID());
				Runnable resize = () ->
				{
					materialTab.setIcon(new ImageIcon(image.getScaledInstance(35, 35, Image.SCALE_SMOOTH)));
					materialTab.setOpaque(true);
					materialTab.setBackground(BUTTON_COLOR);
					materialTab.setHorizontalAlignment(SwingConstants.CENTER);
					materialTab.setVerticalAlignment(SwingConstants.CENTER);
					materialTab.setPreferredSize(new Dimension(35, 35));
				};
				image.onChanged(resize);
				resize.run();

				materialTab.setOnSelectEvent(() ->
				{
					parent.showLootPage(tab.getBossName());
					materialTab.unselect();
					materialTab.setBackground(BACKGROUND_COLOR);
					return true;
				});

				thisTabGroup.addTab(materialTab);
			}

			if (thisTabGroup.getComponentCount() > 0)
			{
				this.add(name, c);
				c.gridy++;
				this.add(thisTabGroup, c);
				c.gridy++;
			}
		}

		boolean f = false;
		for (String name: this.names)
		{
			// Only add if not already via code above
			Tab tab = Tab.getByName(name);
			if (tab == null)
			{
				if (!f)
				{
					JLabel l = new JLabel("Miscellaneous");
					l.setBorder(new EmptyBorder(8, 0, 0, 0));
					l.setForeground(Color.WHITE);
					l.setVerticalAlignment(SwingConstants.CENTER);
					f = true;
					this.add(l, c);
					c.gridy++;
					c.insets = new Insets(10, 0, 0, 0);
				}

				JPanel p = new JPanel();
				p.add(new JLabel(name));
				p.setBackground(BUTTON_COLOR);
				p.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseEntered(MouseEvent e)
					{
						p.setBackground(BUTTON_HOVER_COLOR);
					}

					@Override
					public void mouseExited(MouseEvent e)
					{
						p.setBackground(BUTTON_COLOR);
					}

					@Override
					public void mouseClicked(MouseEvent e)
					{
						SwingUtilities.invokeLater(() -> parent.showLootPage(name));
					}
				});

				this.add(p, c);
				c.gridy++;
			}
		}
	}
}