package src1;

public class Vue {

	boolean isKeyNotMatch(KeyCode expect, KeyCode actual) {
		if (Array.isArray(expect)) {
			return expect.indexOf(actual) == -1;
		} else {
			return expect != actual;
		}
	}

	/**
	 * Runtime helper for checking keyCodes from config.
	 * exposed as Vue.prototype._k
	 * passing in eventKeyName as last argument separately for backwards compat
	 */
	boolean checkKeyCodes(
			KeyCode eventKeyCode,
			int key,
			KeyCode builtInKeyCode,
			KeyCode eventKeyName,
			KeyCode builtInKeyName) {
//var mappedKeyCode = config.keyCodes[key] || builtInKeyCode;
		KeyCode mappedKeyCode = config.keyCodes[key] == null ? null : builtInKeyCode;
		if (builtInKeyName != null && (eventKeyName != null) && !(config.keyCodes[key] != null)) {
			return isKeyNotMatch(builtInKeyName, eventKeyName);
		} else if (mappedKeyCode != null) {
			return isKeyNotMatch(mappedKeyCode, eventKeyCode);
		} else if (eventKeyName != null) {
			return hyphenate(eventKeyName) != key;
		}
		return false;
	}
}
