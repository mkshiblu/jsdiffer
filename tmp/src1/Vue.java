package src1;

public class Vue {

	class Config {
		int[][] keyCodes;
	}

	Config config;

	static class Array {
		static boolean isArray(KeyCode arr) {
			return true;
		}
	}

	class KeyCode {
		int indexOf(KeyCode x) {
			return -2;
		}
	}

	private int hyphenate(KeyCode eventKeyName) {
		// TODO Auto-generated method stub
		return -1;
	}

	/**
	 * Runtime helper for checking keyCodes from config.
	 * exposed as Vue.prototype._k
	 * passing in eventKeyName as last argument separately for backwards compat
	 */
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
