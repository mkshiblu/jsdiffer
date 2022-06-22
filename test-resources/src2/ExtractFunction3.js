function eachResource(request, callback) {
  const browser = request.browser ||
    WebApp.categorizeRequest(request).browser;

  const arch = isModern(browser)
    ? "web.browser"
    : "web.browser.legacy";

  WebApp.clientPrograms[arch].manifest.forEach(callback);
}

const sizeCheck = () => WebApp.connectHandlers.use((req, res, next) => {
  let totalSize = 0;

  eachResource(req, resource => {
    if (resource.where === 'client' &&
        ! RoutePolicy.classify(resource.url) &&
        ! shouldSkip(resource)) {
      totalSize += resource.size;
    }
  });

  if (totalSize > 5 * 1024 * 1024) {
    Meteor._debug(
      "** You are using the appcache package but the total size of the\n" +
      "** cached resources is " +
      `${(totalSize / 1024 / 1024).toFixed(1)}MB.\n` +
      "**\n" +
      "** This is over the recommended maximum of 5 MB and may break your\n" +
      "** app in some browsers! See http://docs.meteor.com/#appcache\n" +
      "** for more information and fixes.\n"
    );
  }
});