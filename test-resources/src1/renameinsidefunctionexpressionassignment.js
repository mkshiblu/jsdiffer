module.exports = function (Chart) {
	var helpers = Chart.helpers = {
	    noop: function() {},
		isNullOrUndef: function (value) {
			return value === null || typeof value === 'undefined';
		},
		isArray: Array.isArray? Array.isArray : function(value) {
        			return Object.prototype.toString.call(value) === '[object Array]';
        		},

		isObject: function(value) {
			return value !== null && Object.prototype.toString.call(value) === '[object Object]';
		},
		getValueOrDefault: function (value, defaultValue) {
			return typeof value === 'undefined' ? defaultValue : value;
		},
		getValueAtIndexOrDefault: function (value, index, defaultValue) {
			if (helpers.isNullOrUndef(value)) {
				return defaultValue;
			}
			if (helpers.isArray(value)) {
				value = value[index];
				return typeof value === 'undefined' ? defaultValue : value;
			} return value;
		},
	}
};