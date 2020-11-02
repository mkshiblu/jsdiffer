const PORTS = new function () {
    let counter = 0;
    this.incrementAndGet = function () {
        return ++counter;
    }
};

function createAddresses(count) {
    var addresses = new Array(count);

    for (let i = 0; i < count; i++) {
        let address = createAddress("127.0.0.1", PORTS.incrementAndGet());
        addresses.push(address);
    }

    return addresses;
}

function createAddress(host, port) {
    try {
        return new Address(host, port);
    } catch (e) {
        e.printStackTrace();
    }
    return null;
}

/**
 * Function Constructor
 */
function Address(host, port) {
    this.host = host;
    this.port = port;
}
