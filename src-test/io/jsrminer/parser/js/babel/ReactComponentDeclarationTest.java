package io.jsrminer.parser.js.babel;

import io.jsrminer.BaseTest;
import io.rminerx.core.api.IClassDeclaration;
import io.rminerx.core.api.ISourceFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReactComponentDeclarationTest extends BaseTest {
    static IClassDeclaration classDeclaration;
    static ISourceFile sourceFile;

    @BeforeAll
    public static void setup() throws IOException {
        var parser = new BabelParser();
        sourceFile = parser.parseSource(Files.readString(Path.of((getRootResourceDirectory() + "parser\\react_component_declarartion.js"))), "react_component_declarartion.js");
        classDeclaration = sourceFile.getClassDeclarations().get(0);
    }

    @Test
    public void testName() {
        assertEquals("Circle", classDeclaration.getName());
    }

    @Test
    public void testQualifiedName() {
        assertEquals("Circle", classDeclaration.getQualifiedName());
    }

    @Test
    public void testParentContainerQualifiedName() {
        assertEquals("file.js", classDeclaration.getParentContainerQualifiedName());
    }

    @Test
    public void testSuperClassName() {
        assertEquals("Shape", classDeclaration.getSuperClass().getTypeName());
    }

    @Test
    public void testAttributeCount() {
        assertEquals(1, classDeclaration.getAttributes().size());
    }


    @Test
    public void testAttributeName() {
        var attribute = classDeclaration.getAttributes().get(0);
        assertEquals("radius", attribute.getName());
    }


    @Test
    public void testAttributeModifiers() {
        var attribute = classDeclaration.getAttributes().get(0);
        assertTrue(attribute.isStatic());
    }

    @Test
    public void testAttributeLocation() {
        var attribute = classDeclaration.getAttributes().get(0);
        var  attributeLocation = attribute.getLocationInfo();
        assertEquals(29, attributeLocation.start);
        assertEquals(48, attributeLocation.end);
    }

    @Test
    public void testAttributeVariableDeclarationLocation() {
        var variableDeclaration = classDeclaration.getAttributes().get(0).getVariableDeclaration();
        var  location = variableDeclaration.getSourceLocation();
        assertEquals(29, location.start);
        assertEquals(48, location.end);
    }

    @Test
    public void testAttributeVariableDeclarationScope() {
        var variableDeclaration = classDeclaration.getAttributes().get(0).getVariableDeclaration();
        var  scope = variableDeclaration.getScope();
        assertEquals(29, scope.start);
        assertEquals(50, scope.end);
    }

    @Test
    public void testAttributeInitializer() {
        var attribute = classDeclaration.getAttributes().get(0);
        assertEquals("10", attribute.getVariableDeclaration().getInitializer().getText());
    }
}
