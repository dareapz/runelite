/*
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
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
package net.runelite.client.plugins.loottracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.loottracker.data.LootRecord;
import net.runelite.client.plugins.loottracker.data.Tab;
import net.runelite.client.plugins.loottracker.data.UniqueItem;
import net.runelite.client.plugins.loottracker.ui.LandingPanel;
import net.runelite.client.plugins.loottracker.ui.LootPanel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;

@Slf4j
public class LootTrackerPanel extends PluginPanel
{
	private static final String HTML_LABEL_TEMPLATE =
			"<html><body style='color:%s'>%s<span style='color:white'>%s</span></body></html>";
	private static final BufferedImage ICON_DELETE;
	private static final BufferedImage ICON_REFRESH;
	private static final BufferedImage ICON_BACK;

	// Panel Colors
	private final static Color BACKGROUND_COLOR = ColorScheme.DARK_GRAY_COLOR;
	private final static Color BUTTON_COLOR = ColorScheme.DARKER_GRAY_COLOR;
	private final static Color BUTTON_HOVER_COLOR = ColorScheme.DARKER_GRAY_HOVER_COLOR;

	static
	{
		BufferedImage i1;
		BufferedImage i2;
		BufferedImage i3;
		try
		{
			synchronized (ImageIO.class)
			{
				i1 = ImageIO.read(LootTrackerPanel.class.getResourceAsStream("delete-white.png"));
				i2 = ImageIO.read(LootTrackerPanel.class.getResourceAsStream("refresh-white.png"));
				i3 = ImageIO.read(LootTrackerPanel.class.getResourceAsStream("back-arrow-white.png"));
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		ICON_DELETE = i1;
		ICON_REFRESH = i2;
		ICON_BACK = i3;
	}

	// When there is no loot, display this
	private final PluginErrorPanel errorPanel = new PluginErrorPanel();

	// Handle overall session data
	private final ItemManager itemManager;
	private final LootTrackerPlugin plugin;

	private String currentView = null;
	private LootPanel lootPanel;
	private LandingPanel landingPanel;
	private Set<String> names;

	private final Multimap<String, LootRecord> lootMap = ArrayListMultimap.create();

	LootTrackerPanel(final ItemManager itemManager, LootTrackerPlugin plugin)
	{
		super(false);

		this.itemManager = itemManager;
		this.plugin = plugin;

		setBorder(new EmptyBorder(6, 6, 6, 6));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());

		errorPanel.setBorder(new EmptyBorder(10, 25, 10, 25));

		this.names = new HashSet<>();
		createLandingPanel();
	}

	public void setNames(Set<String> n)
	{
		this.names = n;
		if (currentView == null)
		{
			// Refresh panel if looking at landing page.
			showLandingPage();
		}
	}

	// Wrapper for creating LootPanel
	private void createLandingPanel()
	{
		this.removeAll();
		currentView = null;

		errorPanel.setContent("Loot Tracker", "Please select the Activity, Player, or NPC you wish to view loot for");

		// TODO: Figure out how to pull this info from plugin without causing errors on initial load
		// Unique Items Info
		log.info("Names: {}", names);
		Set<String> alphabeticalNames = names.stream().sorted().collect(Collectors.toSet());
		landingPanel = new LandingPanel(alphabeticalNames, this, itemManager);

		this.add(errorPanel, BorderLayout.NORTH);
		this.add(wrapContainer(landingPanel), BorderLayout.CENTER);
	}

	// Landing page (Boss Selection Screen)
	private void createLootPanel(String name)
	{
		this.removeAll();
		currentView = name;

		Collection<LootRecord> data = plugin.getData(name);
		lootMap.putAll(name, data);

		// Grab all Uniques for this NPC/Activity
		ArrayList<UniqueItem> uniques = new ArrayList<>();
		if (Tab.getByName(name) != null)
		{
			uniques = UniqueItem.getByActivityName(Tab.getByName(name).getName());
		}

		JPanel title = createLootTitle(name);

		lootPanel = new LootPanel(data, UniqueItem.createPositionSetMap(uniques),  itemManager);

		this.add(title, BorderLayout.NORTH);
		this.add(wrapContainer(lootPanel), BorderLayout.CENTER);
	}

	// Creates the title panel for the recorded loot tab
	private JPanel createLootTitle(String name)
	{
		JPanel title = new JPanel();
		title.setBorder(new CompoundBorder(
				new EmptyBorder(10, 8, 8, 8),
				new MatteBorder(0, 0, 1, 0, Color.GRAY)
		));
		title.setLayout(new BorderLayout());
		title.setBackground(BACKGROUND_COLOR);

		JPanel first = new JPanel();
		first.setBackground(BACKGROUND_COLOR);

		// Back Button
		JLabel back = createIconLabel(ICON_BACK);
		back.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				showLandingPage();
			}
		});

		// Plugin Name
		JLabel text = new JLabel(name);
		text.setForeground(Color.WHITE);

		first.add(back);
		first.add(text);

		JPanel second = new JPanel();
		second.setBackground(BACKGROUND_COLOR);

		// Refresh Data button
		JLabel refresh = createIconLabel(ICON_REFRESH);
		refresh.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				log.info("TODO: refresh");
			}
		});

		// Clear data button
		JLabel clear = createIconLabel(ICON_DELETE);
		clear.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				clearData(name);
			}
		});

		second.add(refresh);
		second.add(clear);

		title.add(first, BorderLayout.WEST);
		title.add(second, BorderLayout.EAST);

		return title;
	}

	public void showLootPage(String name)
	{
		createLootPanel(name);

		this.revalidate();
		this.repaint();
	}

	private void showLandingPage()
	{
		createLandingPanel();

		this.revalidate();
		this.repaint();
	}

	// Helper Functions
	private JLabel createIconLabel(BufferedImage icon)
	{
		JLabel label = new JLabel();
		label.setIcon(new ImageIcon(icon));
		label.setOpaque(true);
		label.setBackground(BACKGROUND_COLOR);

		label.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				label.setBackground(BUTTON_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				label.setBackground(BACKGROUND_COLOR);
			}
		});

		return label;
	}

	// Wrap the panel inside a scroll pane
	private JScrollPane wrapContainer(JPanel container)
	{
		JPanel wrapped = new JPanel(new BorderLayout());
		wrapped.add(container, BorderLayout.NORTH);
		wrapped.setBackground(BACKGROUND_COLOR);

		JScrollPane scroller = new JScrollPane(wrapped);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		scroller.setBackground(BACKGROUND_COLOR);

		return scroller;
	}

	private void clearData(String name)
	{
		if (lootPanel.getRecords().size() == 0)
		{
			JOptionPane.showMessageDialog(this.getRootPane(), "No data to remove!");
			return;
		}

		int delete = JOptionPane.showConfirmDialog(this.getRootPane(), "<html>Are you sure you want to clear all data for this tab?<br/>There is no way to undo this action.</html>", "Warning", JOptionPane.YES_NO_OPTION);
		if (delete == JOptionPane.YES_OPTION)
		{
			plugin.clearData(name);
			// Refresh current panel with empty data
			lootPanel.updateRecords(new ArrayList<>());
			lootMap.removeAll(name);
			names.remove(name);
		}
	}

	public void addLootRecord(LootRecord r)
	{
		String name = r.getName().toLowerCase();
		lootMap.put(name, r);
		names.add(name);

		// Auto switch to tab if on landing page
		if (currentView == null)
		{
			showLootPage(name);
		}
		else if (currentView.equals(name))
		{
			// Recreate the page
			lootPanel.updateRecords(lootMap.get(name));
		}
	}
}
