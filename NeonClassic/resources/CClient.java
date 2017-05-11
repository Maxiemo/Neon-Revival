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

package neon.resources;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class CClient extends Resource {
	// keyboard settings
	public enum KeyboardSetting {
		NUMPAD, AZERTY, QWERTY, QWERTZ;
	}

	public String up = "NUMPAD8";
	public String upright = "NUMPAD9";
	public String right = "NUMPAD6";
	public String downright = "NUMPAD3";
	public String down = "NUMPAD2";
	public String downleft = "NUMPAD1";
	public String left = "NUMPAD4";
	public String upleft = "NUMPAD7";
	public String wait = "NUMPAD5";
	
	public String magic = "G";
	public String shoot = "F";
	public String look = "L";
	public String act = "SPACE";
	public String talk = "T";
	public String unmount = "U";
	public String sneak = "V";
	public String journal = "J";
	public String map = "M";

	private KeyboardSetting keys = KeyboardSetting.NUMPAD;
	
	// language settings
	private Properties strings;
	
	// other settings
	private String bigCoin = "€";
	private String smallCoin = "c";
	private String title = "";
	
	public CClient(String... path) {
		super("client", path);
		
		// file inladen
		Document doc = new Document();
		try (FileInputStream in = new FileInputStream(path[0])){
			doc = new SAXBuilder().build(in);
		} catch(Exception e) {
			e.printStackTrace();
		}
		Element root = doc.getRootElement();

		// keyboard
		setKeys(root.getChild("keys"));
		
		// taal
		Properties defaults = new Properties();	// locale.en laden als default
		try (FileInputStream stream = new FileInputStream("data/locale/locale.en"); 
				InputStreamReader reader = new InputStreamReader(stream, Charset.forName("UTF-8"))){
			defaults.load(reader);
		} catch(IOException e) {
			e.printStackTrace();
		} 

		String lang = root.getChild("lang").getText();
		strings = new Properties(defaults);		// locale initialiseren met 'en' defaults
		try (FileInputStream stream = new FileInputStream("data/locale/locale." + lang); 
				InputStreamReader reader = new InputStreamReader(stream, Charset.forName("UTF-8"))){
			strings.load(reader);
		} catch(IOException e) {
			e.printStackTrace();
		} 
	}

	@Override
	public void load() {}

	@Override
	public void unload() {}
	
	/**
	 * Return the string value with the given name.
	 * 
	 * @param name
	 * @return
	 */
	public String getString(String name) {
		return strings.getProperty(name);
	}
	
	public String getBig() {
		return bigCoin;
	}
	
	public void setBig(String name) {
		bigCoin = name;
	}
	
	public String getSmall() {
		return smallCoin;
	}
	
	public void setSmall(String name) {
		smallCoin = name;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public KeyboardSetting getSettings() {
		return keys;
	}

	public void setKeys(Element settings) {
		if(settings != null) {
			// movement keys
			switch(settings.getText()) {
			case "azerty": setKeys(KeyboardSetting.AZERTY); break;
			case "qwerty": setKeys(KeyboardSetting.QWERTY); break;
			case "qwertz": setKeys(KeyboardSetting.QWERTZ); break;
			}

			// andere keys
			if(settings.getAttribute("map") != null) {
				map = settings.getAttributeValue("map");
			}
			if(settings.getAttribute("act") != null) {
				act = settings.getAttributeValue("act");
			}
			if(settings.getAttribute("magic") != null) {
				magic = settings.getAttributeValue("magic");
			}
			if(settings.getAttribute("shoot") != null) {
				shoot = settings.getAttributeValue("shoot");
			}
			if(settings.getAttribute("look") != null) {
				look = settings.getAttributeValue("look");
			}
			if(settings.getAttribute("talk") != null) {
				talk = settings.getAttributeValue("talk");
			}
			if(settings.getAttribute("unmount") != null) {
				unmount = settings.getAttributeValue("unmount");
			}
			if(settings.getAttribute("sneak") != null) {
				sneak = settings.getAttributeValue("sneak");
			}
			if(settings.getAttribute("journal") != null) {
				journal = settings.getAttributeValue("journal");
			}
		}
	}
	
	public void setKeys(KeyboardSetting choice) {
		keys = choice;
		switch(keys) {
		case NUMPAD: 
			up = "NUMPAD8";
			upright = "NUMPAD9";
			right = "NUMPAD6";
			downright = "NUMPAD3";
			down = "NUMPAD2";
			downleft = "NUMPAD1";
			left = "NUMPAD4";
			upleft = "NUMPAD7";
			wait = "NUMPAD5";
			break;
		case AZERTY:
			up = "Z";
			upright = "E";
			right = "D";
			downright = "C";
			down = "X";
			downleft = "W";
			left = "Q";
			upleft = "A";
			wait = "S";
			break;
		case QWERTY:
			up = "W";
			upright = "E";
			right = "D";
			downright = "C";
			down = "X";
			downleft = "Z";
			left = "A";
			upleft = "Q";
			wait = "S";
			break;
		case QWERTZ:
			up = "W";
			upright = "E";
			right = "D";
			downright = "C";
			down = "X";
			downleft = "Y";
			left = "A";
			upleft = "Q";
			wait = "S";
			break;
		}
	}
}
