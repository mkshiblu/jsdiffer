const parser = require('./scripts/Parser');
let content = `const PORTS = new function () {
    let counter = 0;
    this.incrementAndGet = function () {
        return ++counter;
    }
};

function createAddresses(count, d) {
    var addresses = new Array(count);
    const v = d.raw();
    for (let i = 0; i < count; i++) {
        try {
            addresses[i] = new Address("127.0.0.1", PORTS.incrementAndGet());
        } catch (e) {
            console.log(e);
        }
    }

    return addresses;
}

/**
 * Function Constructor
 */
function Address(host, port) {
    this.host = host;
    this.port = port;
}
`;
//content = require('fs').readFileSync('E:\\PROJECTS_REPO\\vue.js', 'UTF-8');
content = require('fs').readFileSync('../resources/real-projects/vue/src1/vue_common.js', 'utf-8');
const sourceModel = parser.parse(content);
const json = JSON.stringify(sourceModel);
require('fs').writeFileSync("E:\\functions.json", json);
console.log(json);



