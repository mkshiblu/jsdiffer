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
		valueOrDefault: function (value, defaultValue) {
			return typeof value === 'undefined' ? defaultValue : value;
		},
		valueAtIndexOrDefault: function(value, index, defaultValue) {
			return helpers.valueOrDefault(helpers.isArray(value)? value[index] : value, defaultValue);
		},
	}
};