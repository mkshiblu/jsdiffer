// Resolve or reject the Promise based on the status
function settle(resolve, reject, response) {
    (response.status >= 200 && response.status < 300 ?
        resolve :
        reject)(response);
}

/*eslint consistent-return:0*/
module.exports = function httpAdapter(resolve, reject, config) {
    var data = config.data;
    var headers = config.headers;

    var response = {
        status: res.statusCode,
        statusText: res.statusMessage,
        headers: res.headers,
        config: config,
        request: req
    };

    if (config.responseType === 'stream') {
        response.data = stream;
        settle(resolve, reject, response);
    } else {
        var responseBuffer = [];
        stream.on('data', function handleStreamData(chunk) {
            responseBuffer.push(chunk);

            // make sure the content length is not over the maxContentLength if specified
            if (config.maxContentLength > -1 && Buffer.concat(responseBuffer).length > config.maxContentLength) {
                reject(new Error('maxContentLength size of ' + config.maxContentLength + ' exceeded'));
            }
        });

        stream.on('end', function handleStreamEnd() {
            var responseData = Buffer.concat(responseBuffer);
            if (config.responseType !== 'arraybuffer') {
                responseData = responseData.toString('utf8');
            }
            response.data = transformData(responseData, res.headers, config.transformResponse);
            settle(resolve, reject, response);
        });
    }
    // Handle errors
    req.on('error', function handleRequestError(err) {
        if (aborted) return;
        reject(err);
    });
}