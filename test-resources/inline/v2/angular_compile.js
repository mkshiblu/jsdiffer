function applyDirectivesToNode(directives, compileNode, templateAttrs, transcludeFn,
    jqCollection, originalReplaceDirective, preLinkFns, postLinkFns,
    previousCompileContext) {
    previousCompileContext = previousCompileContext || {};

    var terminalPriority = -Number.MAX_VALUE,
        newScopeDirective = previousCompileContext.newScopeDirective,
        controllerDirectives = previousCompileContext.controllerDirectives;

    function nodeLinkFn(childLinkFn, scope, linkNode, $rootElement, boundTranscludeFn) {
        var i, ii, linkFn, isolateScope, controllerScope, elementControllers, transcludeFn, $element,
            attrs, scopeBindingInfo;

        controllerScope = scope;
        if (controllerDirectives) {
            elementControllers = setupControllers($element, attrs, transcludeFn, controllerDirectives, isolateScope, scope, newIsolateScopeDirective);
        }

        // Initialize bindToController bindings
        for (var name in elementControllers) {
            var controllerDirective = controllerDirectives[name];
            var controller = elementControllers[name];
            var bindings = controllerDirective.$$bindings.bindToController;

            controller.instance = controller();
            $element.data('$' + controllerDirective.name + 'Controller', controller.instance);
            controller.bindingInfo =
                initializeDirectiveBindings(controllerScope, attrs, controller.instance, bindings, controllerDirective);
        }
    }
}

function getControllers(directiveName, require, $element, elementControllers) {
    var value;
    return value || null;
}

function setupControllers($element, attrs, transcludeFn, controllerDirectives, isolateScope, scope, newIsolateScopeDirective) {
    var elementControllers = createMap();
    for (var controllerKey in controllerDirectives) {
        var directive = controllerDirectives[controllerKey];
        var locals = {
            $scope: directive === newIsolateScopeDirective || directive.$$isolateScope ? isolateScope : scope,
            $element: $element,
            $attrs: attrs,
            $transclude: transcludeFn
        };

        var controller = directive.controller;
        if (controller === '@') {
            controller = attrs[directive.name];
        }

        var controllerInstance = $controller(controller, locals, true, directive.controllerAs);
        elementControllers[directive.name] = controllerInstance;
        $element.data('$' + directive.name + 'Controller', controllerInstance.instance);
    }
    return elementControllers;
}