/**
 * Runtime helper for checking keyCodes from config.
 * exposed as Vue.prototype._k
 * passing in eventKeyName as last argument separately for backwards compat
 */
function checkKeyCodes (
    eventKeyCode,
    key,
    builtInAlias,
    eventKeyName
) {
    var keyCodes = config.keyCodes[key] || builtInAlias;
    if (keyCodes) {
        if (Array.isArray(keyCodes)) {
            return keyCodes.indexOf(eventKeyCode) === -1
        } else {
            return keyCodes !== eventKeyCode
        }
    } else if (eventKeyName) {
        return hyphenate(eventKeyName) !== key
    }
}
