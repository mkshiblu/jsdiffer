proto.handle = function(req, res, done) {
  var self = this;

  // store options for OPTIONS request
  // only used if OPTIONS request
  var options = [];

  // middleware and routes
  var stack = self.stack;

  // manage inter-router variables
  var parentParams = req.params;
  var parentUrl = req.baseUrl || '';
  done = restore(done, req, 'baseUrl', 'next', 'params');

  // setup next layer
  req.next = next;

  // for options requests, respond with a default if nothing else responds
  if (req.method === 'OPTIONS') {
    done = wrap(done, function(old, err) {
      if (err || options.length === 0) return old(err);
      sendOptionsResponse(res, options, old);
    });
  }
 }
 // send an OPTIONS response
 function sendOptionsResponse(res, options, next) {
   try {
     var body = options.join(',');
     res.set('Allow', body);
     res.send(body);
   } catch (err) {
     next(err);
   }
 }