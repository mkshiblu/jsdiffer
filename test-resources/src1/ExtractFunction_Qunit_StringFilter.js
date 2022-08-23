Test.prototype = {
  before: function () {
    config.current = this;
  },
  valid: function () {
    var include,
      filter = config.filter && config.filter.toLowerCase(),
      module = QUnit.urlParams.module && QUnit.urlParams.module.toLowerCase(),
      fullName = (this.module.name + ": " + this.testName).toLowerCase();

    function testInModuleChain(testModule) {
      var testModuleName = testModule.name ? testModule.name.toLowerCase() : null;
      if (testModuleName === module) {
        return true;
      } else if (testModule.parentModule) {
        return testInModuleChain(testModule.parentModule);
      } else {
        return false;
      }
    }

    // Internally-generated tests are always valid
    if (this.callback && this.callback.validTest) {
      return true;
    }

    if (config.testId.length > 0 && inArray(this.testId, config.testId) < 0) {
      return false;
    }

    if (module && !testInModuleChain(this.module)) {
      return false;
    }

    if (!filter) {
      return true;
    }

    include = filter.charAt(0) !== "!";
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