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
package net.runelite.client.plugins.ironscape.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ClanMemberRank;

@Getter
@AllArgsConstructor
public class Member
{
	private final String name;
	private final ClanMemberRank rank;
	private final String clanChat;
	private final String date;
	@Setter
	private int tally;

	public static Collection<Member> consolidate(Collection<Member> newMembers, Collection<Member> storedMembers)
	{
		List<Member> members = new ArrayList<>(storedMembers);
		Member[] mems = newMembers.toArray(new Member[0]);
		for (Member m : mems)
		{
			Member newM = new Member(m.getName(), m.getRank(), m.getClanChat(), m.getDate(), m.getTally());
			if (members.contains(newM))
			{
				Member old = members.get(members.indexOf(newM));
				if (old.getDate().equals(newM.getDate()))
				{
					// Clear new tally so when adding together we just use old value
					newM.setTally(0);
				}
				newM.setTally(newM.getTally() + old.getTally());
				members.remove(newM);
			}
			members.add(newM);
		}
		return members;
	}
	
	@Override
	public String toString()
	{
		return name + "," + rank + "," + clanChat;
	}

	public String toOutputString()
	{
		return name + "," + rank + "," + date + "," + tally;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Member))
		{
			return false;
		}

		return o.toString().equals(this.toString());
	}

	@Override
	public int hashCode()
	{
		int result = 31;
		int hash = 1;
		hash = result * hash + ((name == null) ? 0 : name.hashCode());
		hash = result * hash + rank.getValue();
		hash = result * hash + ((clanChat == null) ? 0 : clanChat.hashCode());
		return hash;
	}
}
