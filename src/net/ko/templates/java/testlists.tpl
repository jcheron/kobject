KScriptTimer.start("%className%");
		KListObject<%className%> %listName%=(KListObject<%className%>) KoSession.kloadMany(%className%.class);
		KScriptTimer.stop("%className%");
		System.out.println(%listName%);
