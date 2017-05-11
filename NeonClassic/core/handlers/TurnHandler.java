/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2013-2014 - Maarten Driesen
 * 
 *	This program is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neon.core.handlers;

import java.awt.Rectangle;
import java.util.Collection;

import com.google.common.eventbus.Subscribe;

import neon.core.Engine;
import neon.core.event.TurnEvent;
import neon.core.event.UpdateEvent;
import neon.entities.Creature;
import neon.entities.Player;
import neon.entities.components.HealthComponent;
import neon.entities.property.Condition;
import neon.entities.property.Habitat;
import neon.maps.*;
import neon.maps.Region.Modifier;
import neon.resources.CServer;
import neon.resources.RRegionTheme;

public class TurnHandler {
	private Generator generator;
	private Rectangle window;
	private CServer config;
	private Engine engine;
	
	public TurnHandler(CServer config, Engine engine) {
		this.config = config;
		this.engine = engine;
	}
	
	@Subscribe public void tick(TurnEvent te) {
		// indien beurt gedaan is (timer krijgt extra tick): 
		//	1) random regions controleren
		//	2) monsters controleren
		//	3) speler controleren
		
		// kijken of terrain moet gegenereerd worden
		window = te.getWindow();
		if(generator == null || !generator.isAlive()) {
			generator = new Generator();
			generator.start();
		} 

		// monsters controleren
		Player player = engine.getPlayer();
		for(long uid : engine.getGame().getAtlas().getCurrentZone().getCreatures()) {
			Creature creature = (Creature)engine.getGame().getEntity(uid);
			if(!creature.hasCondition(Condition.DEAD)) {
				HealthComponent health = creature.getHealthComponent();
				health.heal(creature.getStatsComponent().getCon()/100f);
				creature.getMagicComponent().addMana(creature.getStatsComponent().getWis()/100f);
				Rectangle pBounds = player.getShapeComponent();
				Rectangle cBounds = creature.getShapeComponent();
				if(pBounds.getLocation().distance(cBounds.getLocation()) < config.getAIRange()) {
					int spd = getSpeed(creature);
					Region region = engine.getGame().getAtlas().getCurrentZone().getRegion(cBounds.getLocation());
					if(creature.species.habitat == Habitat.LAND && region.getMovMod() == Modifier.SWIM) {
						spd = spd/4;	// zwemmende creatures hebben penalty
					}
					if(player.isSneaking()) {
						spd = spd*2;	// player krijgt penalty bij sneaken
					}
					
					while(spd > getSpeed(player)*Math.random()) {
						creature.brain.act();
						spd -= getSpeed(player);
					}
				}
			}
		}

		// player in gereedheid brengen voor volgende beurt
		HealthComponent health = player.getHealthComponent();
		health.heal(player.getStatsComponent().getCon()/100f);
		player.getMagicComponent().addMana(player.getStatsComponent().getWis()/100f);
		
		// en systems updaten
		engine.getPhysicsEngine().update();
		engine.post(new UpdateEvent(this));
	}
	
	private class Generator extends Thread {
		@Override
		public void run() {
			// enkel repainten nadat er iets gegenereerd is
			if(checkRegions()) {
				engine.post(new UpdateEvent(this));
			}
		}
	}
	
	/*
	 * Checks if any regions are visible that should be randomly generated.
	 */
	private boolean checkRegions() {	// die boolean is eigenlijk maar louche
		Zone zone = engine.getGame().getAtlas().getCurrentZone();
		boolean fixed = true;
		boolean generated = false;	// om aan te geven dat er iets gegenereerd werd
		
		do {
			Collection<Region> buffer = zone.getRegions(window);
			fixed = true;
			for(Region r : buffer) {
				if(!r.isFixed()) {
					generated = true;
					fixed = false;
					RRegionTheme theme = r.getTheme();
					r.fix();	// vanaf hier wordt theme null
					if(theme.id.startsWith("town")) {
						new TownGenerator(zone).generate(r.getX(), r.getY(), r.getWidth(), r.getHeight(), theme, r.getZ());
					} else {
						new WildernessGenerator(zone).generate(r, theme);
					}
				}
			}
		} while(fixed == false);
		
		return generated;
	}

	/*
	 * @return	a creature's speed
	 */
	private static int getSpeed(Creature creature) {
		int penalty = 3;
		if(InventoryHandler.getWeight(creature) > 9*creature.species.str) {
			return 0;
		} else if(InventoryHandler.getWeight(creature) > 6*creature.species.str) {
			penalty = 1;
		} else if(InventoryHandler.getWeight(creature) > 3*creature.species.str) {
			penalty = 2;
		}
		return (creature.getStatsComponent().getSpd())*penalty/3;
	}
}
