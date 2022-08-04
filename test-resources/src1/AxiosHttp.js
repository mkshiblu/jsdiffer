module.exports = function httpAdapter(resolve, reject, config) {
    var data = config.data;
    var headers = config.headers;

    var responseBuffer = [];
    stream.on('data', function handleStreamData(chunk) {
        responseBuffer.push(chunk);

        // make sure the content length is not over the maxContentLength if specified
        if (config.maxContentLength > -1 && Buffer.concat(responseBuffer).length > config.maxContentLength) {
            reject(new Error('maxContentLength size of ' + config.maxContentLength + ' exceeded'));
        }
    });

    stream.on('end', function handleStreamEnd() {
        var d = Buffer.concat(responseBuffer);
        if (config.responseType !== 'arraybuffer') {
            d = d.toString('utf8');
        }
        var response = {
            data: transformData(
                d,
                res.headers,
                config.transformResponse
            ),
            status: res.statusCode,
            statusText: res.statusMessage,
            headers: res.headers,
            config: config,
            request: req
        };

        // Resolve or reject the Promise based on the status
        (res.statusCode >= 200 && res.statusCode < 300 ?
            resolve :
            reject)(response);
    });

    // Handle errors
    req.on('error', function handleRequestError(err) {
        if (aborted) return;
        reject(err);
    });
}