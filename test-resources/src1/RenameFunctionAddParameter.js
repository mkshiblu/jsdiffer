/**
 * @ngdoc type
 * @name  select.SelectController
 * @description
 * The controller for the `<select>` directive. This provides support for reading
 * and writing the selected value(s) of the control and also coordinates dynamically
 * added `<option>` elements, perhaps by an `ngRepeat` directive.
 */
var SelectController =
        ['$element', '$scope', /** @this */ function($element, $scope) {

  var self = this,
      optionsMap = new NgMap();

  self.selectValueMap = {}; // Keys are the hashed values, values the original values

  // If the ngModel doesn't get provided then provide a dummy noop version to prevent errors
  self.ngModelCtrl = noopNgModelController;
  self.multiple = false;

  // The "unknown" option is one that is prepended to the list if the viewValue
  // does not match any of the options. When it is rendered the value of the unknown
  // option is '? XXX ?' where XXX is the hashKey of the value that is not known.
  //
  // We can't just jqLite('<option>') since jqLite is not smart enough
  // to create it in <select> and IE barfs otherwise.
  self.unknownOption = jqLite(window.document.createElement('option'));

  // The empty option is an option with the value '' that te application developer can
  // provide inside the select. When the model changes to a value that doesn't match an option,
  // it is selected - so if an empty option is provided, no unknown option is generated.
  // However, the empty option is not removed when the model matches an option. It is always selectable
  // and indicates that a "null" selection has been made.
  self.hasEmptyOption = false;
  self.emptyOption = undefined;

  self.renderUnknownOption = function(val) {
    var unknownVal = self.generateUnknownOptionValue(val);
    self.unknownOption.val(unknownVal);
    $element.prepend(self.unknownOption);
    setOptionAsSelected(self.unknownOption);
    $element.val(unknownVal);
  };

  self.updateUnknownOption = function(val) {
    var unknownVal = self.generateUnknownOptionValue(val);
    self.unknownOption.val(unknownVal);
    setOptionAsSelected(self.unknownOption);
    $element.val(unknownVal);
  };

  self.generateUnknownOptionValue = function(val) {
    return '? ' + hashKey(val) + ' ?';
  };

  self.removeUnknownOption = function() {
    if (self.unknownOption.parent()) self.unknownOption.remove();
  };

  self.selectEmptyOption = function() {
    if (self.emptyOption) {
      $element.val('');
      setOptionAsSelected(self.emptyOption);
    }
  };

  self.unselectEmptyOption = function() {
    if (self.hasEmptyOption) {
      self.emptyOption.removeAttr('selected');
    }
  };

  $scope.$on('$destroy', function() {
    // disable unknown option so that we don't do work when the whole select is being destroyed
    self.renderUnknownOption = noop;
  });

  // Read the value of the select control, the implementation of this changes depending
  // upon whether the select can have multiple values and whether ngOptions is at work.
  self.readValue = function readSingleValue() {
    var val = $element.val();
    // ngValue added option values are stored in the selectValueMap, normal interpolations are not
    var realVal = val in self.selectValueMap ? self.selectValueMap[val] : val;

    if (self.hasOption(realVal)) {
      return realVal;
    }

    return null;
  };


  // Write the value to the select control, the implementation of this changes depending
  // upon whether the select can have multiple values and whether ngOptions is at work.
  self.writeValue = function writeSingleValue(value) {
    // Make sure to remove the selected attribute from the previously selected option
    // Otherwise, screen readers might get confused
    var currentlySelectedOption = $element[0].options[$element[0].selectedIndex];
    if (currentlySelectedOption) currentlySelectedOption.removeAttribute('selected');

    if (self.hasOption(value)) {
      self.removeUnknownOption();

      var hashedVal = hashKey(value);
      $element.val(hashedVal in self.selectValueMap ? hashedVal : value);

      // Set selected attribute and property on selected option for screen readers
      var selectedOption = $element[0].options[$element[0].selectedIndex];
      setOptionAsSelected(jqLite(selectedOption));
    } else {
      if (value == null && self.emptyOption) {
        self.removeUnknownOption();
        self.selectEmptyOption();
      } else if (self.unknownOption.parent().length) {
        self.updateUnknownOption(value);
      } else {
        self.renderUnknownOption(value);
      }
    }
  };


  // Tell the select control that an option, with the given value, has been added
  self.addOption = function(value, element) {
    // Skip comment nodes, as they only pollute the `optionsMap`
    if (element[0].nodeType === NODE_TYPE_COMMENT) return;

    assertNotHasOwnProperty(value, '"option value"');
    if (value === '') {
      self.hasEmptyOption = true;
      self.emptyOption = element;
    }
    var count = optionsMap.get(value) || 0;
    optionsMap.set(value, count + 1);
    // Only render at the end of a digest. This improves render performance when many options
    // are added during a digest and ensures all relevant options are correctly marked as selected
    scheduleRender();
  };

  // Tell the select control that an option, with the given value, has been removed
  self.removeOption = function(value) {
    var count = optionsMap.get(value);
    if (count) {
      if (count === 1) {
        optionsMap.delete(value);
        if (value === '') {
          self.hasEmptyOption = false;
          self.emptyOption = undefined;
        }
      } else {
        optionsMap.set(value, count - 1);
      }
    }
  };

  // Check whether the select control has an option matching the given value
  self.hasOption = function(value) {
    return !!optionsMap.get(value);
  };


  var renderScheduled = false;
  function scheduleRender() {
    if (renderScheduled) return;
    renderScheduled = true;
    $scope.$$postDigest(function() {
      renderScheduled = false;
      self.ngModelCtrl.$render();
    });
  }

  var updateScheduled = false;
  function scheduleViewValueUpdate(renderAfter) {
    if (updateScheduled) return;

    updateScheduled = true;

    $scope.$$postDigest(function() {
      if ($scope.$$destroyed) return;

      updateScheduled = false;
      self.ngModelCtrl.$setViewValue(self.readValue());
      if (renderAfter) self.ngModelCtrl.$render();
    });
  }


  self.registerOption = function(optionScope, optionElement, optionAttrs, interpolateValueFn, interpolateTextFn) {

    if (optionAttrs.$attr.ngValue) {
      // The value attribute is set by ngValue
      var oldVal, hashedVal = NaN;
      optionAttrs.$observe('value', function valueAttributeObserveAction(newVal) {

        var removal;
        var previouslySelected = optionElement.prop('selected');

        if (isDefined(hashedVal)) {
          self.removeOption(oldVal);
          delete self.selectValueMap[hashedVal];
          removal = true;
        }

        hashedVal = hashKey(newVal);
        oldVal = newVal;
        self.selectValueMap[hashedVal] = newVal;
        self.addOption(newVal, optionElement);
        // Set the attribute directly instead of using optionAttrs.$set - this stops the observer
        // from firing a second time. Other $observers on value will also get the result of the
        // ngValue expression, not the hashed value
        optionElement.attr('value', hashedVal);

        if (removal && previouslySelected) {
          scheduleViewValueUpdate();
        }

      });
    } else if (interpolateValueFn) {
      // The value attribute is interpolated
      optionAttrs.$observe('value', function valueAttributeObserveAction(newVal) {
        // This method is overwritten in ngOptions and has side-effects!
        self.readValue();

        var removal;
        var previouslySelected = optionElement.prop('selected');

        if (isDefined(oldVal)) {
          self.removeOption(oldVal);
          removal = true;
        }
        oldVal = newVal;
        self.addOption(newVal, optionElement);

        if (removal && previouslySelected) {
          scheduleViewValueUpdate();
        }
      });
    } else if (interpolateTextFn) {
      // The text content is interpolated
      optionScope.$watch(interpolateTextFn, function interpolateWatchAction(newVal, oldVal) {
        optionAttrs.$set('value', newVal);
        var previouslySelected = optionElement.prop('selected');
        if (oldVal !== newVal) {
          self.removeOption(oldVal);
        }
        self.addOption(newVal, optionElement);

        if (oldVal && previouslySelected) {
          scheduleViewValueUpdate();
        }
      });
    } else {
      // The value attribute is static
      self.addOption(optionAttrs.value, optionElement);
    }


    optionAttrs.$observe('disabled', function(newVal) {

      // Since model updates will also select disabled options (like ngOptions),
      // we only have to handle options becoming disabled, not enabled

      if (newVal === 'true' || newVal && optionElement.prop('selected')) {
        if (self.multiple) {
          scheduleViewValueUpdate(true);
        } else {
          self.ngModelCtrl.$setViewValue(null);
          self.ngModelCtrl.$render();
        }
      }
    });

    optionElement.on('$destroy', function() {
      var currentValue = self.readValue();
      var removeValue = optionAttrs.value;

      self.removeOption(removeValue);
      scheduleRender();

      if (self.multiple && currentValue && currentValue.indexOf(removeValue) !== -1 ||
          currentValue === removeValue
      ) {
        // When multiple (selected) options are destroyed at the same time, we don't want
        // to run a model update for each of them. Instead, run a single update in the $$postDigest
        scheduleViewValueUpdate(true);
      }
    });
  };

  function setOptionAsSelected(optionEl) {
    optionEl.prop('selected', true); // needed for IE
    optionEl.attr('selected', true);
  }
}];
