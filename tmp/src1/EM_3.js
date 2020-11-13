const PORTS = new function () {
    let counter = 0;
    this.incrementAndGet = function () {
        return ++counter;
    }
};

function createAddresses(count) {
    var addresses = new Array(count);

    for (let i = 0; i < count; i++) {
        try {
            let address = new Address("127.0.0.1", PORTS.incrementAndGet());
            addresses[i] = address;
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
