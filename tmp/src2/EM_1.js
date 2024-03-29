const PORTS = new function () {
    let counter = 0;
    this.incrementAndGet = function () {
        return ++counter;
    }
};

function createAddresses(count) {
    let addresses = [];

    for (let i = 0; i < count; i++) {
        addresses.push(createAddress("127.0.0.1", PORTS.incrementAndGet()));
    }

    return addresses;
}

function createAddress(host, port) {
    try {
        return new Address(host, port);
    } catch (e) {
        console.log(e);
    }
    return null;
}


function f3(p1){
    let b = 5;
}