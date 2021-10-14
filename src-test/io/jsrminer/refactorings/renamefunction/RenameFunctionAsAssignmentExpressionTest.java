package io.jsrminer.refactorings.renamefunction;

import io.jsrminer.BaseTest;
import io.jsrminer.JSRefactoringMiner;
import io.jsrminer.api.IRefactoring;
import io.jsrminer.refactorings.MoveClassRefactoring;
import io.jsrminer.refactorings.RefactoringType;
import io.jsrminer.refactorings.RenameOperationRefactoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenameFunctionAsAssignmentExpressionTest extends BaseTest {
    static String code1 = """
               function bootstrap(element, modules) {
                                                  var resumeBootstrapInternal = function() {
                                                    element = jqLite(element);
                                                    modules = modules || [];
                                                    modules.unshift(['$provide', function($provide) {
                                                      $provide.value('$rootElement', element);
                                                    }]);
                                                    modules.unshift('ng');
                                                    var injector = createInjector(modules);
                                                    injector.invoke(['$rootScope', '$rootElement', '$compile', '$injector', '$animator',
                                                       function(scope, element, compile, injector, animator) {
                                                        scope.$apply(function() {
                                                          element.data('$injector', injector);
                                                          compile(element)(scope);
                                                        });
                                                        animator.enabled(true);
                                                      }]
                                                    );
                                                    return injector;
                                                  };

                                                  var NG_DEFER_BOOTSTRAP = /^NG_DEFER_BOOTSTRAP!/;

                                                  if (window && !NG_DEFER_BOOTSTRAP.test(window.name)) {
                                                    return resumeBootstrapInternal();
                                                  }

                                                  window.name = window.name.replace(NG_DEFER_BOOTSTRAP, '');
                                                  angular.resumeBootstrap = function(extraModules) {
                                                    forEach(extraModules, function(module) {
                                                      modules.push(module);
                                                    });
                                                    resumeBootstrapInternal();
                                                  };
                                                }
            """;

    static String code2 = """
                function bootstrap(element, modules) {
                                                             var doBootstrap = function() {
                                                               element = jqLite(element);
                                                               modules = modules || [];
                                                               modules.unshift(['$provide', function($provide) {
                                                                 $provide.value('$rootElement', element);
                                                               }]);
                                                               modules.unshift('ng');
                                                               var injector = createInjector(modules);
                                                               injector.invoke(['$rootScope', '$rootElement', '$compile', '$injector', '$animator',
                                                                  function(scope, element, compile, injector, animator) {
                                                                   scope.$apply(function() {
                                                                     element.data('$injector', injector);
                                                                     compile(element)(scope);
                                                                   });
                                                                   animator.enabled(true);
                                                                 }]
                                                               );
                                                               return injector;
                                                             };

                                                             var NG_DEFER_BOOTSTRAP = /^NG_DEFER_BOOTSTRAP!/;

                                                             if (window && !NG_DEFER_BOOTSTRAP.test(window.name)) {
                                                               return doBootstrap();
                                                             }

                                                             window.name = window.name.replace(NG_DEFER_BOOTSTRAP, '');
                                                             angular.resumeBootstrap = function(extraModules) {
                                                               forEach(extraModules, function(module) {
                                                                 modules.push(module);
                                                               });
                                                               doBootstrap();
                                                             };
                                                           }
            """;
    static RenameOperationRefactoring renameOperationRefactoring;
    static List<IRefactoring> refactorings;

    @BeforeAll
    public static void setup() {
        refactorings = new JSRefactoringMiner().detectBetweenCodeSnippets("snippet.js"
                , code1, "snippet.js", code2);
        renameOperationRefactoring = (RenameOperationRefactoring) refactorings.stream()
                .filter(r -> r.getRefactoringType().equals(RefactoringType.RENAME_METHOD))
                .findFirst().orElse(null);
    }

    @Test
    void testRefactoringCount() {
        assertEquals(1, refactorings.size());
    }

    @Test
    void testOriginalOperationName() {
        assertEquals("resumeBootstrapInternal", renameOperationRefactoring.getOriginalOperation().getName());
    }

    @Test
    void testOriginalOperationQualifiedName() {
        assertEquals("bootstrap.resumeBootstrapInternal", renameOperationRefactoring.getOriginalOperation().getQualifiedName());
    }

    @Test
    void testRenameOperationName() {
        assertEquals("doBootstrap", renameOperationRefactoring.getRenamedOperation().getName());
    }

    @Test
    void testRenameOperationQualifiedName() {
        assertEquals("bootstrap.doBootstrap", renameOperationRefactoring.getRenamedOperation().getQualifiedName());
    }

    @Test
    void testMapperMappingsCount() {
        assertEquals(7, renameOperationRefactoring.getBodyMapper().getMappings().size());
    }

    @Test
    void testOriginalOperationLocation() {
        assertEquals(110, renameOperationRefactoring.getOriginalOperation().getSourceLocation().start);
        assertEquals(1364, renameOperationRefactoring.getOriginalOperation().getSourceLocation().end);
    }

    void testRenamedOperationLocation() {
        assertEquals(110, renameOperationRefactoring.getRenamedOperation().getSourceLocation().start);
        assertEquals(1562, renameOperationRefactoring.getRenamedOperation().getSourceLocation().end);
    }
}
