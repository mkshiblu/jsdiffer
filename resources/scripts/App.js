const parser = require('./Parser');

let content = `const PORTS = new function () {
    let counter = 0;
    this.incrementAndGet = function () {
        return ++counter;
    }
};

function createAddresses(count) {
    var addresses = new Array(count);

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
//content = require('fs').readFileSync('E:\\PROJECTS_REPO\\jquery\\external\\qunit\\qunit.js', 'utf-8');
const sourceModel = parser.parse(content);
const json = JSON.stringify(sourceModel);
console.log(json);



