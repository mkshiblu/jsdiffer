/*!
 * Vue.js v2.5.0
 * (c) 2014-2017 Evan You
 * Released under the MIT License.
 */
(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
	typeof define === 'function' && define.amd ? define(factory) :
	(global.Vue = factory());
}(this, (function () { 'use strict';

/*  */


// these helpers produces better vm code in JS engines due to their
// explicitness and function inlining
function isUndef (v) {
  return v === undefined || v === null
}


// LOTS OF OTHER CODE
//--------
//-----


})));
Test.count = -2;
Test.prototype = {

    after: function() {
        checkPollution();
    },

    queueHook: function (hook, hookName) {
        var promise,
            test = this;
        return function runHook() {
            config.current = test;
            if (config.notrycatch) {
                callHook();
                return;
            }
            try {
                callHook();
            } catch (error) {
                test.pushFailure(hookName + " failed on " + test.testName + ": " +
                    (error.message || error), extractStacktrace(error, 0));
            }

            function callHook() {
                promise = hook.call(test.testEnvironment, test.assert);
                test.resolvePromise(promise, hookName);
            }
        };
    },
};

let c = function d(){
console.log('Hello, world!')

}

c();

new c();
function x(){
}