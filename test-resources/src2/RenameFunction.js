function m2(){
    let z = 5;
    let d = function a1(){
        function isKeyNotMatch (expect, actual) {
          if (Array.isArray(expect)) {
            return expect.indexOf(actual) === -1
          } else {
            return expect !== actual
          }
        }

        /**
         * Runtime helper for checking keyCodes from config.
         * exposed as Vue.prototype._k
         * passing in eventKeyName as last argument separately for backwards compat
         */
        function checkKeyCodes (
          eventKeyCode,
          key,
          builtInKeyCode,
          eventKeyName,
          builtInKeyName
        ) {
          var mappedKeyCode = config.keyCodes[key] || builtInKeyCode;
          if (builtInKeyName && eventKeyName && !config.keyCodes[key]) {
            return isKeyNotMatch(builtInKeyName, eventKeyName)
          } else if (mappedKeyCode) {
            return isKeyNotMatch(mappedKeyCode, eventKeyCode)
          } else if (eventKeyName) {
            return hyphenate(eventKeyName) !== key
          }
        }
        let obj = {
            name: "John",
            Age: 25
        };
    }
}