
Test.prototype = {
  before: function () {
    config.current = this;
  },
  valid: function () {
    var filter = config.filter,
      regexFilter = /^(!?)\/([\w\W]*)\/(i?$)/.exec(filter),
      module = config.module && config.module.toLowerCase(),
      fullName = (this.module.name + ": " + this.testName);

    function moduleChainNameMatch(testModule) {
      var testModuleName = testModule.name ? testModule.name.toLowerCase() : null;
      if (testModuleName === module) {
        return true;
      } else if (testModule.parentModule) {
        return moduleChainNameMatch(testModule.parentModule);
      } else {
        return false;
      }
    }

    function moduleChainIdMatch(testModule) {
      return inArray(testModule.moduleId, config.moduleId) > -1 ||
        testModule.parentModule && moduleChainIdMatch(testModule.parentModule);
    }

    // Internally-generated tests are always valid
    if (this.callback && this.callback.validTest) {
      return true;
    }

    if (config.moduleId && config.moduleId.length > 0 &&
      !moduleChainIdMatch(this.module)) {

      return false;
    }

    if (config.testId && config.testId.length > 0 &&
      inArray(this.testId, config.testId) < 0) {

      return false;
    }

    if (module && !moduleChainNameMatch(this.module)) {
      return false;
    }

    if (!filter) {
      return true;
    }

    return regexFilter ?
      this.regexFilter(!!regexFilter[1], regexFilter[2], regexFilter[3], fullName) :
      this.stringFilter(filter, fullName);
  },

  regexFilter: function (exclude, pattern, flags, fullName) {
    var regex = new RegExp(pattern, flags);
    var match = regex.test(fullName);

    return match !== exclude;
  },

  stringFilter: function (filter, fullName) {
    filter = filter.toLowerCase();
    fullName = fullName.toLowerCase();

    var include = filter.charAt(0) !== "!";
    if (!include) {
      filter = filter.slice(1);
    }

    // If the filter matches, we need to honour include
    if (fullName.indexOf(filter) !== -1) {
      return include;
    }

    // Otherwise, do the opposite
    return !include;
  }
};