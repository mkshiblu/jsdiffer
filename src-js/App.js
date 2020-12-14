const parser = require('./scripts/Parser');
let content = `
Test.count = 0;
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
`;
//content = require('fs').readFileSync('E:\\PROJECTS_REPO\\vue.js', 'UTF-8');
//content = require('fs').readFileSync('../resources/real-projects/vue/src1/vue_common.js', 'utf-8');
//content = require('fs').readFileSync('../test-resources/ExtractOrInlineFunction/src1/vue_runtime_common.js', 'utf-8');
const sourceModel = parser.parse(content);
const json = JSON.stringify(sourceModel);
require('fs').writeFileSync("E:\\functions.json", json);
console.log(json);
