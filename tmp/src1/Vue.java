package src1;

public class Vue {
	boolean checkKeyCodes(
			KeyCode eventKeyCode,
			int key,
			KeyCode builtInAlias,
			KeyCode eventKeyName) {
		// var keyCodes = config.keyCodes[key] || builtInAlias;
		KeyCode keyCodes = config.keyCodes[key] == null ? null : builtInAlias;

		if (keyCodes != null) {
			if (Array.isArray(keyCodes)) {
				return keyCodes.indexOf(eventKeyCode) == -1;
			} else {
				return keyCodes != eventKeyCode;
			}
		} else if (eventKeyName != null) {
			return hyphenate(eventKeyName) != key;
		}
		return false;
	}
}
