function show(text) { 
	engine.show(text);
}

function getEntity(uid) { 
	return engine.get(uid);
}

function getPlayer() {
	return engine.getPlayer().getUID();
}
