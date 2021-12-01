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
            elementControllers = createMap();
            for (var name in controllerDirectives) {
                var directive = controllerDirectives[name];
                var locals = {
                    $scope: directive === newIsolateScopeDirective || directive.$$isolateScope ? isolateScope : scope,
                    $element: $element,
                    $attrs: attrs,
                    $transclude: transcludeFn
                };

                var controllerConstructor = directive.controller;
                if (controllerConstructor === '@') {
                    controllerConstructor = attrs[name];
                }

                var instance = $controller(controllerConstructor, locals, directive.controllerAs);

                $element.data('$' + name + 'Controller', instance);

                // Initialize bindToController bindings
                var bindings = directive.$$bindings.bindToController;
                var bindingInfo = initializeDirectiveBindings(controllerScope, attrs, instance, bindings, directive);

                elementControllers[name] = { instance: instance, bindingInfo: bindingInfo };
            }
        }
    }
}

function getControllers(directiveName, require, $element, elementControllers) {
    var value;
    return value || null;
}
