/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2013 - Maarten Driesen
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

package neon.core;

import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import neon.core.event.LoadEvent;
import neon.core.event.MagicTask;
import neon.core.event.ScriptAction;
import neon.core.event.TaskQueue;
import neon.core.handlers.InventoryHandler;
import neon.core.handlers.SkillHandler;
import neon.entities.Entity;
import neon.entities.EntityFactory;
import neon.entities.Item;
import neon.entities.Player;
import neon.entities.components.Stats;
import neon.entities.property.Ability;
import neon.entities.property.Feat;
import neon.entities.property.Gender;
import neon.entities.property.Skill;
import neon.magic.Effect;
import neon.magic.Spell;
import neon.magic.SpellFactory;
import neon.maps.Map;
import neon.resources.CGame;
import neon.resources.RCreature;
import neon.resources.RMod;
import neon.resources.RSign;
import neon.resources.RSpell.SpellType;
import neon.systems.files.FileUtils;
import neon.systems.files.XMLTranslator;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import com.google.common.eventbus.Subscribe;

public class GameLoader {
	private Engine engine;
	private TaskQueue queue;
	
	public GameLoader(Engine engine) {
		this.engine = engine;
		queue = engine.getQueue();
	}
	
	@Subscribe public void loadGame(LoadEvent le) {
		try {
		// spel laden
		switch(le.getMode()) {
		case LOAD: 
			loadGame(le.getSaveName()); 
			// aangeven dat laden gedaan is
			Engine.post(new LoadEvent.Done(this));
			break;
		case NEW: 
			initGame(le.race, le.name, le.gender, le.specialisation, 
					le.profession, le.sign); 			
			// aangeven dat laden gedaan is
			Engine.post(new LoadEvent.Done(this));
			break;
		default: 
			break;
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Loads a new game using the supplied data.
	 */
	private void initGame(String race, String name, Gender gender, 
			Player.Specialisation spec, String profession, RSign sign) {
		// speler initialiseren
		RCreature species = new RCreature(((RCreature)Engine.getResources().getResource(race)).toElement());
		Player player = new Player(species, name, gender, spec, profession);
		player.species.text = "@";
		engine.startGame(new Game(player, engine.getFileSystem(), engine.getScriptEngine(), engine.getPhysicsEngine()));
		setSign(player, sign);
		for(Skill skill : Skill.values()) {
			SkillHandler.checkFeat(skill, player);
		}
		
		// maps initialiseren
		UIDStore store = engine.getGame().getStore();
		initMaps(store);

		CGame game = (CGame)engine.getResources().getResource("game", "config");
		
		// starting items
		for(String i : game.getStartingItems()) {
			Item item = EntityFactory.getItem(i, store.createNewEntityUID());
			store.addEntity(item);
			InventoryHandler.addItem(player, item);
		}
		// starting spells
		for(String i : game.getStartingSpells()) {
			player.getMagicComponent().addSpell(SpellFactory.getSpell(i));
		}
		
		// player in positie brengen
		Rectangle bounds = player.getShapeComponent();
		bounds.setLocation(game.getStartPosition().x, game.getStartPosition().y);
		Atlas atlas = engine.getGame().getAtlas();
		Map map = atlas.getMap(store.getMapUID(game.getStartMap()));
		engine.getScriptEngine().put("map", map);
		atlas.setMap(map);
		atlas.setCurrentZone(game.getStartZone());
	}
	
	private void setSign(Player player, RSign sign) {
		player.setSign(sign.id);
		for(String power : sign.powers) {
			player.getMagicComponent().addSpell(SpellFactory.getSpell(power));
		}
		for(Ability ability : sign.abilities.keySet()) {
			player.getCharacteristicsComponent().addAbility(ability, sign.abilities.get(ability));
		}		
	}
	
	/*
	 * Loads a saved game.
	 * 
	 * @param save	the name of the saved game
	 */
	private void loadGame(String save) {
		Document doc = new Document();
		try {
			FileInputStream in = new FileInputStream("saves/" + save + "/save.xml");
			doc = new SAXBuilder().build(in);
			in.close();
		} catch (IOException e) {
			System.out.println("IOException in loadGame");
		} catch(JDOMException e) {
			System.out.println("JDOMException in loadGame");
		}
		Element root = doc.getRootElement();
		
		// save map naar temp kopiëren
		Path savePath = Paths.get("saves", save);
		Path tempPath = Paths.get("temp");
		FileUtils.copy(savePath, tempPath);
		
		// maps initialiseren
		initMaps(engine.getGame().getStore());
		
		// tijd juist zetten (met setTime(), anders worden listeners aangeroepen)
		engine.getTimer().setTime(Integer.parseInt(root.getChild("timer").getAttributeValue("ticks")));
		
		// player aanmaken
		loadPlayer(root.getChild("player"), engine.getGame().getAtlas());

		// events
		loadEvents(root.getChild("events"));
		
		// quests
		Element journal = root.getChild("journal");
		for(Element e: journal.getChildren()) {
			Engine.getPlayer().getJournal().addQuest(e.getAttributeValue("id"), e.getText());
			Engine.getPlayer().getJournal().updateQuest(e.getAttributeValue("id"), 
					Integer.parseInt(e.getAttributeValue("stage")));
		}		
	}
	
	private void loadEvents(Element events) {
		// gewone tasks
		for(Element event : events.getChildren("task")) {
			String description = event.getAttributeValue("desc");
			if(event.getAttribute("script") != null) {
				String script = event.getAttributeValue("script");
				queue.add(description, new ScriptAction(script));
			}
		}
		
		// getimede tasks
		for(Element event : events.getChildren("timer")) {
			String[] ticks = event.getAttributeValue("tick").split(":");
			int start = Integer.parseInt(ticks[0]);
			int period = Integer.parseInt(ticks[1]);
			int stop = Integer.parseInt(ticks[2]);
					
			switch(event.getAttributeValue("task")) {
			case "script": 
				queue.add(event.getAttributeValue("script"), start, period, stop);
				break;
			case "magic": 
				Effect effect = Effect.valueOf(event.getAttributeValue("effect").toUpperCase());
				float magnitude = Float.parseFloat(event.getAttributeValue("magnitude"));
				String script = event.getAttributeValue("script");
				SpellType type = SpellType.valueOf(event.getAttributeValue("type").toUpperCase());
				Entity caster = null;
				if(event.getAttribute("caster") != null) {
					caster = Engine.getGame().getEntity(Long.parseLong(event.getAttributeValue("caster")));
				}
				Entity target = null;
				if(event.getAttribute("target") != null) {
					target = Engine.getGame().getEntity(Long.parseLong(event.getAttributeValue("target")));
				}
				Spell spell = new Spell(target, caster, effect, magnitude, script, type);
				queue.add(new MagicTask(spell, stop), start, stop, period);
				break;
			}
		}
	}

	private void loadPlayer(Element playerData, Atlas atlas) {
		// player aanmaken
		RCreature species = (RCreature)Engine.getResources().getResource(playerData.getAttributeValue("race"));
		Player player = new Player(new RCreature(species.toElement()), playerData.getAttributeValue("name"), 
				Gender.valueOf(playerData.getAttributeValue("gender").toUpperCase()), Player.Specialisation.valueOf(playerData.getAttributeValue("spec")),
				playerData.getAttributeValue("prof"));
		engine.startGame(new Game(player, engine.getFileSystem(), engine.getScriptEngine(), engine.getPhysicsEngine()));
		Rectangle bounds = player.getShapeComponent();
		bounds.setLocation(Integer.parseInt(playerData.getAttributeValue("x")), Integer.parseInt(playerData.getAttributeValue("y")));
		player.setSign(playerData.getAttributeValue("sign"));
		player.species.text = "@";
		
		// start map
		int mapUID = Integer.parseInt(playerData.getAttributeValue("map"));
		atlas.setMap(atlas.getMap(mapUID));
		int level = Integer.parseInt(playerData.getAttributeValue("l"));
		atlas.setCurrentZone(level);
		
		// stats
		Stats stats = player.getStatsComponent();
		stats.addStr(Integer.parseInt(playerData.getChild("stats").getAttributeValue("str")) - stats.getStr());
		stats.addCon(Integer.parseInt(playerData.getChild("stats").getAttributeValue("con")) - stats.getCon());
		stats.addDex(Integer.parseInt(playerData.getChild("stats").getAttributeValue("dex")) - stats.getDex());
		stats.addInt(Integer.parseInt(playerData.getChild("stats").getAttributeValue("int")) - stats.getInt());
		stats.addWis(Integer.parseInt(playerData.getChild("stats").getAttributeValue("wis")) - stats.getWis());
		stats.addCha(Integer.parseInt(playerData.getChild("stats").getAttributeValue("cha")) - stats.getCha());
		
		// skills
		for(Attribute skill : (List<Attribute>)playerData.getChild("skills").getAttributes()) {
			player.setSkill(Skill.valueOf(skill.getName()), Integer.parseInt(skill.getValue()));
		}
		
		// items
		for(Element e : playerData.getChildren("item")) {
			long uid = Long.parseLong(e.getAttributeValue("uid"));
			player.getInventoryComponent().addItem(uid);
		}
		
		// spells
		for(Element e : playerData.getChildren("spell")) {
			player.getMagicComponent().addSpell(SpellFactory.getSpell(e.getText()));
		}
		
		// feats
		for(Element e: playerData.getChildren("feat")) {
			player.getCharacteristicsComponent().addFeat(Feat.valueOf(e.getText()));
		}

		// geld
		player.getInventoryComponent().addMoney(Integer.parseInt(playerData.getChildText("money")));		
	}

	private void initMaps(UIDStore store) {
		// mods en maps in uidstore steken
		for(RMod mod : Engine.getResources().getResources(RMod.class)) {
			if(store.getModUID(mod.id) == 0) {
				store.addMod(mod.id);
			}
			for(String[] path : mod.getMaps())
			try {	// maps zitten in twowaymap, en worden dus niet in cache opgeslagen
				Element map = engine.getFileSystem().getFile(new XMLTranslator(), path).getRootElement();
				short mapUID = Short.parseShort(map.getChild("header").getAttributeValue("uid"));
				int uid = UIDStore.getMapUID(store.getModUID(path[0]), mapUID);
				store.addMap(uid, path);
			} catch(Exception e) {
				Engine.getLogger().fine("Map error in mod " + path[0]);
			}			
		}
	}
}
