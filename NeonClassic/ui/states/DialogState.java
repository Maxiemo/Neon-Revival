/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2013-2015 - Maarten Driesen
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

package neon.ui.states;

import neon.core.Engine;
import neon.entities.Creature;
import neon.entities.Player;
import neon.entities.components.HealthComponent;
import neon.magic.MagicUtils;
import neon.resources.CClient;
import neon.resources.RPerson;
import neon.resources.RSpell.SpellType;
import neon.resources.quest.Topic;
import neon.ui.UserInterface;
import neon.ui.custom.TopicCellRenderer;
import neon.ui.dialog.ChargeDialog;
import neon.ui.dialog.CrafterDialog;
import neon.ui.dialog.EnchantDialog;
import neon.ui.dialog.PotionDialog;
import neon.ui.dialog.RepairDialog;
import neon.ui.dialog.SpellMakerDialog;
import neon.ui.dialog.SpellTradeDialog;
import neon.ui.dialog.TattooDialog;
import neon.ui.dialog.TradeDialog;
import neon.ui.dialog.TrainingDialog;
import neon.ui.dialog.TravelDialog;
import neon.util.fsm.State;
import neon.util.fsm.TransitionEvent;

import org.jdom2.Element;

import com.google.common.eventbus.EventBus;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;

/**
 * Class that shows a list of subjects to talk about. The subjects shown depend on 
 * preconditions in running quests. 
 */
public class DialogState extends State {
	@FXML private Button quitButton;
	@FXML private TextFlow textArea;
	@FXML private ListView<Topic> topicList;
	@FXML private ScrollPane scroller;
	
	private Creature target;
    private String big, small;
    private EventBus bus;
    private UserInterface ui;
    private Topic topic;
    private Scene scene;
	
	public DialogState(State parent, EventBus bus, UserInterface ui, CClient config) {
		super(parent);
		this.bus = bus;
		this.ui = ui;
		
		big = config.getSmall();
		small = config.getBig();
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("dialog.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
		} catch (IOException e) {
			e.printStackTrace();
		}

		quitButton.setOnAction(event -> bus.post(new TransitionEvent("return")));
        topicList.setCellFactory(new TopicCellRenderer());
        topicList.setOnKeyPressed(new KeyListener());
        textArea.heightProperty().addListener(new ScrollListener());
	}
	
	@Override
	public void enter(TransitionEvent t) {
		Platform.runLater(() -> textArea.getChildren().clear());
		if(!t.toString().equals("back")) {
			target = (Creature)t.getParameter("speaker");
		}
		if(target != null) {
			Engine.getScriptEngine().put("NPC", target);
			Platform.runLater(() -> initDialog());
			Platform.runLater(() -> initServices());
			ui.showScene(scene);
		} else {
			bus.post(new TransitionEvent("return"));
		}
	}
	
	/*
	 *  Key handler voor de topicList.
	 */
	private class KeyListener implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			switch(ke.getCode()) {
			case ESCAPE: 
				bus.post(new TransitionEvent("return")); 
				break;
			case SPACE:
				topic = topicList.getSelectionModel().getSelectedItem();
				if(topic instanceof DummyTopic) {
					service(topic);
				} else {
					answer(topic);
				}
				initDialog();
				initServices();
				topicList.getSelectionModel().select(0);
				break;
			default:
				break;
			}
		}
	}
	
	private void answer(Topic topic) {
		Text phrase = new Text(topic.phrase + "\n");
		Text answer = new Text(topic.answer + "\n");
		textArea.getChildren().addAll(phrase, answer);
		Engine.getQuestTracker().doAction(topic);
	}
	
	private void service(Topic topic) {
		String service = topic.id;
		Player player = (Player)getVariable("player");

		if(service.equals("travel")) {
			new TravelDialog(ui, bus).show(player, target);
		} else if(service.equals("training")) {
			new TrainingDialog(ui, bus).show(player, target);
		} else if(service.equals("spells")) {
			new SpellTradeDialog(ui, big, small).show(player, target);
		} else if(service.equals("trade")) {
			new TradeDialog(ui, big, small).show(player, target);
		} else if(service.equals("spell maker")) {
			new SpellMakerDialog(ui).show(player, target);
		} else if(service.equals("potion maker")){
			new PotionDialog(ui, small).show(player, target);
		} else if(service.equals("healer")) {
			heal();
		} else if(service.equals("charge")) {
			new ChargeDialog(ui).show(player);
		} else if(service.equals("craft")) {
			new CrafterDialog(ui, small, bus).show(player, target);
		} else if(service.equals("enchant")) {
			new EnchantDialog(ui).show(player, target);
		} else if(service.equals("repair")) {
			new RepairDialog(ui).show(player, target);
		} else if(service.equals("tattoos")) {
			new TattooDialog(ui, small).show(player, target);
		} else {
			System.out.println("not implemented");
		}		
	}
	
	private void heal() {
		Player player = (Player)getVariable("player");
		HealthComponent health = player.getHealthComponent();
		health.heal(health.getHealth() - health.getBaseHealth());
		MagicUtils.cure(player, SpellType.CURSE);
		MagicUtils.cure(player, SpellType.DISEASE);
		MagicUtils.cure(player, SpellType.POISON);
		ui.showMessage("You have been healed!", 2);
	}
	
	private void initDialog() {
		ObservableList<Topic> subjects = topicList.getItems();
		subjects.clear();
		
		if(topic != null && !(topic instanceof DummyTopic) && 
				!Engine.getQuestTracker().getSubtopics(topic).isEmpty()) {
			subjects.addAll(Engine.getQuestTracker().getSubtopics(topic));
		} else {
			subjects.addAll(Engine.getQuestTracker().getDialog(target));
		}
	}
	
	private void initServices() {
		ArrayList<Topic> services = new ArrayList<Topic>();
		
		if(hasService(target.getID(), "training")) {
			services.add(new DummyTopic("training"));
		}
		if(hasService(target.getID(), "spells")) {
			services.add(new DummyTopic("spells"));
		}
		if(hasService(target.getID(), "trade")) {
			services.add(new DummyTopic("trade"));
		}
		if(hasService(target.getID(), "travel")) {
			services.add(new DummyTopic("travel"));
		}
		if(hasService(target.getID(), "spellmaker")) {
			services.add(new DummyTopic("spell maker"));
		}
		if(hasService(target.getID(), "alchemy")) {
			services.add(new DummyTopic("potion maker"));
		}
		if(hasService(target.getID(), "healer")) {
			services.add(new DummyTopic("healer"));
		}
		if(hasService(target.getID(), "charger")) {
			services.add(new DummyTopic("charge"));
		}
		if(hasService(target.getID(), "enchant")) {
			services.add(new DummyTopic("enchant"));
		}
		if(hasService(target.getID(), "craft")) {
			services.add(new DummyTopic("craft"));
		}
		if(hasService(target.getID(), "repair")) {
			services.add(new DummyTopic("repair"));
		}
		if(hasService(target.getID(), "tattoo")) {
			services.add(new DummyTopic("tattoos"));
		}
		
		topicList.getItems().addAll(services);
	}
	
	private boolean hasService(String name, String id) {
		try {
			RPerson person = (RPerson)Engine.getResources().getResource(name);
			for(Element e : person.services) {
				if(e.getAttributeValue("id").equals(id)) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return false;
	}
	
	/*
	 * Scrollt textarea automatisch naar laatst toegevoegde topic.
	 */
	private class ScrollListener implements ChangeListener<Number>{
		@Override
		public void changed(ObservableValue<? extends Number> value,
				Number previous, Number current) {
			scroller.vvalueProperty().setValue(1);			
		}		
	}
	
	public class DummyTopic extends Topic {
		public DummyTopic(String service) {
			super("dummy", "dummy", service, "", "Service: " + service, "", "");
		}		
	}
}
